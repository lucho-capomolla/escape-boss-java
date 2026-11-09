package wgame;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Equivalente al objeto "game" de Wollok Game.
 *
 *   Wollok                          Java
 *   -------------------------------------------------------------
 *   game.title("Mi juego")          WGame.get().title("Mi juego")
 *   game.width(10) / height(8)      WGame.get().width(10) / height(8)
 *   game.cellSize(32)               WGame.get().cellSize(32)
 *   game.addVisual(pepita)          WGame.get().addVisual(pepita)
 *   game.addVisualIn(v, pos)        WGame.get().addVisualIn(v, pos)
 *   game.removeVisual(pepita)       WGame.get().removeVisual(pepita)
 *   game.allVisuals()               WGame.get().allVisuals()
 *   posicion.allElements()          WGame.get().visualsAt(posicion)
 *   game.start() / stop()           WGame.get().start() / stop()
 *   game.onTick(ms,"n",{...})       WGame.get().onTick(ms, "n", () -> {...})
 *   game.schedule(ms, {...})        WGame.get().schedule(ms, () -> {...})
 *   game.onCollideDo(v, {o=>...})   WGame.get().onCollideDo(v, o -> {...})
 *   game.say(pepita,"Hola")         WGame.get().say(pepita, "Hola")
 *   game.sound("Sonidos/x.wav")     WGame.get().sound("Sonidos/x.wav")
 *   game.boardGround("x.png")       WGame.get().boardGround("x.png")
 *   game.center() / origin()        WGame.get().center() / origin()
 *   game.clear()                    WGame.get().clear()
 */
public class WGame {

    private static final WGame INSTANCE = new WGame();
    public static WGame get() { return INSTANCE; }

    private String title = "Wollok Game (portado a Java)";
    private int width = 10;
    private int height = 8;
    private int cellSize = 40;
    private String boardGround;
    private boolean showGrid = false;
    private boolean fullscreen = false;
    private boolean paused = false;
    private int pauseKeyCode = java.awt.event.KeyEvent.VK_P;

    private final List<Visual> visuals = new ArrayList<>();
    private final Map<String, Timer> tickTimers = new HashMap<>();
    private final Map<Visual, Consumer<Visual>> collideListeners = new HashMap<>();
    private final Map<Visual, SpeechBubble> speechBubbles = new HashMap<>();
    private static final long SPEECH_DURATION_MS = 2500;

    private record SpeechBubble(String text, long expiresAt) {}

    private JFrame frame;
    private GamePanel panel;
    private Timer renderTimer;

    private WGame() {}

    // --- configuración ---
    public WGame title(String t) { this.title = t; return this; }
    public WGame width(int w) { this.width = w; return this; }
    public WGame height(int h) { this.height = h; return this; }
    public WGame cellSize(int c) { this.cellSize = c; return this; }
    public int width() { return width; }
    public int height() { return height; }
    public int cellSize() { return cellSize; }

    public void boardGround(String image) { this.boardGround = image; }
    String boardGroundImage() { return boardGround; }

    /** Muestra u oculta las líneas de la grilla (por defecto: ocultas). Solo afecta el dibujado, no la lógica. */
    public void showGrid(boolean visible) { this.showGrid = visible; }
    boolean showGrid() { return showGrid; }

    /**
     * Si se llama con true ANTES de start(), el juego arranca directamente
     * en pantalla completa (sin bordes de ventana). En cualquier momento
     * (arrancado en el modo que sea) F11 alterna entre pantalla completa y
     * ventana. La resolución de las celdas sigue siendo siempre cellSize()
     * en términos lógicos — lo que cambia es a qué tamaño de píxeles reales
     * se termina escalando todo el tablero (ver GamePanel).
     */
    public WGame fullscreen(boolean f) { this.fullscreen = f; return this; }

