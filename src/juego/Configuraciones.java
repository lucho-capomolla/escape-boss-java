package juego;

import wgame.Keyboard;
import wgame.Log;
import wgame.WGame;

/**
 * Puerto de configuraciones.wlk (real, subido).
 */
public class Configuraciones {

    public static final int ANCHO = 19;
    public static final int ALTURA = 13;

    private static final Configuraciones INSTANCE = new Configuraciones();
    public static Configuraciones get() { return INSTANCE; }
    private Configuraciones() {}

    private Estado estadoActual = EstadoMenuInicio.get();

    public void configurarTeclas() {
        Keyboard.get().up().onPressDo(() -> estadoActual.alPresionarUp());
        Keyboard.get().down().onPressDo(() -> estadoActual.alPresionarDown());
        Keyboard.get().right().onPressDo(() -> estadoActual.alPresionarRight());
        Keyboard.get().left().onPressDo(() -> estadoActual.alPresionarLeft());
        Keyboard.get().e().onPressDo(() -> estadoActual.alPresionarE());
        Keyboard.get().enter().onPressDo(() -> estadoActual.alPresionarEnter());
        Keyboard.get().num1().onPressDo(() -> estadoActual.alPresionar1());
        Keyboard.get().num2().onPressDo(() -> estadoActual.alPresionar2());
        Keyboard.get().num3().onPressDo(() -> estadoActual.alPresionar3());
        Log.info("Configuraciones", "Teclas configuradas");
    }

    public void cambiarEstado(Estado nuevoEstado) {
        Log.info("Configuraciones", "cambiarEstado: " + describir(estadoActual) + " -> " + describir(nuevoEstado));
        this.estadoActual = nuevoEstado;
    }

    private String describir(Estado e) { return e == null ? "null" : e.getClass().getSimpleName(); }

    /**
     * Equivale a configuraciones.configurarColisiones().
     * "algo.teEncontro()" en Wollok es duck typing puro; en Java funciona
     * directo porque teEncontro() ya es parte de la interfaz Visual
     * (con no-op por defecto).
     */
    public void configurarColisiones() {
        WGame.get().onCollideDo(Jugador.get(), wgame.Visual::teEncontro);
        Log.info("Configuraciones", "Colisiones del jugador configuradas");
    }
}
