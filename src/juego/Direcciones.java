package juego;

import wgame.Position;
import wgame.WGame;

/**
 * Puerto de direcciones.wlk. Cada "object" de Wollok (arriba/abajo/derecha/
 * izquierda) se modela como una constante del enum anidado Direccion.
 */
public class Direcciones {

    public enum Direccion {

        ARRIBA {
            @Override public String nombre() { return "Espalda"; }
            @Override public Position posicion(Position p) { return p.up(1); }
            @Override public boolean puedeIr(Personaje personaje) {
                return personaje.position().y() < (Configuraciones.ALTURA - 3) && libre(personaje, this);
            }
            @Override public void orientar(Personaje personaje) { /* no-op, como en Wollok */ }
        },
        ABAJO {
            @Override public String nombre() { return "DeFrente"; }
            @Override public Position posicion(Position p) { return p.down(1); }
            @Override public boolean puedeIr(Personaje personaje) {
                return personaje.position().y() > 1 && libre(personaje, this);
            }
            @Override public void orientar(Personaje personaje) { /* no-op, como en Wollok */ }
        },
        DERECHA {
            @Override public String nombre() { return "Derecha"; }
            @Override public Position posicion(Position p) { return p.right(1); }
            @Override public boolean puedeIr(Personaje personaje) {
                return personaje.position().x() < (Configuraciones.ANCHO - 5) && libre(personaje, this);
            }
            @Override public void orientar(Personaje personaje) { personaje.orientacion(this); }
        },
        IZQUIERDA {
            @Override public String nombre() { return "Izquierda"; }
            @Override public Position posicion(Position p) { return p.left(1); }
            @Override public boolean puedeIr(Personaje personaje) {
                return personaje.position().x() > 3 && libre(personaje, this);
            }
            @Override public void orientar(Personaje personaje) { personaje.orientacion(this); }
        };

        public abstract String nombre();
        public abstract Position posicion(Position p);
        public abstract boolean puedeIr(Personaje personaje);

        /**
         * NOTA: en el Wollok original este método existe pero no se llama
         * desde ningún lado en los archivos que me pasaste (moverHacia()
         * de Personaje setea la orientación directamente). Lo dejo
         * portado por fidelidad.
         */
        public abstract void orientar(Personaje personaje);

        private static boolean libre(Personaje personaje, Direccion direccion) {
            Position destino = direccion.posicion(personaje.position());
            return WGame.get().visualsAt(destino).stream().allMatch(wgame.Visual::esAtravesable);
        }
    }
}
