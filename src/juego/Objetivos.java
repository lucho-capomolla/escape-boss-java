package juego;

import wgame.GameObject;
import wgame.Position;
import wgame.StaticImage;
import wgame.WGame;

/**
 * Puerto de objetivos.wlk. Las clases Tarea/Pedido/ObjetoEstado van
 * anidadas acá porque solo tiene sentido usarlas calificadas
 * (Objetivos.Tarea, Objetivos.Pedido), igual que sus instancias.
 */
public class Objetivos {

    // ---------- Nivel 1: tareas ----------
    public static class Tarea extends GameObject {
        private String estado = "Not";
        private String color;

        public Tarea(Position position, String color) {
            super(position);
            this.color = color;
        }

        Tarea() { this(WGame.get().origin(), ""); }

        public String color() { return color; }
        public void color(String color) { this.color = color; }
        protected String estado() { return estado; }

        @Override public String image() { return "Objetivos/Tarea" + color + estado + ".png"; }

        public void seEntrego() { estado = "Ok"; }
        public void reEntregar() { estado = "Not"; }
    }

    public static final Tarea TAREA_AZUL = new Tarea(WGame.get().at(16, 3), "Azul");
    public static final Tarea TAREA_ROJO = new Tarea(WGame.get().at(16, 7), "Rojo");
    public static final Tarea TAREA_VERDE = new Tarea(WGame.get().at(16, 5), "Verde");
    public static final Tarea SIN_TAREA = new Tarea();

    // ---------- Nivel 2: pedidos ----------
    public static class Pedido extends Tarea {
        private String tipo;

        public Pedido(Position position, String color, String tipo) {
            super(position, color);
            this.tipo = tipo;
        }

        Pedido(String tipo) { this(WGame.get().origin(), "", tipo); }
        Pedido() { this(WGame.get().origin(), "", null); }

        public String tipo() { return tipo; }
        public void tipo(String tipo) { this.tipo = tipo; }

        @Override public String image() { return "Objetivos/Pedido" + color() + tipo + estado() + ".png"; }
    }

    public static final Pedido HELADO = new Pedido("Helado");
    public static final Pedido CAFE = new Pedido("Cafe");
    public static final Pedido[] PRODUCTOS = { HELADO, CAFE };

    public static final Pedido PEDIDO_AZUL = new Pedido(WGame.get().at(16, 3), "Azul", null);
    public static final Pedido PEDIDO_ROJO = new Pedido(WGame.get().at(16, 7), "Rojo", null);
    public static final Pedido PEDIDO_VERDE = new Pedido(WGame.get().at(16, 5), "Verde", null);
    public static final Pedido VACIO = new Pedido();

    /** Equivale a productos.anyOne() */
    public static Pedido productoAlAzar() {
        return PRODUCTOS[(int) (Math.random() * PRODUCTOS.length)];
    }

    // ---------- Nivel 3: objetos personales ----------
    public static final StaticImage CONTENIDO_MOCHILA =
            new StaticImage(WGame.get().at(16, 3), "Objetivos/ContenidoMochila.png");

    public static class ObjetoEstado extends GameObject {
        private final String nombre;
        private String estado = "Not";

        public ObjetoEstado(String nombre) {
            super(WGame.get().at(16, 3));
            this.nombre = nombre;
        }

        @Override public String image() { return "Objetivos/" + nombre + estado + ".png"; }

        public void encontrado() { estado = "Ok"; }
        public void perdido() { estado = "Not"; }
    }

    public static final ObjetoEstado AURICULARES_ESTADO = new ObjetoEstado("Auriculares");
    public static final ObjetoEstado LLAVES_ESTADO = new ObjetoEstado("Llaves");
    public static final ObjetoEstado LAPTOP_ESTADO = new ObjetoEstado("Laptop");
    public static final ObjetoEstado CREDENCIAL_ESTADO = new ObjetoEstado("Credencial");
    public static final ObjetoEstado CELULAR_ESTADO = new ObjetoEstado("Celular");
    public static final ObjetoEstado BILLETERA_ESTADO = new ObjetoEstado("Billetera");

    /**
     * Deja tareas/pedidos/objetos como recién arrancado el juego (todos
     * "Not"/perdidos). Se llama al volver al menú de inicio después de
     * game over o victoria, para que una partida nueva no arranque con
     * cosas marcadas como ya completadas de la partida anterior.
     */
    public static void reiniciarTodo() {
        for (Tarea t : java.util.List.of(TAREA_AZUL, TAREA_ROJO, TAREA_VERDE, PEDIDO_AZUL, PEDIDO_ROJO, PEDIDO_VERDE)) {
            t.reEntregar();
        }
        for (ObjetoEstado oe : java.util.List.of(
                AURICULARES_ESTADO, LLAVES_ESTADO, LAPTOP_ESTADO, CREDENCIAL_ESTADO, CELULAR_ESTADO, BILLETERA_ESTADO)) {
            oe.perdido();
        }
    }
}