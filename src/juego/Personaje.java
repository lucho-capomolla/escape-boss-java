package juego;

import wgame.GameObject;
import wgame.Position;
import wgame.WGame;
import juego.Direcciones.Direccion;

/**
 * Puerto de "class Personaje" (jugador.wlk). Base compartida para mover
 * personajes en el tablero (jugador, y probablemente el jefe también).
 *
 * NOTA: en el Wollok original, Personaje no declara "orientacion" pero
 * moverHacia() igual le manda el mensaje personaje.orientacion(direccion)
 * (funciona por duck typing). Acá la subimos a esta clase base para que
 * compile de forma genérica sin duck typing.
 */
public abstract class Personaje extends GameObject {

    protected Direccion orientacion = Direccion.DERECHA;

    protected Personaje(Position posicionInicial) {
        super(posicionInicial);
    }

    public Direccion orientacion() { return orientacion; }
    public void orientacion(Direccion nuevaOrientacion) { this.orientacion = nuevaOrientacion; }

    public void moverse(Direccion direccion) {
        moverHaciaSiSePuede(this, direccion);
    }

    protected void moverHaciaSiSePuede(Personaje personaje, Direccion direccion) {
        if (direccion.puedeIr(personaje)) {
            moverHacia(personaje, direccion);
        } else {
            personaje.error("No es por ahi man");
        }
    }

    protected boolean puedeMoverse(Position posicion) {
        return WGame.get().visualsAt(posicion).stream().allMatch(wgame.Visual::esAtravesable);
    }

    protected void moverHacia(Personaje personaje, Direccion direccion) {
        Position nuevaPosicion = direccion.posicion(personaje.position());
        if (puedeMoverse(nuevaPosicion)) {
            personaje.orientacion(direccion);
            personaje.position(nuevaPosicion);
        }
    }

    /** Equivalente a self.error(mensaje) de Wollok (allá lanza una excepción de negocio). */
    protected void error(String mensaje) {
        wgame.Log.warn(getClass().getSimpleName(), mensaje);
    }
}
