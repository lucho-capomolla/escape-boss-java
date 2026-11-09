package wgame;

/**
 * Muchos "object" de Wollok Game son solamente una imagen fija en una
 * posición fija, sin comportamiento propio:
 *   object fondoGanador {
 *     var property position = game.origin()
 *     method image() = "Fondos/Tomorrowland.png"
 *   }
 * En vez de crear una clase Java por cada uno, se puede instanciar esta.
 */
public class StaticImage extends GameObject {

    private final String image;

    public StaticImage(Position position, String image) {
        super(position);
        this.image = image;
    }

    @Override
    public String image() { return image; }
}