    /** Cambiar la tecla de pausa/reanudar (por defecto P). Llamar antes de start(). */
    public WGame pauseKey(int keyCode) { this.pauseKeyCode = keyCode; return this; }

    public boolean isPaused() { return paused; }

    /**
     * Pausa o reanuda el juego: mientras está pausado, se frenan todos los
     * onTick (persecución del jefe, aparición de tarjetas, etc.), se deja
     * de detectar colisiones, y las teclas del "keyboard" que ve Wollok se
     * ignoran — salvo la propia tecla de pausa, que sirve para reanudar.
     * Se dibuja una leyenda de "PAUSA" superpuesta (ver GamePanel).
     */
    public void togglePause() {
        paused = !paused;
        if (paused) {
            Log.info("WGame", "Juego en pausa");
            for (Timer t : tickTimers.values()) t.stop();
        } else {
            Log.info("WGame", "Juego reanudado");
            for (Timer t : tickTimers.values()) t.start();
        }
    }

    /** Usado por GamePanel para el texto de la leyenda de pausa. */
    String pauseKeyLabel() { return java.awt.event.KeyEvent.getKeyText(pauseKeyCode); }

    // --- posiciones ---
    public Position at(int x, int y) { return new Position(x, y); }
    public Position origin() { return new Position(0, 0); }
    public Position center() { return new Position(width / 2, height / 2); }

    public boolean isInsideBoard(Position p) {
        return p.x() >= 0 && p.x() < width && p.y() >= 0 && p.y() < height;
    }

    public Position clampToBoard(Position p) {
        int x = Math.max(0, Math.min(p.x(), width - 1));
        int y = Math.max(0, Math.min(p.y(), height - 1));
        return new Position(x, y);
    }

    // --- visuals ---
    public void addVisual(Visual v) {
        visuals.add(v);
        Log.debug("WGame", "addVisual: " + describe(v) + " (total visuals=" + visuals.size() + ")");
    }

    public void removeVisual(Visual v) {
        boolean estaba = visuals.remove(v);
        if (estaba) {
            Log.debug("WGame", "removeVisual: " + describe(v) + " (total visuals=" + visuals.size() + ")");
        } else {
            Log.debug("WGame", "removeVisual: " + describe(v) + " ignorado, no estaba en el tablero");
        }
    }

    public boolean hasVisual(Visual v) { return visuals.contains(v); }
    public List<Visual> allVisuals() { return visuals; }

    /** Equivale a game.addVisualIn(visual, posicion) */
    public void addVisualIn(Visual v, Position p) {
        if (v instanceof GameObject) ((GameObject) v).moveTo(p);
        addVisual(v);
    }

    /** Equivale a posicion.allElements(): todos los visuals ubicados en esa celda */
    public List<Visual> visualsAt(Position p) {
        List<Visual> result = new ArrayList<>();
        for (Visual v : visuals) if (v.position().equals(p)) result.add(v);
        return result;
    }

    /** Equivale a game.clear() */
    public void clear() {
        Log.info("WGame", "clear(): se sacan " + visuals.size() + " visuals del tablero");
        visuals.clear();
    }

    /**
     * Equivale a game.showAttributes(visual): en el editor web de Wollok
     * Game muestra un panel de inspección de atributos al pasar el mouse.
     * No tiene equivalente visual en esta versión Swing — no-op.
     */
    public void showAttributes(Visual v) {
        Log.debug("WGame", "showAttributes(" + describe(v) + ") — no-op en esta versión (sin editor visual)");
    }

    // --- sonido ---
    public Sound sound(String path) { return new Sound(path); }

    // --- mensajes flotantes (game.say) ---
    /** Equivale a game.say(visual, mensaje): el globito desaparece solo a los 2.5s. */
    public void say(Visual v, String message) {
        speechBubbles.put(v, new SpeechBubble(message, System.currentTimeMillis() + SPEECH_DURATION_MS));
    }

