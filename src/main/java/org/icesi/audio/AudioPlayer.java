package org.icesi.audio;

import javax.sound.sampled.*;

public class AudioPlayer {
    private SourceDataLine line;

    public AudioPlayer() {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("[AUDIO] ✗ Formato no soportado para reproducción");
                return;
            }

            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            System.out.println("[AUDIO] ✓ AudioPlayer iniciado");
            System.out.println("[AUDIO] Altavoz disponible: " + line.toString());

        } catch (LineUnavailableException e) {
            System.err.println("[AUDIO] ✗ Altavoz no disponible: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[AUDIO] ✗ Error inicializando reproductor: " + e.getMessage());
        }
    }

    public void playChunk(byte[] audioData) {
        if (line == null || audioData == null) {
            return;
        }

        try {
            if (audioData.length > 0) {
                int written = line.write(audioData, 0, audioData.length);
                if (written != audioData.length) {
                    System.out.println("[AUDIO] ⚠ Solo se escribieron " + written + " de " + audioData.length + " bytes");
                }
            }
        } catch (Exception e) {
            System.err.println("[AUDIO] ✗ Error reproduciendo chunk: " + e.getMessage());
        }
    }

    public void playFull(byte[] audioData) {
        if (line == null || audioData == null) {
            System.err.println("[AUDIO] ✗ AudioPlayer no inicializado");
            return;
        }

        try {
            if (audioData.length == 0) {
                System.err.println("[AUDIO] ✗ No hay datos de audio para reproducir");
                return;
            }

            System.out.println("[AUDIO] ▶ Reproduciendo (" + audioData.length + " bytes)...");

            // Escribir todo el audio
            int bytesWritten = 0;
            int chunkSize = 4096;

            while (bytesWritten < audioData.length) {
                int remainingBytes = audioData.length - bytesWritten;
                int toWrite = Math.min(chunkSize, remainingBytes);

                line.write(audioData, bytesWritten, toWrite);
                bytesWritten += toWrite;
            }

            // Drain para asegurar que todo se reproduce
            line.drain();
            System.out.println("[AUDIO] ✓ Reproducción completada");

        } catch (Exception e) {
            System.err.println("[AUDIO] ✗ Error reproduciendo: " + e.getMessage());
        }
    }

    public void stop() {
        if (line != null) {
            try {
                line.stop();
                line.close();
                System.out.println("[AUDIO] ✓ Reproductor detenido");
            } catch (Exception e) {
                System.err.println("[AUDIO] ✗ Error deteniendo reproductor: " + e.getMessage());
            }
        }
    }

    public boolean isPlaying() {
        return line != null && line.isRunning();
    }
}
