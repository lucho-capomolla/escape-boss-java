package wgame;

import javax.sound.sampled.*;
import java.io.File;

/**
 * Equivalente a un sonido de Wollok Game:
 *   Wollok: var musica = sonido.sonido("cancion.mp3"); musica.play(); musica.stop()
 *   Java:   Sound musica = WGame.get().sound("Sonidos/cancion.mp3"); musica.play(); musica.stop();
 *
 * IMPORTANTE - soporte de formatos:
 * javax.sound.sampled (API estándar de Java, sin dependencias externas)
 * soporta .wav/.au/.aiff, pero NO soporta .mp3 de forma nativa.
 * Si tu juego usa .mp3, convertilo a .wav o agregá una dependencia como
 * javazoom:jlayer al pom.xml para reproducirlo.
 *
 * Los archivos se buscan en resources/<ruta>, tal como la pasás
 * (p. ej. "Sonidos/Confirmar.wav"), igual que en Wollok.
 */
public class Sound {

    private final String fileName;
    private Clip clip;

    public Sound(String fileName) {
        this.fileName = fileName;
    }

    /** Equivale a sonido.play() */
    public void play() {
        try {
            File file = new File("resources/" + fileName);
            if (!file.exists()) {
                Log.warn("Sound", "No se encontró resources/" + fileName);
                return;
            }
            if (!fileName.toLowerCase().endsWith(".wav")) {
                Log.warn("Sound", "'" + fileName + "' no es .wav. javax.sound.sampled solo " +
                        "soporta wav/au/aiff de forma nativa. Convertilo a .wav o agregá soporte de mp3.");
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
            Log.debug("Sound", "play: " + fileName);
        } catch (Exception e) {
            Log.error("Sound", "No se pudo reproducir " + fileName, e);
        }
    }

    /** Equivale a sonido.stop() */
    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
            Log.debug("Sound", "stop: " + fileName);
        }
    }
}
