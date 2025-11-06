package org.icesi.network;

import org.icesi.server.ChatServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class UDPServer implements Runnable{
    private final DatagramSocket socket;
    private final int port;
    private final Map<Integer, InetSocketAddress> clientAddresses = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> activeCallPairs = new ConcurrentHashMap<>();
    private final Map<Integer, Set<Integer>> groupCalls = new ConcurrentHashMap<>();
    private static final int BUFFER_SIZE = 4096;

    public UDPServer(int port) throws SocketException {
        this.port = port;
        this.socket = new DatagramSocket(port);
        this.socket.setReuseAddress(true);
    }

    @Override
    public void run() {
        System.out.println("[UDP_SERVER] ✓ Servidor UDP escuchando en puerto " + port);
        byte[] buffer = new byte[BUFFER_SIZE];

        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                InetSocketAddress senderAddr = new InetSocketAddress(packet.getAddress(), packet.getPort());
                Integer senderId = findUserByAddress(senderAddr);

                if (senderId != null) {
                    handleIncomingAudio(senderId, packet);
                }
            } catch (IOException e) {
                System.err.println("[UDP_SERVER] ✗ Error: " + e.getMessage());
                break;
            }
        }
    }

    private void handleIncomingAudio(Integer senderId, DatagramPacket packet) {
        // Verificar llamadas P2P
        if (activeCallPairs.containsKey(senderId)) {
            Integer recipientId = activeCallPairs.get(senderId);
            forwardToUser(recipientId, packet);
        }

        // Verificar llamadas grupales
        if (isInGroupCall(senderId)) {
            forwardToGroup(senderId, packet);
        }
    }

    private boolean isInGroupCall(int userId) {
        return groupCalls.values().stream()
                .anyMatch(members -> members.contains(userId));
    }

    private void forwardToGroup(int senderId, DatagramPacket originalPacket) {
        groupCalls.forEach((groupId, members) -> {
            if (members.contains(senderId)) {
                members.forEach(memberId -> {
                    if (memberId != senderId) { // No enviar al mismo usuario
                        forwardToUser(memberId, originalPacket);
                    }
                });
            }
        });
    }

    private void forwardToUser(Integer userId, DatagramPacket originalPacket) {
        InetSocketAddress recipientAddr = clientAddresses.get(userId);
        if (recipientAddr != null) {
            try {
                DatagramPacket sendPacket = new DatagramPacket(
                        originalPacket.getData(), originalPacket.getLength(),
                        recipientAddr.getAddress(), recipientAddr.getPort()
                );
                socket.send(sendPacket);
            } catch (IOException e) {
                System.err.println("[UDP_SERVER] ✗ Error enviando a usuario " + userId + ": " + e.getMessage());
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
        activeCallPairs.values().removeIf(id -> id.equals(userId));

        // Remover de llamadas grupales
        groupCalls.values().forEach(members -> members.remove(userId));
        groupCalls.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        System.out.println("[UDP_SERVER] ✓ Cliente #" + userId + " desregistrado");
    }

    public void startCallPair(int userId1, int userId2) {
        activeCallPairs.put(userId1, userId2);
        activeCallPairs.put(userId2, userId1);
        System.out.println("[UDP_SERVER] ✓ Llamada P2P iniciada: #" + userId1 + " <-> #" + userId2);
    }

    public void startGroupCall(int groupId, Set<Integer> members) {
        groupCalls.put(groupId, Collections.synchronizedSet(new HashSet<>(members)));
        System.out.println("[UDP_SERVER] ✓ Llamada grupal #" + groupId +
                " con " + members.size() + " miembros");
    }

    public void endCallPair(int userId) {
        Integer otherUserId = activeCallPairs.remove(userId);
        if (otherUserId != null) {
            activeCallPairs.remove(otherUserId);
            System.out.println("[UDP_SERVER] ✓ Llamada finalizada entre #" + userId + " y #" + otherUserId);
        }
    }

    public void endGroupCall(int groupId) {
        Set<Integer> removed = groupCalls.remove(groupId);
        if (removed != null) {
            System.out.println("[UDP_SERVER] ✓ Llamada grupal #" + groupId + " finalizada");
        }
    }

    public void cleanup() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
