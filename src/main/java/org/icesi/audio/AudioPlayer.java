package org.icesi.audio;

import javax.sound.sampled.*;

public class AudioPlayer {
    private SourceDataLine line;

    public AudioPlayer() {
        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            System.out.println("[AUDIO] Reproductor inicializado correctamente");
        } catch (LineUnavailableException e) {
            System.err.println("[AUDIO] Error inicializando reproducción: " + e.getMessage());
        }
    }

    public void playChunk(byte[] audioData) {
        if (line != null && audioData != null) {
            line.write(audioData, 0, audioData.length);
        }
    }

    public void playFull(byte[] audioData) {
        if (line == null) return;
        try {
            line.write(audioData, 0, audioData.length);
            line.drain();
            System.out.println("[AUDIO] Reproducción completada");
        } catch (Exception e) {
            System.err.println("[AUDIO] Error reproduciendo: " + e.getMessage());
        }
    }

    public void stop() {
        if (line != null) {
            line.stop();
            line.close();
        }
        System.out.println("[AUDIO] Reproductor detenido");
    }
}
