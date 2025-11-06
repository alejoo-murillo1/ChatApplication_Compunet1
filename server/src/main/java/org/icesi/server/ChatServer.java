package org.icesi.server;

import org.icesi.db.DatabaseManager;
import org.icesi.model.Message;
import org.icesi.model.VoiceMessage;
import org.icesi.network.UDPServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


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
        this.udpServer = new UDPServer(UDP_PORT);
        new Thread(udpServer).start();
    }

    public void start() {
        System.out.println("╔════════════════════════════════╗");
        System.out.println("║        SERVIDOR DE CHAT        ║");
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
                break;
            }
        }
    }

    public void registerClient(int userId, ClientHandler handler, int udpPort) {
        connectedClients.put(userId, handler);

        // CORRECCIÓN: Obtener la dirección del socket del cliente
        InetSocketAddress udpAddress = new InetSocketAddress(
                handler.getSocket().getInetAddress(), // Usar getSocket() en lugar de getClientAddress()
                udpPort
        );
        udpServer.registerClient(userId, udpAddress);

        System.out.println("[SERVER] Cliente conectado: ID " + userId + " (UDP: " + udpPort + ")");
    }

    public void unregisterClient(int userId) {
        connectedClients.remove(userId);
        udpServer.unregisterClient(userId);
        System.out.println("[SERVER] Cliente desconectado: ID " + userId);
    }

    public void broadcastMessage(Message msg) {
        if (msg.isGroup()) {
            // SOLUCIÓN: Enviar solo a miembros del grupo
            List<Integer> groupMembers = db.getGroupMembers(msg.getRecipientId());
            for (Integer memberId : groupMembers) {
                ClientHandler member = connectedClients.get(memberId);
                if (member != null && memberId != msg.getSenderId()) {
                    try {
                        member.sendMessage(msg);
                    } catch (IOException e) {
                        System.err.println("[SERVER] Error enviando mensaje grupal: " + e.getMessage());
                    }
                }
            }
        } else {
            // Mensaje privado
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

    public void broadcastVoiceMessage(VoiceMessage voiceMsg) {
        if (voiceMsg.isGroup()) {
            // SOLUCIÓN: Enviar solo a miembros del grupo
            List<Integer> groupMembers = db.getGroupMembers(voiceMsg.getRecipientId());
            for (Integer memberId : groupMembers) {
                ClientHandler member = connectedClients.get(memberId);
                if (member != null && memberId != voiceMsg.getSenderId()) {
                    try {
                        member.sendVoiceMessage(voiceMsg);
                    } catch (IOException e) {
                        System.err.println("[SERVER] Error enviando audio grupal: " + e.getMessage());
                    }
                }
            }
        } else {
            // Audio privado
            ClientHandler recipient = connectedClients.get(voiceMsg.getRecipientId());
            if (recipient != null) {
                try {
                    recipient.sendVoiceMessage(voiceMsg);
                } catch (IOException e) {
                    System.err.println("[SERVER] Error enviando audio: " + e.getMessage());
                }
            }
        }
    }

    // NUEVO: Método para manejar llamadas grupales correctamente
    public void startGroupCall(int groupId, int callerId) {
        List<Integer> members = db.getGroupMembers(groupId);
        Set<Integer> onlineMembers = members.stream()
                .filter(memberId -> connectedClients.containsKey(memberId) && memberId != callerId)
                .collect(Collectors.toSet());

        // Registrar llamada grupal en UDP
        udpServer.startGroupCall(groupId, onlineMembers);

        // Notificar a miembros
        for (Integer memberId : onlineMembers) {
            ClientHandler member = connectedClients.get(memberId);
            if (member != null) {
                try {
                    String callerName = db.getUsernameById(callerId);
                    member.getOut().writeObject("GROUP_CALL_INCOMING:" + groupId + ":" + callerName);
                    member.getOut().flush();
                } catch (IOException e) {
                    System.err.println("[GROUP_CALL] Error notificando a miembro " + memberId);
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

    public void endGroupCall(int groupId) {
        if (udpServer != null) {
            udpServer.endGroupCall(groupId);
        }
    }

    public void broadcastToAll(String message) {
        connectedClients.values().forEach(handler -> {
            try {
                handler.getOut().writeObject(message);
                handler.getOut().flush();
            } catch (IOException e) {
                System.err.println("[SERVER] ✗ Error enviando broadcast: " + e.getMessage());
            }
        });
    }

    public void broadcastStatusUpdate(int userId, String status) {
        String statusMsg = "USER_STATUS_UPDATE:" + userId + ":" + status;
        broadcastToAll(statusMsg);
    }

    public void cleanup() {
        try {
            threadPool.shutdown();
            if (udpServer != null) {
                udpServer.cleanup();
            }
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("[SERVER] Error limpiando recursos: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer();
        try {
            server.start();
        } finally {
            server.cleanup();
        }
    }
}

