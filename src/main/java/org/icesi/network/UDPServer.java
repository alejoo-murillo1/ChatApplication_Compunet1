package org.icesi.network;

import org.icesi.server.ChatServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UDPServer implements Runnable{
    private DatagramSocket socket;
    private int port;
    private ChatServer chatServer;
    private Map<Integer, InetSocketAddress> clientAddresses = new ConcurrentHashMap<>();
    private Map<Integer, Integer> activeCallPairs = new ConcurrentHashMap<>();
    private static final int BUFFER_SIZE = 4096;

    public UDPServer(int port, ChatServer chatServer) throws SocketException {
        this.port = port;
        this.chatServer = chatServer;
        this.socket = new DatagramSocket(port);
        this.socket.setReuseAddress(true);
    }

    @Override
    public void run() {
        System.out.println("[UDP_SERVER] ✓ Servidor UDP escuchando en puerto " + port);
        byte[] buffer = new byte[65535];

        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Encontrar quién envió el paquete
                InetSocketAddress senderAddr = new InetSocketAddress(packet.getAddress(), packet.getPort());
                Integer senderId = findUserByAddress(senderAddr);

                if (senderId != null) {
                    // Verificar si el usuario está en una llamada activa
                    if (activeCallPairs.containsKey(senderId)) {
                        Integer recipientId = activeCallPairs.get(senderId);
                        InetSocketAddress recipientAddr = clientAddresses.get(recipientId);

                        if (recipientAddr != null) {
                            // Retransmitir SOLO al otro usuario en la llamada
                            DatagramPacket sendPacket = new DatagramPacket(
                                    packet.getData(), packet.getLength(),
                                    recipientAddr.getAddress(), recipientAddr.getPort()
                            );
                            socket.send(sendPacket);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("[UDP_SERVER] ✗ Error: " + e.getMessage());
            }
        }
    }

    private Integer findUserByAddress(InetSocketAddress addr) {
        for (Map.Entry<Integer, InetSocketAddress> entry : clientAddresses.entrySet()) {
            InetSocketAddress registered = entry.getValue();
            if (registered.getAddress().equals(addr.getAddress()) &&
                    registered.getPort() == addr.getPort()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void registerClient(int userId, InetSocketAddress address) {
        clientAddresses.put(userId, address);
        System.out.println("[UDP_SERVER] ✓ Cliente #" + userId + " registrado en " + address);
    }

    public void unregisterClient(int userId) {
        clientAddresses.remove(userId);
        activeCallPairs.remove(userId);
        activeCallPairs.values().removeIf(id -> id == userId);
        System.out.println("[UDP_SERVER] ✓ Cliente #" + userId + " desregistrado");
    }

    public void startCallPair(int userId1, int userId2) {
        activeCallPairs.put(userId1, userId2);
        activeCallPairs.put(userId2, userId1);
        System.out.println("[UDP_SERVER] ✓ Llamada P2P iniciada: #" + userId1 + " <-> #" + userId2);
    }

    public void startGroupCall(int groupId, Set<Integer> members) {
        // Para llamadas de grupo, todos los miembros se comunican entre sí
        for (Integer userId : members) {
            activeCallPairs.put(userId, groupId);
        }
        System.out.println("[UDP_SERVER] ✓ Llamada grupal iniciada: Grupo #" + groupId + " con " + members.size() + " miembros");
    }

    public void endCallPair(int userId) {
        Integer otherUserId = activeCallPairs.remove(userId);
        if (otherUserId != null) {
            activeCallPairs.remove(otherUserId);
            System.out.println("[UDP_SERVER] ✓ Llamada finalizada entre #" + userId + " y #" + otherUserId);
        }
    }
}
