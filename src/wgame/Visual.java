package wgame;

/**
 * En Wollok, cualquier objeto es "dibujable" si entiende los mensajes
 * position() e image(). Acá lo formalizamos como interfaz.
 */
public interface Visual {

    Position position();

    /** Nombre de archivo de imagen (relativo a resources/), o null. */
    String image();

    /** Texto a mostrar sobre el visual (equivalente a game.say), o null. */
    default String text() { return null; }

    /** Se llama automáticamente cuando otro visual ocupa la misma celda. */
    default void onCollideWith(Visual other) { /* no-op por defecto */ }

    /** Equivale a "esAtravesable()": si un personaje puede pisar esta celda. */
    default boolean esAtravesable() { return true; }

    /** Equivale a "interactuar()", llamado al presionar la tecla de acción parado sobre este visual. */
    default void interactuar() { /* no-op por defecto */ }

    /** Equivale a "teEncontro()", llamado vía game.onCollideDo(jugador, ...). */
    default void teEncontro() { /* no-op por defecto */ }
}
