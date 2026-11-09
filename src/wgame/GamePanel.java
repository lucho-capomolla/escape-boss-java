package wgame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Equivale al "board" que Wollok Game sirve por http://localhost:3001,
 * pero acá se dibuja directamente en una ventana Swing.
 *
 * El dibujado siempre razona en coordenadas LÓGICAS (cellSize real, p. ej.
 * 60), y recién al final se aplica un escalado (con letterbox si hace
 * falta) para que el tablero entero entre en el tamaño real de la ventana
 * o pantalla — así cellSize se mantiene fijo en el código del juego, y lo
 * único "responsive" es a qué tamaño de píxeles termina viéndose.
 */
class GamePanel extends JPanel {

    private final WGame game;
    private final Map<String, BufferedImage> imageCache = new HashMap<>();

    GamePanel(WGame game) {
        this.game = game;
        setFocusable(true);
        setBackground(Color.BLACK);
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { Keyboard.get().keyPressed(e.getKeyCode()); }
            @Override public void keyReleased(KeyEvent e) { Keyboard.get().keyReleased(e.getKeyCode()); }
        });
    }

    @Override
    protected void paintComponent(Graphics gRaw) {
        super.paintComponent(gRaw);
        Graphics2D g2root = (Graphics2D) gRaw;

        int cell = game.cellSize();
        int logicalW = game.width() * cell;
        int logicalH = game.height() * cell;
        if (logicalW <= 0 || logicalH <= 0 || getWidth() <= 0 || getHeight() <= 0) return;

        // Escala para que el tablero lógico entre completo en el tamaño real
        // del panel (que puede cambiar en cualquier momento: resize de
        // ventana, o pantalla completa). Se recalcula en cada frame, así que
        // es responsive de verdad, no solo al arrancar.
        double scale = Math.min((double) getWidth() / logicalW, (double) getHeight() / logicalH);
        int scaledW = (int) Math.round(logicalW * scale);
        int scaledH = (int) Math.round(logicalH * scale);
        int offsetX = (getWidth() - scaledW) / 2;
        int offsetY = (getHeight() - scaledH) / 2;

        Graphics2D g = (Graphics2D) g2root.create();
        g.translate(offsetX, offsetY);
        g.scale(scale, scale);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // A partir de acá, todo el dibujado usa coordenadas lógicas (0..logicalW, 0..logicalH).

        // fondo (equivalente a game.boardGround("x.png"))
        String ground = game.boardGroundImage();
        BufferedImage groundImg = ground != null ? loadImage(ground) : null;
        if (groundImg != null) {
            g.drawImage(groundImg, 0, 0, logicalW, logicalH, null);
        } else {
            g.setColor(new Color(235, 245, 235));
            g.fillRect(0, 0, logicalW, logicalH);
        }

        // grilla, oculta por defecto (WGame.showGrid(true) para reactivarla, p. ej. debugueando niveles)
        if (game.showGrid()) {
            g.setColor(new Color(210, 220, 210, 120));
            for (int i = 0; i <= game.width(); i++) g.drawLine(i * cell, 0, i * cell, logicalH);
            for (int j = 0; j <= game.height(); j++) g.drawLine(0, j * cell, logicalW, j * cell);
        }

        List<Visual> visuals = game.allVisuals();
        List<Runnable> globitosPendientes = new ArrayList<>();
        for (Visual v : visuals) {
            try {
                dibujarVisual(g, v, cell, globitosPendientes);
            } catch (Exception e) {
                Log.error("GamePanel", "No se pudo dibujar " + v.getClass().getSimpleName() + "@" + v.position(), e);
            }
        }
        // los globitos de diálogo se dibujan al final, por encima de todo lo demás
        for (Runnable dibujarGlobito : globitosPendientes) dibujarGlobito.run();

        if (game.isPaused()) dibujarPausa(g, logicalW, logicalH);

        g.dispose();
    }

    /** Leyenda de "PAUSA" superpuesta a todo el tablero, mientras game.isPaused(). */
    private void dibujarPausa(Graphics2D g, int logicalW, int logicalH) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, logicalW, logicalH);

        int cx = logicalW / 2;
        int cy = logicalH / 2;

        Font tituloFont = g.getFont().deriveFont(Font.BOLD, 40f);
        Font subtituloFont = g.getFont().deriveFont(Font.PLAIN, 18f);

        g.setFont(tituloFont);
        FontMetrics fmTitulo = g.getFontMetrics();
        String titulo = "PAUSA";
        g.setColor(Color.WHITE);
        g.drawString(titulo, cx - fmTitulo.stringWidth(titulo) / 2, cy - 8);

        g.setFont(subtituloFont);
        FontMetrics fmSub = g.getFontMetrics();
        String subtitulo = "Presioná " + game.pauseKeyLabel() + " para continuar";
        g.setColor(new Color(230, 230, 230));
        g.drawString(subtitulo, cx - fmSub.stringWidth(subtitulo) / 2, cy + 26);
    }

    private void dibujarVisual(Graphics2D g, Visual v, int cell, List<Runnable> globitosPendientes) {
        Position p = v.position();
        int px = p.x() * cell;

        BufferedImage img = v.image() != null ? loadImage(v.image()) : null;
        int py;
        int anchoVisual;
        if (img != null) {
            // Anclaje: el borde INFERIOR de la imagen coincide con el borde
            // inferior de la celda de "position" (la posición es la base del
            // objeto, que crece hacia arriba, como en Wollok). Con una imagen
            // de 1 celda esto da lo mismo que antes; con una imagen más alta
            // (p. ej. un fondo de pantalla completa) queda anclada arriba de
            // la pantalla en vez de "colgar" fuera de la ventana.
            py = (game.height() - p.y()) * cell - img.getHeight();
            g.drawImage(img, px, py, img.getWidth(), img.getHeight(), null);
            anchoVisual = img.getWidth();
        } else {
            py = (game.height() - 1 - p.y()) * cell;
            // placeholder si no hay imagen: un cuadrado con la inicial de la clase.
            // OJO: getSimpleName() da "" para clases anónimas (p. ej. Jefe.JEFE1,
            // varias cartas de Tarjetas), por eso el fallback a "?".
            String simpleName = v.getClass().getSimpleName();
            String etiqueta = simpleName.isEmpty() ? "?" : simpleName.substring(0, 1);
            g.setColor(new Color(90, 150, 220));
            g.fillRoundRect(px + 2, py + 2, cell - 4, cell - 4, 8, 8);
            g.setColor(Color.WHITE);
            g.drawString(etiqueta, px + cell / 2 - 3, py + cell / 2 + 4);
            anchoVisual = cell;
        }

        String texto = v.text() != null ? v.text() : game.speechFor(v);
        if (texto != null) {
            int anchorX = px + anchoVisual / 2;
            int anchorY = py;
            // se guarda para dibujar después de todos los sprites, así ningún
            // globito queda tapado por el objeto de al lado
            globitosPendientes.add(() -> dibujarGlobito(g, texto, anchorX, anchorY));
        }
    }

    /** Dibuja un globito de diálogo estilo historieta: burbuja redondeada + colita, con salto de línea automático. */
    private void dibujarGlobito(Graphics2D g, String texto, int anchorX, int anchorY) {
        Font original = g.getFont();
        Font font = original.deriveFont(Font.PLAIN, 15f);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        int maxAncho = 260;
        int paddingX = 12;
        int paddingY = 8;

        List<String> lineas = ajustarLineas(texto, fm, maxAncho);
        int anchoTexto = lineas.stream().mapToInt(fm::stringWidth).max().orElse(0);
        int lineHeight = fm.getHeight();

        int bubbleW = anchoTexto + paddingX * 2;
        int bubbleH = lineas.size() * lineHeight + paddingY * 2;
        int tailH = 10;

        int bx = anchorX - bubbleW / 2;
        int by = anchorY - bubbleH - tailH - 6;

        g.setColor(new Color(255, 255, 255, 235));
        g.fillRoundRect(bx, by, bubbleW, bubbleH, 16, 16);

        Polygon cola = new Polygon();
        cola.addPoint(anchorX - 8, by + bubbleH - 1);
        cola.addPoint(anchorX + 8, by + bubbleH - 1);
        cola.addPoint(anchorX, by + bubbleH + tailH);
        g.fillPolygon(cola);

        g.setColor(new Color(70, 70, 70));
        g.setStroke(new BasicStroke(1.6f));
        g.drawRoundRect(bx, by, bubbleW, bubbleH, 16, 16);
        g.drawLine(anchorX - 8, by + bubbleH - 1, anchorX, by + bubbleH + tailH);
        g.drawLine(anchorX + 8, by + bubbleH - 1, anchorX, by + bubbleH + tailH);

        g.setColor(new Color(30, 30, 30));
        int ty = by + paddingY + fm.getAscent();
        for (String linea : lineas) {
            int lw = fm.stringWidth(linea);
            g.drawString(linea, anchorX - lw / 2, ty);
            ty += lineHeight;
        }

        g.setFont(original);
    }

    /** Parte el texto en líneas de a lo sumo maxAncho píxeles, cortando por palabra. */
    private List<String> ajustarLineas(String texto, FontMetrics fm, int maxAncho) {
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            String candidata = actual.isEmpty() ? palabra : actual + " " + palabra;
            if (fm.stringWidth(candidata) > maxAncho && !actual.isEmpty()) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(candidata);
            }
        }
        if (!actual.isEmpty()) lineas.add(actual.toString());
        return lineas.isEmpty() ? List.of("") : lineas;
    }

    private BufferedImage loadImage(String name) {
        return imageCache.computeIfAbsent(name, n -> {
            try {
                File f = new File("resources/" + n);
                if (f.exists()) {
                    BufferedImage img = ImageIO.read(f);
                    Log.debug("GamePanel", "imagen cargada: " + n + " (" + img.getWidth() + "x" + img.getHeight() + ")");
                    return img;
                }
                var stream = getClass().getResourceAsStream("/" + n);
                if (stream != null) {
                    BufferedImage img = ImageIO.read(stream);
                    Log.debug("GamePanel", "imagen cargada (classpath): " + n);
                    return img;
                }
                Log.warn("GamePanel", "No se encontró resources/" + n + " — se dibuja un placeholder");
                return null;
            } catch (IOException e) {
                Log.error("GamePanel", "Error leyendo la imagen " + n, e);
                return null;
            }
        });
    }
}