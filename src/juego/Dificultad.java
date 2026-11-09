package juego;

/**
 * Puerto de:
 *   class Dificultad{ var nivelDeDificultad; method nivel() = nivelDeDificultad }
 *   object facil inherits Dificultad(nivelDeDificultad=2000){}
 *   object normal inherits Dificultad(nivelDeDificultad=1000){}
 *   object dificil inherits Dificultad(nivelDeDificultad=500){}
 *
 * Wollok permite un "object" que hereda de una clase pasándole parámetros
 * al padre. En Java el equivalente directo es declarar 3 instancias fijas
 * como constantes estáticas de la propia clase.
 */
public class Dificultad {

    private final int nivelDeDificultad;

    public Dificultad(int nivelDeDificultad) {
        this.nivelDeDificultad = nivelDeDificultad;
    }

    public int nivel() { return nivelDeDificultad; }

    public static final Dificultad FACIL = new Dificultad(2000);
    public static final Dificultad NORMAL = new Dificultad(1000);
    public static final Dificultad DIFICIL = new Dificultad(500);
}
