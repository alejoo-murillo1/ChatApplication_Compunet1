package org.icesi.server;

import org.icesi.db.DatabaseManager;
import org.icesi.model.Message;
import org.icesi.model.VoiceMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
            db.setUserStatus(userId, "online");
            server.registerClient(userId, this);
            out.writeObject("LOGIN_SUCCESS:" + userId);
            out.flush();

            while (true) {
                Object obj = in.readObject();

                if (obj instanceof Message msg) {
                    db.saveMessage(msg.getSenderId(), msg.getRecipientId(),
                            msg.getContent(), msg.isGroup());
                    server.broadcastMessage(msg);
                    System.out.println("[MSG] " + username + " -> " +
                            "Usuario " + msg.getRecipientId() + ": " + msg.getContent());
                }
                else if (obj instanceof VoiceMessage voiceMsg) {
                    db.saveVoiceMessage(voiceMsg.getSenderId(), voiceMsg.getRecipientId(),
                            voiceMsg.getAudioData(), voiceMsg.isGroup());
                    System.out.println("[VOICE] " + username + " envió mensaje de voz");

                    ClientHandler recipient = server.getClient(voiceMsg.getRecipientId());
                    if (recipient != null) {
                        recipient.out.writeObject(voiceMsg);
                        recipient.out.flush();
                    }
                }
                else if (obj instanceof String cmd) {
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
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[HANDLER] Desconexión del cliente: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void handleCall(String cmd) {
        String[] parts = cmd.split(":");
        if (parts.length > 1) {
            int recipientId = Integer.parseInt(parts[1]);
            ClientHandler recipient = server.getClient(recipientId);
            if (recipient != null) {
                try {
                    db.setUserStatus(userId, "in_call");
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
                    db.setUserStatus(userId, "in_call");
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

    private void handleGroupCall(String cmd) {
        String[] parts = cmd.split(":");
        if (parts.length > 1) {
            try {
                int groupId = Integer.parseInt(parts[1]);
                db.setUserStatus(userId, "in_call");

                // Notificar a todos sobre la llamada grupal
                server.broadcastToAll("GROUP_CALL_INCOMING:" + groupId + ":" + username);

                System.out.println("[GROUP_CALL] ✓ #" + username + " inició llamada grupal #" + groupId);
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
                db.setUserStatus(userId, "in_call");

                // Notificar a todos en el grupo
                server.broadcastToAll("GROUP_CALL_MEMBER_JOINED:" + groupId + ":" + username);

                System.out.println("[GROUP_CALL] ✓ #" + username + " se unió a la llamada grupal #" + groupId);
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
                db.setUserStatus(userId, "online");

                server.broadcastToAll("GROUP_CALL_REJECTED_BY:" + db.getGroupnameById(groupId) + ":" + username);

                System.out.println("[GROUP_CALL] ✓ #" + username + " rechazó la llamada grupal de" + db.getGroupnameById(groupId));
            } catch (NumberFormatException e) {
                System.err.println("[GROUP_CALL] ✗ Error: " + e.getMessage());
            }
        }
    }

    public void sendMessage(Message msg) throws IOException {
        out.writeObject(msg);
        out.flush();
    }

    private void cleanup() {
        db.setUserStatus(userId, "offline");
        server.unregisterClient(userId);
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("[HANDLER] Error cerrando socket: " + e.getMessage());
        }
    }

    public ObjectOutputStream getOut() {
        return out;
    }
}
