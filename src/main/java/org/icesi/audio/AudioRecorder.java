package org.icesi.audio;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

public class AudioRecorder {
    private TargetDataLine line;
    private boolean recording = false;

    public AudioRecorder() {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("[AUDIO] ✗ Formato no soportado");
                return;
            }

            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            recording = true;

            System.out.println("[AUDIO] ✓ AudioRecorder iniciado");
            System.out.println("[AUDIO] Micrófono disponible: " + line.toString());

        } catch (LineUnavailableException e) {
            System.err.println("[AUDIO] ✗ Micrófono no disponible: " + e.getMessage());
            recording = false;
        } catch (Exception e) {
            System.err.println("[AUDIO] ✗ Error inicializando grabador: " + e.getMessage());
            recording = false;
        }
    }

    public byte[] recordChunk() {
        if (!recording || line == null) {
            return null;
        }

        try {
            byte[] buffer = new byte[4096];
            int numBytesRead = line.read(buffer, 0, buffer.length);

            if (numBytesRead > 0) {
                byte[] chunk = new byte[numBytesRead];
                System.arraycopy(buffer, 0, chunk, 0, numBytesRead);
                return chunk;
            }
        } catch (Exception e) {
            System.err.println("[AUDIO] ✗ Error leyendo micrófono: " + e.getMessage());
        }

        return null;
    }

    public byte[] recordFull(int durationMs) {
        if (!recording || line == null) {
            System.err.println("[AUDIO] ✗ Grabador no inicializado");
            return new byte[0];
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        long startTime = System.currentTimeMillis();
        int totalBytesRead = 0;

        try {
            System.out.println("[AUDIO] ▶ Grabando...");

            while (System.currentTimeMillis() - startTime < durationMs) {
                try {
                    int numBytesRead = line.read(buffer, 0, buffer.length);

                    if (numBytesRead > 0) {
                        baos.write(buffer, 0, numBytesRead);
                        totalBytesRead += numBytesRead;
                    }
                } catch (Exception e) {
                    System.err.println("[AUDIO] ✗ Error durante grabación: " + e.getMessage());
                    break;
                }
            }

            System.out.println("[AUDIO] ✓ Grabación completada (" + totalBytesRead + " bytes)");

        } catch (Exception e) {
            System.err.println("[AUDIO] ✗ Error grabando: " + e.getMessage());
        }

        return baos.toByteArray();
    }

    public void stop() {
        recording = false;

        if (line != null) {
            try {
                line.stop();
                line.close();
                System.out.println("[AUDIO] ✓ Grabador detenido");
            } catch (Exception e) {
                System.err.println("[AUDIO] ✗ Error deteniendo grabador: " + e.getMessage());
            }
        }
    }

    public boolean isRecording() {
        return recording && line != null;
    }
}
