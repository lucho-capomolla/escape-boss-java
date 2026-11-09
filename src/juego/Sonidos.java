package juego;

import wgame.Sound;
import wgame.WGame;

/**
 * Puerto de sonidos.wlk (real, subido).
 * (los objetos mockGame/mockSonido del archivo original son mocks para
 * tests de Wollok — no hace falta portarlos, en Java se testea con
 * JUnit/Mockito si hiciera falta).
 */
public class Sonidos {

    private static final Sonidos INSTANCE = new Sonidos();
    public static Sonidos get() { return INSTANCE; }
    private Sonidos() {}

    public Sound sonido(String audio) {
        return WGame.get().sound("Sonidos/" + audio);
    }

    public void reproducir(String audio) {
        sonido(audio).play();
    }

    /**
     * NOTA: igual que en el original, esto crea un Sound NUEVO y lo para
     * al toque -- no detiene un sonido que ya venia sonando desde antes.
     * Es un comportamiento heredado tal cual del wlk original.
     */
    public void parar(String audio) {
        sonido(audio).stop();
    }
}
