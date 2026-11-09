package juego;

import wgame.GameObject;
import wgame.Log;
import wgame.Sound;
import wgame.WGame;

/**
 * Puerto de menuInicio.wlk (real, subido).
 */
public class MenuInicio extends GameObject {

    private static final MenuInicio INSTANCE = new MenuInicio();
    public static MenuInicio get() { return INSTANCE; }
    private MenuInicio() { super(WGame.get().origin()); }

    private final Sound musicaInicio = Sonidos.get().sonido("HouseOfTheRisingSun.wav");

    @Override
    public String image() { return "Fondos/MenuInicio.png"; }

    public void iniciar() {
        Log.info("MenuInicio", "iniciar(): mostrando pantalla de inicio");
        WGame.get().addVisualIn(this, WGame.get().origin());
        Configuraciones.get().cambiarEstado(EstadoMenuInicio.get());
        WGame.get().schedule(1, musicaInicio::play);
    }

    public void cerrar() {
        for (var visual : java.util.List.copyOf(WGame.get().allVisuals())) {
            WGame.get().removeVisual(visual);
        }
    }

    public void elegirDificultad() {
        Log.info("MenuInicio", "elegirDificultad(): mostrando pantalla de dificultad");
        Configuraciones.get().cambiarEstado(EstadoDificultad.get());
        WGame.get().addVisualIn(Dificultades.PANTALLA, WGame.get().origin());
    }

    public void mostrarInstrucciones() {
        Log.info("MenuInicio", "mostrarInstrucciones(): mostrando instrucciones (8s antes de arrancar)");
        WGame.get().addVisualIn(Instrucciones.PANTALLA, WGame.get().origin());
        WGame.get().schedule(8000, this::continuar);
    }

    public void continuar() {
        Log.info("MenuInicio", "continuar(): cerrando menú, arrancando pantallaJuego");
        cerrar();
        musicaInicio.stop();
        PantallaJuego.get().iniciar();
    }
}
