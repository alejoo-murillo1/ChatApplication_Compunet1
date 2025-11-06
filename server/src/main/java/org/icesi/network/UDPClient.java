package org.icesi.network;

import org.icesi.audio.AudioPlayer;
import org.icesi.audio.AudioRecorder;

import java.io.IOException;
import java.net.*;

public class UDPClient {
    private DatagramSocket socket;
    private final int udpPort;
    private volatile boolean inCall = false;
    private final InetAddress serverInetAddress;
    private AudioRecorder recorder;
    private AudioPlayer player;
    private Thread recordThread, playThread;

    public UDPClient(String serverAddress, int udpPort) throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket();
        this.udpPort = udpPort;
        this.serverInetAddress = InetAddress.getByName(serverAddress);
        System.out.println("[UDP] Cliente UDP inicializado en puerto " + socket.getLocalPort());
    }

    public void startCall(int recipientId, int myUserId) {
        if (inCall) {
            System.out.println("[UDP] Ya está en una llamada");
            return;
        }

        // ✅ VERIFICAR que el socket esté usable
        if (socket.isClosed()) {
            System.err.println("[UDP] ERROR: Socket UDP cerrado, no se puede iniciar llamada");
            return;
        }

        this.inCall = true;
        System.out.println("[UDP] Iniciando streaming de audio...");

        this.recorder = new AudioRecorder();
        this.player = new AudioPlayer();

        // Threads de audio (código igual que antes)
        recordThread = new Thread(() -> {
            try {
                System.out.println("[AUDIO] ▶ Grabando audio...");
                while (inCall) {
                    byte[] audioData = recorder.recordChunk();
                    if (audioData != null && audioData.length > 0) {
                        sendAudioPacket(audioData);
                    }
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                if (inCall) {
                    System.err.println("[AUDIO] ✗ Error grabando: " + e.getMessage());
                }
            } finally {
                if (recorder != null) {
                    recorder.stop();
                }
            }
        });

        playThread = new Thread(() -> {
            try {
                System.out.println("[AUDIO] 🎧 Escuchando audio entrante...");
                byte[] buffer = new byte[4096];
                while (inCall) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    try {
                        socket.setSoTimeout(1000);
                        socket.receive(packet);
                        byte[] audioData = new byte[packet.getLength()];
                        System.arraycopy(packet.getData(), 0, audioData, 0, packet.getLength());
                        if (audioData.length > 0) {
                            player.playChunk(audioData);
                        }
                    } catch (SocketTimeoutException e) {
                        continue;
                    }
                }
            } catch (IOException e) {
                if (inCall) {
                    System.err.println("[AUDIO] ✗ Error recibiendo: " + e.getMessage());
                }
            } finally {
                if (player != null) {
                    player.cleanup();
                }
            }
        });

        recordThread.setDaemon(true);
        playThread.setDaemon(true);
        recordThread.start();
        playThread.start();
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
        if (!inCall) return;

        inCall = false;

        try {
            if (recordThread != null && recordThread.isAlive()) {
                recordThread.join(1000);
            }
            if (playThread != null && playThread.isAlive()) {
                playThread.join(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("[CALL] ☎ Llamada finalizada (socket listo para reusar)");
    }

    public boolean isInCall() {
        return inCall;
    }

    public int getLocalPort() {
        return socket != null ? socket.getLocalPort() : -1;
    }
}
