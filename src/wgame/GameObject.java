package wgame;

/**
 * Clase base opcional (no obligatoria) que da comportamiento por defecto
 * a un Visual con posición mutable, tal como suele hacerse en Wollok:
 *
 *   class Pepita {
 *     var position = game.at(0,0)
 *     method position() = position
 *   }
 */
public abstract class GameObject implements Visual {

    protected Position position;

    protected GameObject(Position initial) {
        // por si el objeto se crea directamente con una posición inválida
        this.position = WGame.get().clampToBoard(initial);
    }

    @Override
    public Position position() { return position; }

    /**
     * Mueve el objeto a una posición, sin permitir que se salga del tablero
     * (se recorta al borde más cercano si se pasa de los límites).
     */
    public void moveTo(Position p) {
        this.position = WGame.get().clampToBoard(p);
    }

    /** Alias de moveTo, para calzar con el setter que genera "var property position" en Wollok. */
    public void position(Position p) { moveTo(p); }

    public void right(int n) { moveTo(position.right(n)); }
    public void left(int n)  { moveTo(position.left(n)); }
    public void up(int n)    { moveTo(position.up(n)); }
    public void down(int n)  { moveTo(position.down(n)); }
}
