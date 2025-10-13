package org.icesi.audio;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

public class AudioRecorder {
    private TargetDataLine line;
    private boolean recording = false;

    public AudioRecorder() {
        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            recording = true;
            System.out.println("[AUDIO] Grabador inicializado correctamente");
        } catch (LineUnavailableException e) {
            System.err.println("[AUDIO] Error inicializando grabación: " + e.getMessage());
        }
    }

    public byte[] recordChunk() {
        if (!recording || line == null) return null;
        byte[] buffer = new byte[4096];
        int numBytesRead = line.read(buffer, 0, buffer.length);
        if (numBytesRead > 0) {
            byte[] chunk = new byte[numBytesRead];
            System.arraycopy(buffer, 0, chunk, 0, numBytesRead);
            return chunk;
        }
        return null;
    }

    public byte[] recordFull(int durationMs) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        long startTime = System.currentTimeMillis();

        try {
            while (System.currentTimeMillis() - startTime < durationMs) {
                int numBytesRead = line.read(buffer, 0, buffer.length);
                if (numBytesRead > 0) {
                    baos.write(buffer, 0, numBytesRead);
                }
            }
        } catch (Exception e) {
            System.err.println("[AUDIO] Error grabando: " + e.getMessage());
        }

        return baos.toByteArray();
    }

    public void stop() {
        recording = false;
        if (line != null) {
            line.stop();
            line.close();
        }
        System.out.println("[AUDIO] Grabador detenido");
    }
}
