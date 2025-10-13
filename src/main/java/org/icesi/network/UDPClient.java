package org.icesi.network;

import org.icesi.audio.AudioPlayer;
import org.icesi.audio.AudioRecorder;

import java.io.IOException;
import java.net.*;

public class UDPClient {
    private final DatagramSocket socket;
    private final int udpPort;
    private volatile boolean inCall = false;
    private final InetAddress serverInetAddress;

    public UDPClient(String serverAddress, int udpPort) throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket();
        this.udpPort = udpPort;
        this.serverInetAddress = InetAddress.getByName(serverAddress);
        System.out.println("[UDP] Cliente UDP inicializado en puerto " + socket.getLocalPort());
    }

    public void startCall(int recipientId, int myUserId) {
        this.inCall = true;

        System.out.println("[UDP] Iniciando streaming de audio...");
        System.out.println("[UDP] Micrófono: Puerto local " + socket.getLocalPort());

        new Thread(() -> {
            AudioRecorder recorder = new AudioRecorder();
            AudioPlayer player = new AudioPlayer();

            // Thread para grabar y enviar
            Thread recordThread = new Thread(() -> {
                try {
                    System.out.println("[AUDIO] ▶ Grabando audio...");
                    while (inCall) {
                        byte[] audioData = recorder.recordChunk();
                        if (audioData != null && audioData.length > 0) {
                            sendAudioPacket(audioData);
                        }
                    }
                    recorder.stop();
                    System.out.println("[AUDIO] ⏹ Grabación detenida");
                } catch (Exception e) {
                    System.err.println("[AUDIO] ✗ Error grabando: " + e.getMessage());
                }
            });

            // Thread para recibir y reproducir
            Thread playThread = new Thread(() -> {
                try {
                    System.out.println("[AUDIO] 🎧 Escuchando audio entrante...");
                    byte[] buffer = new byte[4096];
                    while (inCall) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.setSoTimeout(500);
                        try {
                            socket.receive(packet);
                            byte[] audioData = new byte[packet.getLength()];
                            System.arraycopy(packet.getData(), 0, audioData, 0, packet.getLength());
                            player.playChunk(audioData);
                        } catch (SocketTimeoutException e) {
                            // Timeout normal, continúa esperando
                        }
                    }
                    player.stop();
                    System.out.println("[AUDIO] 🔇 Audio detenido");
                } catch (IOException e) {
                    System.err.println("[AUDIO] ✗ Error recibiendo: " + e.getMessage());
                }
            });

            recordThread.setName("AudioRecorder");
            playThread.setName("AudioPlayer");

            recordThread.start();
            playThread.start();
        }).start();
    }

    private void sendAudioPacket(byte[] audioData) {
        try {
            DatagramPacket packet = new DatagramPacket(
                    audioData, audioData.length, serverInetAddress, udpPort
            );
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("[UDP] ✗ Error enviando audio: " + e.getMessage());
        }
    }

    public void endCall() {
        inCall = false;
        System.out.println("[CALL] ☎ Llamada finalizada");
    }

    public boolean isInCall() {
        return inCall;
    }

    public int getLocalPort() {
        return socket.getLocalPort();
    }
}
