package wgame;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger centralizado y minimalista para todo el juego: sin dependencias
 * externas, con niveles, timestamp, una "categoría" (para saber de qué
 * parte del juego viene cada línea) y, opcionalmente, un archivo de log
 * además de la consola — útil para trazabilidad post-mortem ("¿qué pasó
 * en la partida que se rompió?").
 *
 * Uso típico:
 *   Log.toFile("game.log");     // opcional, en TheGame.main() al arrancar
 *   Log.info("WGame", "Juego iniciado: 19x13, cellSize=60");
 *   Log.warn("Sound", "No se encontró resources/Sonidos/Confirmar.wav");
 *   Log.error("Sound", "No se pudo reproducir x.wav", excepcion);
 */
public final class Log {

    public enum Level { DEBUG, INFO, WARN, ERROR }

    private static volatile Level minLevel = Level.INFO;
    private static volatile PrintWriter fileWriter;
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private Log() {}

    /** Nivel mínimo a mostrar (por defecto INFO: no muestra DEBUG). */
    public static void level(Level l) { minLevel = l; }

    /**
     * A partir de acá, además de por consola, cada línea también se
     * escribe (agregando, sin pisar corridas anteriores) en este archivo.
     * Pensado para llamarse una sola vez al arrancar el juego.
     */
    public static synchronized void toFile(String path) {
        try {
            PrintWriter w = new PrintWriter(Files.newBufferedWriter(
                    Path.of(path), StandardOpenOption.CREATE, StandardOpenOption.APPEND));
            fileWriter = w;
            info("Log", "=== Nueva sesión de juego: log en " + path + " ===");
        } catch (IOException e) {
            System.err.println("[Log] No se pudo abrir el archivo de log '" + path + "': " + e.getMessage());
        }
    }

    public static void debug(String categoria, String mensaje) { log(Level.DEBUG, categoria, mensaje); }
    public static void info(String categoria, String mensaje)  { log(Level.INFO, categoria, mensaje); }
    public static void warn(String categoria, String mensaje)  { log(Level.WARN, categoria, mensaje); }
    public static void error(String categoria, String mensaje) { log(Level.ERROR, categoria, mensaje); }

    public static void error(String categoria, String mensaje, Throwable causa) {
        log(Level.ERROR, categoria, mensaje + " -- " + causa.getClass().getSimpleName() + ": " + causa.getMessage());
    }

    private static synchronized void log(Level level, String categoria, String mensaje) {
        if (level.ordinal() < minLevel.ordinal()) return;
        String linea = "[" + LocalDateTime.now().format(TS) + "] [" + pad(level) + "] [" + categoria + "] " + mensaje;
        (level == Level.ERROR || level == Level.WARN ? System.err : System.out).println(linea);
        if (fileWriter != null) {
            fileWriter.println(linea);
            fileWriter.flush();
        }
    }

    private static String pad(Level level) {
        return switch (level) {
            case DEBUG -> "DEBUG";
            case INFO  -> "INFO ";
            case WARN  -> "WARN ";
            case ERROR -> "ERROR";
        };
    }
}
