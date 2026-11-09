package juego;

import wgame.Log;
import wgame.WGame;

/**
 * Puerto de theGame.wpgm (el "program" de Wollok).
 *
 *   program juego {
 *     game.title("Escape Boss")
 *     game.height(altura)
 *     game.width(ancho)
 *     game.cellSize(60)
 *     configuraciones.configurarTeclas()
 *     menuInicio.iniciar()
 *     game.boardGround("Fondos/Fondo.png")
 *     game.start()
 *   }
 */
public class TheGame {
    public static void main(String[] args) {
        // Todo lo que pase durante la partida queda también en este archivo,
        // además de la consola, para poder revisarlo después de cerrar el juego.
        Log.toFile("game.log");
        // Cambiar a Log.Level.DEBUG acá (o pasar -Dlog.level=DEBUG, ver más abajo)
        // para ver también el detalle de cada movimiento, tecla, colisión, etc.
        String nivelLog = System.getProperty("log.level", "INFO");
        try {
            Log.level(Log.Level.valueOf(nivelLog.toUpperCase()));
        } catch (IllegalArgumentException e) {
            Log.warn("TheGame", "log.level='" + nivelLog + "' inválido, usando INFO");
        }

        Log.info("TheGame", "Arrancando Escape Boss...");

        WGame game = WGame.get();
        game.title("Escape Boss");
        game.height(Configuraciones.ALTURA);
        game.width(Configuraciones.ANCHO);
        game.cellSize(60);
        // Por defecto arranca en ventana, ajustada a la pantalla disponible,
        // y F11 alterna a pantalla completa en cualquier momento. Si preferís
        // que arranque directo en pantalla completa, descomentá esta línea:
        game.fullscreen(true);

        Configuraciones.get().configurarTeclas();
        MenuInicio.get().iniciar();

        game.boardGround("Fondos/Fondo.png");

        game.start();
    }
}
