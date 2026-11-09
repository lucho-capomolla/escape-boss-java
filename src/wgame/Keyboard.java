package wgame;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Equivale al objeto "keyboard" de Wollok Game:
 *   Wollok: keyboard.up().onPressDo({ pepita.subir() })
 *   Java:   Keyboard.get().up().onPressDo(() -> pepita.subir())
 */
public class Keyboard {

    private static final Keyboard INSTANCE = new Keyboard();
    public static Keyboard get() { return INSTANCE; }

    private final Set<Integer> pressed = new HashSet<>();
    private final Map<Integer, Runnable> onPress = new HashMap<>();

    public class Key {
        private final int code;
        Key(int code) { this.code = code; }

        /** Equivalente a keyboard.X().onPressDo(bloque) */
        public void onPressDo(Runnable action) { onPress.put(code, action); }

        /** Equivalente a keyboard.X().pressed() */
        public boolean pressed() { return pressed.contains(code); }
    }

    public Key up()    { return new Key(KeyEvent.VK_UP); }
    public Key down()  { return new Key(KeyEvent.VK_DOWN); }
    public Key left()  { return new Key(KeyEvent.VK_LEFT); }
    public Key right() { return new Key(KeyEvent.VK_RIGHT); }
    public Key space() { return new Key(KeyEvent.VK_SPACE); }
    public Key enter() { return new Key(KeyEvent.VK_ENTER); }
    public Key e()     { return new Key(KeyEvent.VK_E); }
    public Key num1()  { return new Key(KeyEvent.VK_1); }
    public Key num2()  { return new Key(KeyEvent.VK_2); }
    public Key num3()  { return new Key(KeyEvent.VK_3); }
    public Key letter(char c) { return new Key(KeyEvent.getExtendedKeyCodeForChar(c)); }

    // --- usado internamente por GamePanel, no forma parte de la API pública ---
    void keyPressed(int code) {
        pressed.add(code);
        if (WGame.get().isPaused()) {
            Log.debug("Keyboard", "tecla ignorada (juego en pausa): " + KeyEvent.getKeyText(code));
            return;
        }
        Runnable action = onPress.get(code);
        if (action != null) {
            Log.debug("Keyboard", "tecla presionada: " + KeyEvent.getKeyText(code));
            try {
                action.run();
            } catch (Exception ex) {
                Log.error("Keyboard", "Excepción manejando la tecla " + KeyEvent.getKeyText(code), ex);
            }
        }
    }

    void keyReleased(int code) {
        pressed.remove(code);
    }
}