    String speechFor(Visual v) {
        SpeechBubble b = speechBubbles.get(v);
        if (b == null) return null;
        if (System.currentTimeMillis() > b.expiresAt()) {
            speechBubbles.remove(v);
            return null;
        }
        return b.text();
    }

    /** Equivale a game.onTick(ms, "nombre", accion): timer propio por cada onTick. */
    public void onTick(int millis, String name, Runnable action) {
        Timer old = tickTimers.remove(name);
        if (old != null) old.stop();
        Timer t = new Timer(millis, e -> {
            try {
                action.run();
            } catch (Exception ex) {
                Log.error("WGame", "Excepción en onTick '" + name + "'", ex);
            }
        });
        t.start();
        tickTimers.put(name, t);
        Log.debug("WGame", "onTick registrado: '" + name + "' cada " + millis + "ms");
    }

    public void onTick(int millis, Runnable action) {
        onTick(millis, "tick-" + tickTimers.size(), action);
    }

    public void removeTickEvent(String name) {
        Timer t = tickTimers.remove(name);
        if (t != null) {
            t.stop();
            Log.debug("WGame", "onTick removido: '" + name + "'");
        } else {
            Log.debug("WGame", "removeTickEvent('" + name + "') ignorado, no existía");
        }
    }

    /**
     * Frena TODOS los onTick activos de una — usar cuando el juego termina
     * de verdad (game over, victoria), para que no sigan corriendo en
     * segundo plano cosas como "aparecer una tarjeta" o "perseguir al
     * jugador" mientras se muestra la pantalla final.
     */
    public void stopAllTicks() {
        if (tickTimers.isEmpty()) return;
        Log.info("WGame", "stopAllTicks(): deteniendo " + tickTimers.size() + " onTick activos (" + tickTimers.keySet() + ")");
        for (Timer t : tickTimers.values()) t.stop();
        tickTimers.clear();
    }

    /** Equivale a game.schedule(ms, accion): ejecuta una sola vez, luego de "delayMs". */
    public void schedule(long delayMs, Runnable action) {
        Log.debug("WGame", "schedule: en " + delayMs + "ms");
        Timer t = new Timer((int) Math.max(delayMs, 1), e -> {
            try {
                action.run();
            } catch (Exception ex) {
                Log.error("WGame", "Excepción en schedule (" + delayMs + "ms)", ex);
            }
        });
        t.setRepeats(false);
        t.start();
    }

    /** Equivale a game.onCollideDo(visual, { otro => ... }) */
    public void onCollideDo(Visual target, Consumer<Visual> action) {
        collideListeners.put(target, action);
        Log.debug("WGame", "onCollideDo registrado para " + describe(target));
    }

    public void stop() {
        Log.info("WGame", "stop(): deteniendo el juego");
        if (renderTimer != null) renderTimer.stop();
        stopAllTicks();
        if (frame != null) frame.dispose();
    }

