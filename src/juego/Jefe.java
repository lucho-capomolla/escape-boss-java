package juego;

import wgame.Log;
import wgame.Position;
import wgame.WGame;
import juego.Direcciones.Direccion;

import java.util.List;

/**
 * Puerto de jefe.wlk (real, subido).
 *
 * NOTA: el original comenta (deja "muerto") un override de moverHacia()
 * que movía a un objeto "pikachu" a la posición anterior del jefe (un
 * efecto visual de "estela" descartado por el autor). No lo porto porque
 * está comentado en la fuente real, no forma parte del comportamiento
 * activo del juego.
 */
public abstract class Jefe extends Personaje {

    private Dificultad dificultad;
    private final String nombre;

    protected Jefe(String nombre, Position posicionInicial, Direccion orientacionInicial) {
        super(posicionInicial);
        this.nombre = nombre;
        orientacion(orientacionInicial);
    }

    public void elegirDificultad(Dificultad nuevaDificultad) {
        this.dificultad = nuevaDificultad;
        Log.info(nombre, "elegirDificultad: nivel=" + nuevaDificultad.nivel() + "ms entre movimientos");
    }

    protected Dificultad dificultad() { return dificultad; }

    /** Cada jefe (jefe1/jefe2) pisa esto con su propia posición inicial. */
    public void volverAlInicio() { /* no-op en la base */ }

    @Override
    public boolean esAtravesable() { return true; }

    @Override
    public void teEncontro() {
        if (position().equals(Jugador.get().position())) {
            Log.info(nombre, "Game over: atrapó al jugador en " + position());
            Niveles.MUSICA_JUEGO.stop();
            WGame.get().stopAllTicks();
            WGame.get().clear();
            mostrarFondo();
            Sonidos.get().reproducir("Lost.wav");
            Sonidos.get().reproducir("MadWorld.wav");
            WGame.get().schedule(12000, () -> PantallaJuego.get().volverAlMenu());
        }
    }

    /** Cada jefe agrega su propio fondo de "game over" (fondoJefe1Gano / fondoJefe2Gano). */
    protected void mostrarFondo() { /* no-op en la base */ }

    public boolean puedeEsconderse() { return false; }

    public void esconderse() {
        Log.debug(nombre, "esconderse(): deja de perseguir por 4s");
        dejarDePerseguir();
        WGame.get().schedule(4000, this::moverse);
    }

    /** Cada jefe arranca su propio onTick de persecución (con su nombre de evento). */
    public void moverse() { /* no-op en la base */ }

    protected void dejarDePerseguir() { /* no-op en la base */ }

    public void perseguir() {
        if (Jugador.get().estaEscondido()) {
            irASalida();
        } else {
            moverHaciaJugador();
        }
    }

    private void moverHaciaJugador() {
        Direccion masConveniente = masCercanaA(direccionesAtravesables(), Jugador.get().position());
        if (masConveniente != null) moverHaciaSiSePuede(this, masConveniente);
    }

    private void irASalida() {
        Direccion aSalida = masCercanaA(direccionesAtravesables(), Oficina.SALIDA_POS);
        if (aSalida != null) moverHaciaSiSePuede(this, aSalida);
    }

    /** Equivale a direcciones.min({direccion => direccion.posicion(position).distance(objetivo)}) */
    private Direccion masCercanaA(List<Direccion> direcciones, Position objetivo) {
        Direccion mejor = null;
        double mejorDistancia = Double.MAX_VALUE;
        for (Direccion d : direcciones) {
            double distancia = d.posicion(position()).distance(objetivo);
            if (distancia < mejorDistancia) {
                mejorDistancia = distancia;
                mejor = d;
            }
        }
        return mejor; // null si direcciones está vacía (jefe encerrado) — caso defensivo, no está en el original
    }

    private List<Direccion> direccionesAtravesables() {
        return List.of(Direccion.IZQUIERDA, Direccion.ARRIBA, Direccion.ABAJO, Direccion.DERECHA)
                .stream().filter(d -> d.puedeIr(this)).toList();
    }

    // ========== jefe1 ==========
    public static final Jefe JEFE1 = new Jefe("jefe1", WGame.get().at(14, 1), Direccion.IZQUIERDA) {
        @Override public String image() { return "Jefe/Jefe1" + orientacion().nombre() + ".png"; }

        @Override public void volverAlInicio() { position(WGame.get().at(14, 1)); }

        @Override public void moverse() {
            WGame.get().onTick(dificultad().nivel(), "Perseguir1", this::perseguir);
        }

        @Override protected void dejarDePerseguir() { WGame.get().removeTickEvent("Perseguir1"); }

        @Override protected void mostrarFondo() { WGame.get().addVisual(PantallaJuego.FONDO_JEFE1_GANO); }
    };

    // ========== jefe2 ==========
    public static final Jefe JEFE2 = new Jefe("jefe2", WGame.get().at(14, 10), Direccion.IZQUIERDA) {
        @Override public String image() { return "Jefe/Jefe2" + orientacion().nombre() + ".png"; }

        @Override public void volverAlInicio() { position(WGame.get().at(14, 10)); }

        @Override public void moverse() {
            WGame.get().onTick(dificultad().nivel(), "Perseguir2", this::perseguir);
        }

        @Override protected void dejarDePerseguir() { WGame.get().removeTickEvent("Perseguir2"); }

        @Override protected void mostrarFondo() { WGame.get().addVisual(PantallaJuego.FONDO_JEFE2_GANO); }
    };
}