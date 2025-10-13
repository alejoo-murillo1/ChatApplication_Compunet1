package org.icesi.server;

import org.icesi.db.DatabaseManager;
import org.icesi.model.Message;
import org.icesi.network.UDPServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int PORT = 5000;
    private static final int UDP_PORT = 5001;
    private final ServerSocket serverSocket;
    private final DatabaseManager db;
    private final Map<Integer, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    public UDPServer udpServer;

    public ChatServer() throws IOException {
        this.db = new DatabaseManager();
        this.serverSocket = new ServerSocket(PORT);
        this.udpServer = new UDPServer(UDP_PORT, this);
        new Thread(udpServer).start();
    }

    public void start() {
        System.out.println("╔════════════════════════════════╗");
        System.out.println("║   SERVIDOR DE CHAT v1.0        ║");
        System.out.println("╚════════════════════════════════╝");
        System.out.println("[SERVER] ✓ Chat iniciado en puerto " + PORT);
        System.out.println("[SERVER] Esperando conexiones...\n");

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this, db);
                threadPool.execute(handler);
            } catch (IOException e) {
                System.err.println("[SERVER] ✗ Error aceptando cliente: " + e.getMessage());
            }
        }
    }

    public void registerClient(int userId, ClientHandler handler) {
        connectedClients.put(userId, handler);
        System.out.println("[SERVER] Cliente conectado: ID " + userId);
    }

    public void unregisterClient(int userId) {
        connectedClients.remove(userId);
        System.out.println("[SERVER] Cliente desconectado: ID " + userId);
    }

    public void broadcastMessage(Message msg) {
        if (msg.isGroup()) {
            connectedClients.values().forEach(handler -> {
                try {
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    System.err.println("[SERVER] Error enviando mensaje: " + e.getMessage());
                }
            });
        } else {
            ClientHandler recipient = connectedClients.get(msg.getRecipientId());
            if (recipient != null) {
                try {
                    recipient.sendMessage(msg);
                } catch (IOException e) {
                    System.err.println("[SERVER] Error enviando mensaje: " + e.getMessage());
                }
            }
        }
    }

    public ClientHandler getClient(int userId) {
        return connectedClients.get(userId);
    }

    public void endCall(int userId) {
        if (udpServer != null) {
            udpServer.endCallPair(userId);
        }
    }

    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer();
        server.start();
    }
}