    /** Equivale a game.start() */
    public void start() {
        Log.info("WGame", "start(): título='" + title + "', " + width + "x" + height +
                ", cellSize=" + cellSize + ", boardGround=" + boardGround + ", visuals=" + visuals.size());

        frame = new JFrame(title);
        panel = new GamePanel(this);
        frame.getContentPane().add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Tamaño lógico del tablero, siempre a cellSize "real" (60 en Escape Boss).
        // La ventana en sí se ajusta a la pantalla disponible; GamePanel escala el
        // dibujado para que entre entero sin deformarse (ver dibujado con letterbox).
        java.awt.Rectangle screenBounds = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int logicalW = width * cellSize;
        int logicalH = height * cellSize;

        if (fullscreen) {
            enterFullscreen();
        } else {
            double scale = Math.min(1.0, Math.min(
                    (double) screenBounds.width / logicalW,
                    (double) screenBounds.height / logicalH));
            panel.setPreferredSize(new java.awt.Dimension(
                    (int) Math.round(logicalW * scale), (int) Math.round(logicalH * scale)));
            frame.setResizable(true);
            frame.pack();
            frame.setLocationRelativeTo(null);
        }

        // F11 alterna pantalla completa / ventana, y la tecla de pausa
        // alterna pausa, en cualquier momento. Son teclas de la ventana en
        // sí, separadas del "keyboard" que ve Wollok.
        panel.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_F11) toggleFullscreen();
                else if (e.getKeyCode() == pauseKeyCode) togglePause();
            }
        });

        // enterFullscreen() ya deja la ventana visible y enfocada; en modo
        // ventana normal hace falta hacerlo acá.
        if (!fullscreen) {
            frame.setVisible(true);
            panel.requestFocusInWindow();
        }

        // loop principal: repinta y detecta colisiones. Los onTick corren en sus propios timers.
        renderTimer = new Timer(33, e -> {
            try {
                if (!paused) detectCollisions();
                panel.repaint();
            } catch (Exception ex) {
                Log.error("WGame", "Excepción en el loop principal (render/colisiones)", ex);
            }
        });
        renderTimer.start();
    }

    private void enterFullscreen() {
        java.awt.GraphicsDevice device = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        frame.dispose();
        frame.setUndecorated(true);
        frame.setResizable(false);
        if (device.isFullScreenSupported()) {
            device.setFullScreenWindow(frame);
            Log.info("WGame", "Pantalla completa activada (modo exclusivo)");
        } else {
            // fallback: maximizar como ventana sin bordes, si el SO no soporta el modo exclusivo
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setBounds(java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
            Log.info("WGame", "Pantalla completa activada (fallback maximizado, el SO no soporta modo exclusivo)");
        }
        fullscreen = true;
        frame.setVisible(true);
        panel.requestFocusInWindow();
    }

    private void exitFullscreen() {
        java.awt.GraphicsDevice device = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (device.getFullScreenWindow() == frame) device.setFullScreenWindow(null);
        frame.dispose();
        frame.setUndecorated(false);
        frame.setResizable(true);

        java.awt.Rectangle screenBounds = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int logicalW = width * cellSize;
        int logicalH = height * cellSize;
        double scale = Math.min(1.0, Math.min((double) screenBounds.width / logicalW, (double) screenBounds.height / logicalH));
        panel.setPreferredSize(new java.awt.Dimension((int) Math.round(logicalW * scale), (int) Math.round(logicalH * scale)));
        frame.pack();
        frame.setLocationRelativeTo(null);

        fullscreen = false;
        frame.setVisible(true);
        panel.requestFocusInWindow();
        Log.info("WGame", "Pantalla completa desactivada, vuelve a ventana");
    }

    /** Alterna entre pantalla completa y ventana. Atado a F11 automáticamente en start(). */
    public void toggleFullscreen() {
        if (frame == null) return;
        if (fullscreen) exitFullscreen(); else enterFullscreen();
    }

    private void detectCollisions() {
        for (int i = 0; i < visuals.size(); i++) {
            for (int j = i + 1; j < visuals.size(); j++) {
                Visual a = visuals.get(i);
                Visual b = visuals.get(j);
                if (a.position().equals(b.position())) {
                    a.onCollideWith(b);
                    b.onCollideWith(a);
                    notifyCollideListener(a, b);
                    notifyCollideListener(b, a);
                }
            }
        }
    }

    private void notifyCollideListener(Visual target, Visual other) {
        Consumer<Visual> action = collideListeners.get(target);
        if (action != null) {
            Log.debug("WGame", describe(target) + " colisionó con " + describe(other));
            try {
                action.accept(other);
            } catch (Exception ex) {
                Log.error("WGame", "Excepción en onCollideDo de " + describe(target), ex);
            }
        }
    }

    private static String describe(Visual v) {
        if (v == null) return "null";
        return v.getClass().getSimpleName() + "@" + v.position();
    }
}