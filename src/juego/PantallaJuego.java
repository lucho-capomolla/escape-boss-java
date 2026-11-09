package juego;

import wgame.GameObject;
import wgame.Log;
import wgame.StaticImage;
import wgame.WGame;

/**
 * Puerto de pantallaJuego.wlk (real, subido).
 */
public class PantallaJuego {

    private static final PantallaJuego INSTANCE = new PantallaJuego();
    public static PantallaJuego get() { return INSTANCE; }
    private PantallaJuego() {}

    private Niveles.Nivel nivelActual = Niveles.NIVEL1;

    public Niveles.Nivel nivelActual() { return nivelActual; }

    public void iniciar() {
        Log.info("PantallaJuego", "iniciar(): cargando " + nombreDe(nivelActual));
        nivelActual.cargarNivel();
    }

    public void avanzarNivel(Niveles.Nivel unNivel) {
        Log.info("PantallaJuego", "avanzarNivel: " + nombreDe(nivelActual) + " -> " + nombreDe(unNivel));
        nivelActual = unNivel;
        iniciar();
    }

    public void terminarJuego() {
        Log.info("PantallaJuego", "terminarJuego(): el jugador ganó");
        Niveles.MUSICA_JUEGO.stop();
        WGame.get().stopAllTicks();
        Sonidos.get().reproducir("Yodelling.wav");
        Sonidos.get().reproducir("TurnDownForWhat.wav");
        WGame.get().clear();
        WGame.get().addVisual(FONDO_GANADOR);
        WGame.get().schedule(10000, this::volverAlMenu);
    }

    /**
     * Reinicia todo el estado de la partida y vuelve a mostrar la pantalla
     * de inicio, SIN cerrar la aplicación. Se llama automáticamente unos
     * segundos después de game over (energía en 0, atrapado por el jefe)
     * o de ganar el juego.
     */
    public void volverAlMenu() {
        Log.info("PantallaJuego", "volverAlMenu(): reiniciando la partida y mostrando el menú de inicio");
        Jugador.get().reiniciar();
        Objetivos.reiniciarTodo();
        Oficina.reiniciarTodo();
        Niveles.NIVEL1.reiniciar();
        Niveles.NIVEL2.reiniciar();
        Niveles.NIVEL3.reiniciar();
        nivelActual = Niveles.NIVEL1;

        WGame.get().clear();
        MenuInicio.get().iniciar();
    }

    private String nombreDe(Niveles.Nivel nivel) {
        if (nivel == Niveles.NIVEL1) return "nivel1";
        if (nivel == Niveles.NIVEL2) return "nivel2";
        if (nivel == Niveles.NIVEL3) return "nivel3";
        return String.valueOf(nivel);
    }

    public static final StaticImage FONDO_ULTIMO_NIVEL = new StaticImage(WGame.get().origin(), "Fondos/FondoUltimoPiso.png");
    public static final StaticImage FONDO_NIVEL_2 = new StaticImage(WGame.get().origin(), "Fondos/FondoPisosAltos.png");
    public static final StaticImage FONDO_PERDIO_ENERGIA = new StaticImage(WGame.get().origin(), "Fondos/GameOverEnergia.png");
    public static final StaticImage FONDO_JEFE1_GANO = new StaticImage(WGame.get().origin(), "Fondos/GameOverJefe1.png");
    public static final StaticImage FONDO_JEFE2_GANO = new StaticImage(WGame.get().origin(), "Fondos/GameOverJefe2.png");
    public static final StaticImage FONDO_GANADOR = new StaticImage(WGame.get().origin(), "Fondos/Tomorrowland.png");
}

/** Puerto de "object energiaJugador" (pantallaJuego.wlk). */
class EnergiaJugador extends GameObject {
    static final EnergiaJugador INSTANCE = new EnergiaJugador();
    private EnergiaJugador() { super(WGame.get().at(16, 1)); }

    @Override
    public String image() {
        int nivel = (int) Math.ceil(Jugador.get().energia() / 10.0);
        return "BarraEnergia/Energia" + nivel + ".png";
    }
}