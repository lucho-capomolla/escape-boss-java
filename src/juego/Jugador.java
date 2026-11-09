package juego;

import wgame.Log;
import wgame.WGame;
import juego.Direcciones.Direccion;

import java.util.HashSet;
import java.util.Set;

/**
 * Puerto de jugador.wlk (real, subido). Extiende Personaje.
 */
public class Jugador extends Personaje {

    private static final Jugador INSTANCE = new Jugador();
    public static Jugador get() { return INSTANCE; }

    private int energia = 100;
    private final Set<Objetivos.Tarea> tareasRealizadas = new HashSet<>();
    private final Set<Oficina.Companieri> companierisAyudados = new HashSet<>();
    private final Set<Oficina.ObjetoPersonal> objetosEnMochila = new HashSet<>();
    private Objetivos.Tarea tareaEnMano = Objetivos.SIN_TAREA;
    private Objetivos.Pedido objetoEnMano = Objetivos.VACIO;
    private boolean tieneMochila = false;

    private Jugador() {
        super(WGame.get().at(3, 1));
    }

    public int energia() { return energia; }

    @Override
    public String image() {
        if (estaEscondido()) return "Personaje/Escondido.png";
        if (tieneMochila) return "Personaje/Jugador" + orientacion().nombre() + "Mochila.png";
        if (noTieneNingunaTarea()) {
            String tipo = objetoEnMano.tipo() != null ? objetoEnMano.tipo() : "";
            return "Personaje/Jugador" + orientacion().nombre() + tipo + ".png";
        }
        return "Personaje/Jugador" + orientacion().nombre() + tareaEnMano.color() + ".png";
    }

    // --- movimiento ---
    public void moverAlInicio() {
        orientacion(Direccion.DERECHA);
        position(WGame.get().at(3, 1));
    }

    @Override
    public void moverse(Direccion direccion) {
        super.moverse(direccion);
        disminuirEnergia(2);
    }

    public void accionar() {
        for (var elemento : WGame.get().visualsAt(position())) {
            elemento.interactuar();
        }
    }

    @Override
    public void interactuar() { /* el jugador mismo no hace nada al ser "interactuado" */ }

    // --- energía ---
    public void aumentarEnergia(int cantidad) {
        energia = Math.min(energia + cantidad, 100);
        Log.debug("Jugador", "aumentarEnergia(+" + cantidad + ") -> energia=" + energia);
    }

    public void disminuirEnergia(int cantidad) {
        energia = Math.max(energia - cantidad, 0);
        Log.debug("Jugador", "disminuirEnergia(-" + cantidad + ") -> energia=" + energia);
        if (energia == 0) {
            Log.info("Jugador", "Game over: energía en 0");
            Niveles.MUSICA_JUEGO.stop();
            WGame.get().stopAllTicks();
            WGame.get().clear();
            WGame.get().addVisual(PantallaJuego.FONDO_PERDIO_ENERGIA);
            Sonidos.get().reproducir("Lost.wav");
            Sonidos.get().reproducir("MadWorld.wav");
            WGame.get().schedule(12000, () -> PantallaJuego.get().volverAlMenu());
        }
    }

    // --- objetos personales / mochila ---
    public void agarrarMochila() { tieneMochila = true; }
    public boolean tieneMochila() { return tieneMochila; }
    public Set<Oficina.ObjetoPersonal> objetosEnMochila() { return objetosEnMochila; }

    public void guardarObjeto(Oficina.ObjetoPersonal unObjeto) {
        if (puedoGuardar()) {
            objetosEnMochila.add(unObjeto);
            WGame.get().say(this, "Preciooosooo");
        } else {
            WGame.get().say(this, "¿En donde queres que lo guarde?");
            Sonidos.get().reproducir("Huh.wav");
        }
    }

    public void quitarObjeto(Oficina.ObjetoPersonal unObjeto) {
        objetosEnMochila.remove(unObjeto);
        WGame.get().addVisual(unObjeto);
        unObjeto.estado().perdido();
    }

    public boolean puedoGuardar() { return tieneMochila; }

    // --- esconderse ---
    public boolean estaEscondido() {
        return position().equals(Oficina.PLANTA1.position()) || position().equals(Oficina.PLANTA2.position());
    }

    public boolean puedeEsconderse() { return true; }

    @Override
    public boolean esAtravesable() { return !estaEscondido(); }

    // --- tareas ---
    public void agregarTarea(Objetivos.Tarea tarea) { tareaEnMano = tarea; }
    public Objetivos.Tarea tareaEnMano() { return tareaEnMano; }

    public void terminarTarea(Objetivos.Tarea tarea) {
        tareaEnMano = Objetivos.SIN_TAREA;
        tareasRealizadas.add(tarea);
    }

    public void quitarTarea(Objetivos.Tarea tarea) {
        tareasRealizadas.remove(tarea);
        tarea.reEntregar();
    }

    public boolean entregoTarea(Objetivos.Tarea tarea) { return tareasRealizadas.contains(tarea); }
    public boolean noTieneNingunaTarea() { return tareaEnMano == Objetivos.SIN_TAREA; }

    // --- objetos / pedidos ---
    public void agarrarObjeto(Objetivos.Pedido objeto) { objetoEnMano = objeto; }

    public void entregarObjeto(Oficina.Companieri companieri) {
        objetoEnMano = Objetivos.VACIO;
        companierisAyudados.add(companieri);
    }

    public void quitarPedido(Oficina.Companieri companieri) {
        companierisAyudados.remove(companieri);
        companieri.perdioObjeto();
    }

    public Objetivos.Pedido objetoEnMano() { return objetoEnMano; }
    public boolean noTieneNingunObjeto() { return objetoEnMano == Objetivos.VACIO; }

    // --- requeridos por niveles.wlk / tarjetas.wlk ---
    public Set<Objetivos.Tarea> tareasRealizadas() { return tareasRealizadas; }
    public Set<Oficina.Companieri> companierisAyudados() { return companierisAyudados; }

    /** Setter de la property "energia" (usado por la tarjeta restaurarEnergia). */
    public void energia(int valor) { this.energia = valor; }

    /**
     * Deja al jugador como recién arrancada la partida: energía llena, sin
     * tareas/objetos/compañeris pendientes, sin mochila, en la posición
     * inicial. Se llama al volver al menú de inicio después de game over
     * o victoria.
     */
    public void reiniciar() {
        energia = 100;
        tareasRealizadas.clear();
        companierisAyudados.clear();
        objetosEnMochila.clear();
        tareaEnMano = Objetivos.SIN_TAREA;
        objetoEnMano = Objetivos.VACIO;
        tieneMochila = false;
        moverAlInicio();
    }
}