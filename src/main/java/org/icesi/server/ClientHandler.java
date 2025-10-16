package org.icesi.server;

import org.icesi.db.DatabaseManager;
import org.icesi.model.Message;
import org.icesi.model.VoiceMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private final Socket socket;
    private final ChatServer server;
    private final DatabaseManager db;
    private ObjectOutputStream out;
    private int userId;
    private String username;

    public ClientHandler(Socket socket, ChatServer server, DatabaseManager db) {
        this.socket = socket;
        this.server = server;
        this.db = db;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Recibir login
            username = (String) in.readObject();
            userId = db.registerUser(username);

            // Recibir puerto UDP del cliente
            int udpPort = in.readInt();

            updateUserStatus("online");
            server.registerClient(userId, this, udpPort);

            out.writeObject("LOGIN_SUCCESS:" + userId);
            out.flush();

            while (true) {
                Object obj = in.readObject();

                if (obj instanceof Message msg) {
                    handleMessage(msg);
                }
                else if (obj instanceof VoiceMessage voiceMsg) {
                    handleVoiceMessage(voiceMsg);
                }
                else if (obj instanceof String cmd) {
                    handleCommand(cmd);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[HANDLER] Desconexión del cliente " + username + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void handleMessage(Message msg) {
        if (msg.isGroup()) {
            db.saveGroupMessage(msg.getSenderId(), msg.getRecipientId(), msg.getContent());
        } else {
            db.savePrivateMessage(msg.getSenderId(), msg.getRecipientId(), msg.getContent());
        }
        server.broadcastMessage(msg);
        System.out.println("[MSG] " + username + " -> " +
                (msg.isGroup() ? "Grupo " : "Usuario ") + msg.getRecipientId() + ": " + msg.getContent());
    }

    private void handleVoiceMessage(VoiceMessage voiceMsg) {
        if (voiceMsg.isGroup()) {
            db.saveGroupVoiceMessage(voiceMsg.getSenderId(), voiceMsg.getRecipientId(), voiceMsg.getAudioData());
        } else {
            db.savePrivateVoiceMessage(voiceMsg.getSenderId(), voiceMsg.getRecipientId(), voiceMsg.getAudioData());
        }
        server.broadcastVoiceMessage(voiceMsg);
        System.out.println("[VOICE] " + username + " envió mensaje de voz a " +
                (voiceMsg.isGroup() ? "grupo " : "usuario ") + voiceMsg.getRecipientId());
    }

    private void handleCommand(String cmd) {
        if (cmd.startsWith("CALL:")) {
            handleCall(cmd);
        }
        else if (cmd.startsWith("CALL_ACCEPTED:")) {
            handleCallAccepted(cmd);
        }
        else if (cmd.startsWith("CALL_REJECTED:")) {
            handleCallRejected(cmd);
        }
        else if (cmd.startsWith("GROUP_CALL:")) {
            handleGroupCall(cmd);
        }
        else if (cmd.startsWith("GROUP_CALL_ACCEPTED:")) {
            handleGroupCallAccepted(cmd);
        }
        else if (cmd.startsWith("GROUP_CALL_REJECTED:")) {
            handleGroupCallRejected(cmd);
        }
        else if (cmd.equals("END_CALL")) {
            handleEndCall();
        }
        else if (cmd.startsWith("STATUS_UPDATE:")) {
            handleStatusUpdate(cmd);
        }
    }

    private void handleCall(String cmd) {
        String[] parts = cmd.split(":");
        if (parts.length > 1) {
            int recipientId = Integer.parseInt(parts[1]);
            ClientHandler recipient = server.getClient(recipientId);
            if (recipient != null) {
                try {
                    updateUserStatus("in_call");
                    recipient.out.writeObject("INCOMING_CALL:" + userId + ":" + username);
                    recipient.out.flush();
                    System.out.println("[CALL] " + username + " está llamando a usuario " + recipientId);
                } catch (IOException e) {
                    System.err.println("[CALL] Error estableciendo llamada: " + e.getMessage());
                }
            } else {
                try {
                    out.writeObject("CALL_ERROR:Usuario no encontrado");
                    out.flush();
                } catch (IOException e) {
                    System.err.println("[CALL] Error: " + e.getMessage());
                }
            }
        }
    }

    private void handleCallAccepted(String cmd) {
        String[] parts = cmd.split(":");
        if (parts.length > 1) {
            int callerId = Integer.parseInt(parts[1]);
            ClientHandler caller = server.getClient(callerId);
            if (caller != null) {
                try {
                    updateUserStatus("in_call");
                    db.setUserStatus(callerId, "in_call");
                    caller.out.writeObject("CALL_ACCEPTED");
                    caller.out.flush();
                    // Establecer el par de llamada en UDP
                    server.udpServer.startCallPair(userId, callerId);
                    System.out.println("[CALL] " + username + " aceptó la llamada de usuario " + callerId);
                } catch (IOException e) {
                    System.err.println("[CALL] Error aceptando llamada: " + e.getMessage());
                }
            }
        }
    }

    private void handleCallRejected(String cmd) {
        String[] parts = cmd.split(":");
        if (parts.length > 1) {
            int callerId = Integer.parseInt(parts[1]);
            ClientHandler caller = server.getClient(callerId);
            if (caller != null) {
                try {
                    caller.out.writeObject("CALL_REJECTED");
                    caller.out.flush();
                    System.out.println("[CALL] " + username + " rechazó la llamada de usuario " + callerId);
                } catch (IOException e) {
                    System.err.println("[CALL] Error rechazando llamada: " + e.getMessage());
                }
            }
        }
    }

    private void handleEndCall() {
        updateUserStatus("online");
        server.endCall(userId);
        System.out.println("[CALL] " + username + " finalizó la llamada");
    }

    private void handleGroupCall(String cmd) {
        String[] parts = cmd.split(":");
        if (parts.length > 1) {
            try {
                int groupId = Integer.parseInt(parts[1]);
                updateUserStatus("in_call");
                server.startGroupCall(groupId, userId);
                System.out.println("[GROUP_CALL] ✓ " + username + " inició llamada grupal #" + groupId);
            } catch (NumberFormatException e) {
                System.err.println("[GROUP_CALL] ✗ Error: " + e.getMessage());
            }
        }
    }

    private void handleGroupCallAccepted(String cmd) {
        String[] parts = cmd.split(":");
        if (parts.length > 1) {
            try {
                int groupId = Integer.parseInt(parts[1]);
                updateUserStatus("in_call");
                System.out.println("[GROUP_CALL] ✓ " + username + " se unió a la llamada grupal #" + groupId);
            } catch (NumberFormatException e) {
                System.err.println("[GROUP_CALL] ✗ Error: " + e.getMessage());
            }
        }
    }

    private void handleGroupCallRejected(String cmd) {
        String[] parts = cmd.split(":");
        if (parts.length > 1) {
            try {
                int groupId = Integer.parseInt(parts[1]);
                updateUserStatus("online");
                System.out.println("[GROUP_CALL] ✓ " + username + " rechazó la llamada grupal #" + groupId);
            } catch (NumberFormatException e) {
                System.err.println("[GROUP_CALL] ✗ Error: " + e.getMessage());
            }
        }
    }

    private void handleStatusUpdate(String cmd) {
        String[] parts = cmd.split(":");
        if (parts.length > 1) {
            String status = parts[1];
            db.setUserStatus(userId, status);
            server.broadcastStatusUpdate(userId, status);
        }
    }

    private void updateUserStatus(String status) {
        db.setUserStatus(userId, status);
    }

    public void sendMessage(Message msg) throws IOException {
        out.writeObject(msg);
        out.flush();
    }

    public void sendVoiceMessage(VoiceMessage voiceMsg) throws IOException {
        out.writeObject(voiceMsg);
        out.flush();
    }

    private void cleanup() {
        updateUserStatus("offline");
        server.unregisterClient(userId);
        try {
            if (out != null) {
                out.close();
            }
            socket.close();
        } catch (IOException e) {
            System.err.println("[HANDLER] Error cerrando socket: " + e.getMessage());
        }
    }

    public ObjectOutputStream getOut() {
        return out;
    }

    public InetAddress getClientAddress() {
        return socket.getInetAddress();
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUsername() {
        return username;
    }
}
