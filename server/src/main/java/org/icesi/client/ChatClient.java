package org.icesi.client;

import org.icesi.audio.AudioPlayer;
import org.icesi.db.DatabaseManager;
import org.icesi.model.Message;
import org.icesi.model.VoiceMessage;
import org.icesi.network.UDPClient;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class ChatClient {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final DatabaseManager db;
    private int userId;
    private String username;
    private UDPClient udpClient;
    private final Queue<VoiceMessage> voiceMessageQueue = new ConcurrentLinkedQueue<>();
    private final Map<Integer, List<VoiceMessage>> groupVoiceMessages = new ConcurrentHashMap<>();

    public ChatClient(String serverAddress, int port) throws IOException {
        this.db = new DatabaseManager();
        this.socket = new Socket(serverAddress, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
        try {
            this.udpClient = new UDPClient(serverAddress, 5001);
        } catch (Exception e) {
            System.err.println("[NETWORK] Error inicializando cliente UDP: " + e.getMessage());
        }
    }

    public void login(String username) {
        try {
            this.username = username;
            out.writeObject(username);
            out.flush();

            // ENVIAR puerto UDP al servidor (NUEVO)
            if (udpClient != null) {
                out.writeInt(udpClient.getLocalPort());
                out.flush();
            } else {
                out.writeInt(-1); // Indicar que no hay UDP
                out.flush();
            }

            String response = (String) in.readObject();
            if (response.startsWith("LOGIN_SUCCESS")) {
                this.userId = Integer.parseInt(response.split(":")[1]);
                System.out.println("[AUTH] ✓ Conectado como " + username + " (ID: " + userId + ")");
                updateUserStatus("online");
                startMessageReceiver();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[AUTH] ✗ Error en login: " + e.getMessage());
        }
    }

    private void startMessageReceiver() {
        Thread receiverThread = new Thread(() -> {
            try {
                while (true) {
                    Object obj = in.readObject();
                    if (obj instanceof Message msg) {
                        if (msg.isGroup()) {
                            var groupName = db.getGroupnameById(msg.getRecipientId());
                            var sender = db.getUsernameById(msg.getSenderId());
                            System.out.println("\n[GROUP_MSG] Grupo " + groupName + " de "+ sender+ ": " + msg.getContent());
                        } else {
                            var sender = db.getUsernameById(msg.getSenderId());
                            System.out.println("\n[P2P_MSG] Mensaje de " + sender + ": " + msg.getContent());
                        }
                    } else if (obj instanceof VoiceMessage voiceMsg) {
                        if (voiceMsg.isGroup()) {
                            groupVoiceMessages.computeIfAbsent(voiceMsg.getRecipientId(), k -> new CopyOnWriteArrayList<>())
                                    .add(voiceMsg);
                            System.out.println("\n[GROUP_AUDIO] Grupo " + db.getGroupnameById(voiceMsg.getRecipientId()) +
                                    ": Audio de " + voiceMsg.getSenderUsername());
                        } else {
                            voiceMessageQueue.add(voiceMsg);
                            System.out.println("\n[P2P_AUDIO] Audio de " + voiceMsg.getSenderUsername());
                        }
                    } else if (obj instanceof String cmd) {
                        if (cmd.startsWith("INCOMING_CALL:")) {
                            handleIncomingCall(cmd);
                        } else if (cmd.equals("CALL_ACCEPTED")) {
                            System.out.println("\n[CALL_EVENT] ✓ Llamada aceptada. Conectando audio...");
                        } else if (cmd.equals("CALL_REJECTED")) {
                            System.out.println("\n[CALL_EVENT] ✗ Llamada rechazada");
                        } else if (cmd.startsWith("USER_STATUS_UPDATE:")) {
                            // Podrías implementar actualización de UI aquí
                            System.out.println("[STATUS] " + cmd);
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("[NETWORK] Desconectado del servidor");
            }
        });
        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    private void handleIncomingCall(String cmd) {
        String[] parts = cmd.split(":");
        int callerId = Integer.parseInt(parts[1]);
        String callerName = parts.length > 2 ? parts[2] : "Usuario " + callerId;

        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║    ☎ LLAMADA ENTRANTE          ║");
        System.out.println("║    De: " + callerName);
        System.out.println("║    (ID: " + callerId + ")");
        System.out.println("╚════════════════════════════════╝");
    }

    // ===== GESTIÓN DE ESTADOS =====
    private void updateUserStatus(String status) {
        db.setUserStatus(userId, status);
        try {
            out.writeObject("STATUS_UPDATE:" + status);
            out.flush();
        } catch (IOException e) {
            System.err.println("[STATUS] Error actualizando estado: " + e.getMessage());
        }
    }

    // ===== AUDIO P2P =====
    public void playLastVoiceMessage() {
        if (voiceMessageQueue.isEmpty()) {
            System.out.println("[AUDIO] No hay mensajes de voz P2P para reproducir");
            return;
        }

        VoiceMessage voiceMsg = voiceMessageQueue.poll();
        System.out.println("\n[AUDIO] Reproduciendo audio de " + voiceMsg.getSenderUsername() + "...");

        AudioPlayer player = new AudioPlayer();
        player.playFull(voiceMsg.getAudioData());
        player.cleanup();

        System.out.println("[AUDIO] ✓ Reproducción completada");
    }

    public void listVoiceMessages() {
        if (voiceMessageQueue.isEmpty()) {
            System.out.println("[AUDIO] No hay mensajes de voz P2P pendientes");
            return;
        }

        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║   MENSAJES DE VOZ P2P          ║");
        System.out.println("╚════════════════════════════════╝");

        int i = 1;
        for (VoiceMessage msg : voiceMessageQueue) {
            System.out.println(i + ". De " + msg.getSenderUsername() + " - " + msg.getTimestamp());
            i++;
        }
    }

    // ===== AUDIO GRUPO =====
    public void playLastGroupVoiceMessage(int groupId) {
        List<VoiceMessage> messages = groupVoiceMessages.get(groupId);
        if (messages == null || messages.isEmpty()) {
            System.out.println("[GROUP_AUDIO] No hay audios del grupo #" + groupId);
            return;
        }

        VoiceMessage voiceMsg = messages.removeFirst();
        System.out.println("\n[GROUP_AUDIO] Reproduciendo audio de " + voiceMsg.getSenderUsername() + "...");

        AudioPlayer player = new AudioPlayer();
        player.playFull(voiceMsg.getAudioData());
        player.cleanup();

        System.out.println("[GROUP_AUDIO] ✓ Reproducción completada");
    }

    public void listGroupVoiceMessages(int groupId) {
        List<VoiceMessage> messages = groupVoiceMessages.get(groupId);
        if (messages == null || messages.isEmpty()) {
            System.out.println("[GROUP_AUDIO] No hay audios pendientes del grupo #" + groupId);
            return;
        }

        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║   AUDIOS DEL GRUPO #" + groupId);
        System.out.println("╚════════════════════════════════╝");

        int i = 1;
        for (VoiceMessage msg : messages) {
            System.out.println(i + ". De " + msg.getSenderUsername() + " - " + msg.getTimestamp().getHour());
            i++;
        }
    }

    // ===== MENSAJES P2P =====
    public void sendMessage(int recipientId, String content) {
        try {
            Message msg = new Message(userId, recipientId, content, false);
            out.writeObject(msg);
            out.flush();
            System.out.println("[P2P_MSG] ✓ Mensaje enviado a ID " + recipientId);
        } catch (IOException e) {
            System.err.println("[ERROR] Enviando mensaje: " + e.getMessage());
        }
    }

    public void sendVoiceMessage(int recipientId, byte[] audioData) {
        try {
            VoiceMessage voiceMsg = new VoiceMessage(userId, recipientId, audioData, false, username);
            out.writeObject(voiceMsg);
            out.flush();
            db.savePrivateVoiceMessage(userId, recipientId, audioData);
            System.out.println("[P2P_AUDIO] ✓ Audio enviado a: " + getUsernameById(recipientId));
        } catch (IOException e) {
            System.err.println("[ERROR] Enviando audio: " + e.getMessage());
        }
    }

    // ===== LLAMADAS P2P =====
    public void makeCall(int recipientId) {
        try {
            String callCmd = "CALL:" + recipientId;
            out.writeObject(callCmd);
            out.flush();
            updateUserStatus("in_call");

            System.out.println("\n[CALL_EVENT] ☎ Llamando a: " + getUsernameById(recipientId) + "...");

            if (udpClient != null) {
                udpClient.startCall(recipientId, userId);
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Iniciando llamada: " + e.getMessage());
        }
    }

    public void endCall() {
        updateUserStatus("online");
        if (udpClient != null && udpClient.isInCall()) {
            udpClient.endCall();
        }
        try {
            out.writeObject("END_CALL");
            out.flush();
        } catch (IOException e) {
            System.err.println("[ERROR] Finalizando llamada: " + e.getMessage());
        }
        System.out.println("[CALL_EVENT] ☎ Llamada finalizada");
    }

    public void acceptCall(int callerId) {
        try {
            String acceptCmd = "CALL_ACCEPTED:" + callerId;
            out.writeObject(acceptCmd);
            out.flush();
            updateUserStatus("in_call");

            if (udpClient != null) {
                udpClient.startCall(callerId, userId);
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Aceptando llamada: " + e.getMessage());
        }
    }

    public void rejectCall(int callerId) {
        try {
            String rejectCmd = "CALL_REJECTED:" + callerId;
            out.writeObject(rejectCmd);
            out.flush();
            System.out.println("[CALL_EVENT] Llamada rechazada");
        } catch (IOException e) {
            System.err.println("[ERROR] Rechazando llamada: " + e.getMessage());
        }
    }

    // ===== GRUPOS =====
    public void listGroups() {
        db.listAllGroups();
    }

    public void createGroup(String groupName) {
        int groupId = db.createGroup(groupName, userId);
        if (groupId != -1) {
            System.out.println("[GROUP] ✓ Grupo '" + groupName + "' creado (ID: " + groupId + ")");
        } else {
            System.out.println("[GROUP] ✗ No se pudo crear el grupo");
        }
    }

    public void addMemberToGroup(int groupId, String username) {
        int memberId = db.getUserIdByUsername(username);
        if (memberId == -1) {
            System.out.println("[ERROR] Usuario '" + username + "' no encontrado");
            return;
        }
        db.addGroupMember(groupId, memberId);
        System.out.println("[GROUP] ✓ " + username + " agregado al grupo #" + groupId);
    }

    public void sendGroupMessage(int groupId, String content) {
        try {
            Message msg = new Message(userId, groupId, content, true);
            out.writeObject(msg);
            out.flush();
            System.out.println("[GROUP_MSG] ✓ Mensaje enviado al grupo #" + groupId);
        } catch (IOException e) {
            System.err.println("[ERROR] Enviando mensaje al grupo: " + e.getMessage());
        }
    }

    public void sendGroupVoiceMessage(int groupId, byte[] audioData) {
        try {
            VoiceMessage voiceMsg = new VoiceMessage(userId, groupId, audioData, true, username);
            out.writeObject(voiceMsg);
            out.flush();
            db.saveGroupVoiceMessage(userId, groupId, audioData);
            System.out.println("[GROUP_AUDIO] ✓ Audio enviado al grupo #" + groupId);
        } catch (IOException e) {
            System.err.println("[ERROR] Enviando audio al grupo: " + e.getMessage());
        }
    }

    public void makeGroupCall(int groupId) {
        try {
            String callCmd = "GROUP_CALL:" + groupId;
            out.writeObject(callCmd);
            out.flush();
            updateUserStatus("in_call");

            System.out.println("[GROUP_CALL] ☎ Iniciando llamada grupal #" + groupId + "...");

            if (udpClient != null) {
                udpClient.startCall(groupId, userId);
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Iniciando llamada grupal: " + e.getMessage());
        }
    }

    public void acceptGroupCall(int groupId) {
        try {
            String acceptCmd = "GROUP_CALL_ACCEPTED:" + groupId;
            out.writeObject(acceptCmd);
            out.flush();
            updateUserStatus("in_call");

            if (udpClient != null) {
                udpClient.startCall(groupId, userId);
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Aceptando llamada grupal: " + e.getMessage());
        }
    }

    public void rejectGroupCall(int groupId) {
        try {
            String rejectCmd = "GROUP_CALL_REJECTED:" + groupId;
            out.writeObject(rejectCmd);
            out.flush();
            updateUserStatus("online");
            System.out.println("[GROUP_CALL] Llamada grupal rechazada");
        } catch (IOException e) {
            System.err.println("[ERROR] Rechazando llamada grupal: " + e.getMessage());
        }
    }

    public String getUsernameById(int userId) {
        return db.getUsernameById(userId);
    }

    public String getGroupnameById(int groupId) {
        return db.getGroupnameById(groupId);
    }

    // ===== UTILS =====
    public void viewHistory() {
        db.displayHistory(userId);
    }

    public void listUsers() {
        db.listAllUsers(userId);
    }

    public void viewAudioHistory() {
        db.displayAudioHistory(userId);
    }

    public String getUsername() {
        return username;
    }

    public void disconnect() {
        updateUserStatus("offline");
        try {
            if (udpClient != null) {
                udpClient.endCall();
            }
            socket.close();
            System.out.println("[CONNECTION] Desconectado del servidor");
        } catch (IOException e) {
            System.err.println("[ERROR] Desconectando: " + e.getMessage());
        }
    }
}
