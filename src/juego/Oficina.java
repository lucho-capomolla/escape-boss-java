package juego;

import wgame.GameObject;
import wgame.Position;
import wgame.StaticImage;
import wgame.Visual;
import wgame.WGame;

/**
 * Puerto de oficina.wlk. Todas las clases del archivo original van acá
 * como clases anidadas (package/estáticas), con sus instancias como
 * constantes de OficinaObjetos, para no explotar en 10 archivos sueltos.
 */
public class Oficina {

    // ========== Consumibles ==========
    public static class Consumible extends GameObject {
        private final int energiaAportada;
        private final String nombre;

        public Consumible(Position position, String nombre, int energiaAportada) {
            super(position);
            this.nombre = nombre;
            this.energiaAportada = energiaAportada;
        }

        @Override public String image() { return "Oficina/" + nombre + ".png"; }
        @Override public void teEncontro() { /* no-op: solo se usa desde interactuar() */ }

        @Override
        public void interactuar() {
            Jugador.get().aumentarEnergia(energiaAportada);
            WGame.get().say(Jugador.get(), "Aumento la energia en " + energiaAportada);
            WGame.get().removeVisual(this);
            Sonidos.get().reproducir("Burp.wav");
        }
    }

    public static final Consumible CAFE_CON_LECHE = new Consumible(WGame.get().at(3, 5), "Cafe", 25);
    public static final Consumible CHOCOLATE = new Consumible(WGame.get().at(9, 2), "Chocolate", 20);
    public static final Consumible HAMBURGUESA = new Consumible(WGame.get().at(13, 6), "Hamburguesa", 40);
    public static final Consumible PIZZA = new Consumible(WGame.get().at(6, 8), "Pizza", 35);
    public static final Consumible BEBIDA = new Consumible(WGame.get().at(14, 9), "Bebida", 15);
    public static final Consumible ROSQUILLA = new Consumible(WGame.get().at(11, 3), "Rosquilla", 10);

    // ========== Mochila (singleton) ==========
    public static class Mochila extends GameObject {
        private Mochila() { super(WGame.get().center()); }

        @Override public String image() { return "Oficina/Mochila.png"; }

        @Override
        public void interactuar() {
            Jugador.get().agarrarMochila();
            Sonidos.get().reproducir("Cierre.wav");
            WGame.get().removeVisual(this);
        }
    }

    public static final Mochila MOCHILA = new Mochila();

    // ========== Objetos personales ==========
    public static class ObjetoPersonal extends GameObject {
        private final String nombre;
        private final Objetivos.ObjetoEstado estado;
        private boolean guardado = false;

        public ObjetoPersonal(Position position, String nombre, Objetivos.ObjetoEstado estado) {
            super(position);
            this.nombre = nombre;
            this.estado = estado;
        }

        public Objetivos.ObjetoEstado estado() { return estado; }
        public boolean guardado() { return guardado; }

        /** Deja el objeto como recién arrancado el juego (no guardado en la mochila). */
        public void reiniciar() { guardado = false; }

        @Override public String image() { return "Oficina/" + nombre + ".png"; }

        @Override
        public void interactuar() {
            if (Jugador.get().puedoGuardar()) {
                Jugador.get().guardarObjeto(this);
                estado.encontrado();
                WGame.get().removeVisual(this);
                Sonidos.get().reproducir("Collect.wav");
                guardado = true;
            } else {
                WGame.get().say(this, "¿En donde queres que lo guarde?");
                Sonidos.get().reproducir("Huh.wav");
            }
        }
    }

    public static final ObjetoPersonal BILLETERA = new ObjetoPersonal(WGame.get().at(3, 2), "Billetera", Objetivos.BILLETERA_ESTADO);
    public static final ObjetoPersonal CELULAR = new ObjetoPersonal(WGame.get().at(12, 8), "Celular", Objetivos.CELULAR_ESTADO);
    public static final ObjetoPersonal CREDENCIAL = new ObjetoPersonal(WGame.get().at(9, 9), "Credencial", Objetivos.CREDENCIAL_ESTADO);
    public static final ObjetoPersonal LAPTOP = new ObjetoPersonal(WGame.get().at(5, 3), "Laptop", Objetivos.LAPTOP_ESTADO);
    public static final ObjetoPersonal LLAVES = new ObjetoPersonal(WGame.get().at(12, 4), "Llaves", Objetivos.LLAVES_ESTADO);
    public static final ObjetoPersonal AURICULARES = new ObjetoPersonal(WGame.get().at(7, 6), "Auriculares", Objetivos.AURICULARES_ESTADO);

    // ========== Plantas ==========
    public static class Planta extends GameObject {
        public Planta(Position position) { super(position); }

        @Override public String image() { return "Oficina/Planta.png"; }
        @Override public void teEncontro() { /* nada especial, ver esAtravesable */ }

        @Override
        public void interactuar() {
            Jugador.get().error("Eto no eh comida papi");
        }

        @Override
        public boolean esAtravesable() {
            return Jugador.get().puedeEsconderse() && !Jefe.JEFE1.puedeEsconderse();
        }
    }

    public static final Planta PLANTA1 = new Planta(WGame.get().at(3, 6));
    public static final Planta PLANTA2 = new Planta(WGame.get().at(13, 4));

    // ========== Impresoras ==========
    public static class Impresora extends GameObject {
        private final Objetivos.Tarea tarea;
        private final String color;
        private final String tipo;

        public Impresora(Position position, String tipo, String color, Objetivos.Tarea tarea) {
            super(position);
            this.tipo = tipo;
            this.color = color;
            this.tarea = tarea;
        }

        @Override public String image() { return "Oficina/Impresora" + tipo + color + ".png"; }
        @Override public void teEncontro() { /* siempre "encontrable"; no hace falta lógica extra */ }

        public boolean puedeImprimir() { return !Jugador.get().entregoTarea(tarea); }

        @Override
        public void interactuar() {
            if (puedeImprimir()) {
                Jugador.get().agregarTarea(tarea);
                WGame.get().say(this, "Printer does BRRR BRRR");
                Sonidos.get().reproducir("Impresion.wav");
            } else {
                WGame.get().say(this, "Hace falta magenta");
            }
        }
    }

    public static final Impresora IMPRESORA_AZUL = new Impresora(WGame.get().at(3, 3), "A", "Azul", Objetivos.TAREA_AZUL);
    public static final Impresora IMPRESORA_ROJO = new Impresora(WGame.get().at(9, 4), "B", "Rojo", Objetivos.TAREA_ROJO);
    public static final Impresora IMPRESORA_VERDE = new Impresora(WGame.get().at(14, 3), "A", "Verde", Objetivos.TAREA_VERDE);

    // ========== Máquinas ==========
    public static class Maquina extends GameObject {
        private final String nombre;
        private final Objetivos.Pedido producto;
        private final String sonidoAlActivar;

        public Maquina(Position position, String nombre, Objetivos.Pedido producto, String sonidoAlActivar) {
            super(position);
            this.nombre = nombre;
            this.producto = producto;
            this.sonidoAlActivar = sonidoAlActivar;
        }

        @Override public String image() { return "Oficina/" + nombre + ".png"; }
        @Override public void teEncontro() { /* no-op */ }

        @Override
        public void interactuar() {
            Jugador.get().agarrarObjeto(producto);
            if (sonidoAlActivar != null) Sonidos.get().reproducir(sonidoAlActivar);
        }
    }

    public static final Maquina MAQUINA_CAFE = new Maquina(WGame.get().at(6, 10), "MaquinaCafe", Objetivos.CAFE, "MaquinaCafe.wav");
    public static final Maquina HELADERA = new Maquina(WGame.get().center(), "Heladerita", Objetivos.HELADO, null);

    // ========== Compañeris ==========
    public static class Companieri extends GameObject {
        private final Objetivos.Tarea tareaRequerida;
        private final Objetivos.Pedido pedido;
        private Objetivos.Pedido producto;
        private final String color;
        private boolean objetoEntregado = false;
        private boolean tareaEntregada = false;
        private final String sonidoAlQuejarse;

        public Companieri(Position position, String color, Objetivos.Tarea tareaRequerida,
                          Objetivos.Pedido producto, Objetivos.Pedido pedido, String sonidoAlQuejarse) {
            super(position);
            this.color = color;
            this.tareaRequerida = tareaRequerida;
            this.producto = producto;
            this.pedido = pedido;
            this.sonidoAlQuejarse = sonidoAlQuejarse;
        }

        @Override public String image() { return "Oficina/Companieri" + color + ".png"; }
        @Override public void teEncontro() { /* no-op */ }

        public void generarPedido() { pedido.tipo(producto.tipo()); }
        public String tipoProducto() { return producto.tipo(); }
        public boolean objetoEntregado() { return objetoEntregado; }
        public boolean tareaEntregada() { return tareaEntregada; }

        /** Deja al compañeri como recién arrancado el juego (nada entregado todavía). */
        public void reiniciar() {
            objetoEntregado = false;
            tareaEntregada = false;
        }

        @Override
        public void interactuar() {
            Jugador jugador = Jugador.get();
            if (jugador.noTieneNingunaTarea() && jugador.noTieneNingunObjeto()) {
                WGame.get().say(this, "No me hagas perder el tiempo.");
                Sonidos.get().reproducir("Growly.wav");
            } else if (jugador.noTieneNingunObjeto()) {
                entregarTarea();
            } else {
                entregarObjeto();
            }
        }

        private void entregarTarea() {
            Objetivos.Tarea tarea = Jugador.get().tareaEnMano();
            if (tareaRequerida == tarea) {
                WGame.get().say(this, "¡Me has salvado!");
                Jugador.get().terminarTarea(tarea);
                tarea.seEntrego();
                Sonidos.get().reproducir("Carpeta.wav");
                tareaEntregada = true;
            } else {
                WGame.get().say(this, "Te equivocaste de tarea, papafrita");
                quejarse();
            }
        }

        private void entregarObjeto() {
            Objetivos.Pedido objeto = Jugador.get().objetoEnMano();
            if (producto == objeto && !objetoEntregado) {
                WGame.get().say(this, "Gracias! Me salvaste la tarde");
                Jugador.get().entregarObjeto(this);
                pedido.seEntrego();
                objetoEntregado = true;
                Sonidos.get().reproducir("ThankYou.wav");
            } else {
                WGame.get().say(this, "Gracias, pero no era lo que quería.");
                quejarse();
            }
        }

        public void perdioObjeto() { objetoEntregado = false; }

        private void quejarse() {
            if (sonidoAlQuejarse != null) Sonidos.get().reproducir(sonidoAlQuejarse);
        }
    }

    public static final Companieri COMPANIERI_AZUL = new Companieri(
            WGame.get().at(13, 8), "Azul", Objetivos.TAREA_AZUL, Objetivos.productoAlAzar(), Objetivos.PEDIDO_AZUL, "Wrong.wav");
    public static final Companieri COMPANIERI_ROJO = new Companieri(
            WGame.get().at(6, 4), "Rojo", Objetivos.TAREA_ROJO, Objetivos.productoAlAzar(), Objetivos.PEDIDO_ROJO, "Wrong.wav");
    public static final Companieri COMPANIERI_VERDE = new Companieri(
            WGame.get().at(3, 8), "Verde", Objetivos.TAREA_VERDE, Objetivos.productoAlAzar(), Objetivos.PEDIDO_VERDE, "NoChica.wav");

    // ========== Salidas ==========
    /** Equivale a "const salida = game.at(8,10)" de oficina.wlk (usado también por jefe.wlk). */
    public static final Position SALIDA_POS = WGame.get().at(8, 10);

    public static class Salida extends GameObject {
        private final String imagen;

        public Salida(String imagen) {
            super(SALIDA_POS);
            this.imagen = imagen;
        }

        @Override public String image() { return imagen; }

        public void avanzar() { PantallaJuego.get().nivelActual().finalizarNivel(); }

        @Override public void teEncontro() { avanzar(); }
    }

    public static final Salida PUERTA = new Salida("Oficina/Puerta.png");
    public static final Salida ASCENSOR1 = new Salida("Oficina/Ascensor1.png");
    public static final Salida ASCENSOR2 = new Salida("Oficina/Ascensor2.png");

    // ========== Muros ==========
    public static class Muro extends GameObject {
        private final String imagen;

        public Muro(Position position, String imagen) {
            super(position);
            this.imagen = imagen;
        }

        public Muro(Position position) { this(position, "Oficina/Vacio.png"); }

        @Override public String image() { return imagen; }
        @Override public boolean esAtravesable() { return false; }
    }

    public static final Muro MURO_HORIZONTAL = new Muro(WGame.get().at(6, 7), "Oficina/MuroHorizontal.png");
    public static final Muro MURO_HORIZONTAL2 = new Muro(WGame.get().at(7, 7));
    public static final Muro MURO_HORIZONTAL3 = new Muro(WGame.get().at(8, 7));
    public static final Muro MURO_HORIZONTAL4 = new Muro(WGame.get().at(9, 7));
    public static final Muro MURO_HORIZONTAL5 = new Muro(WGame.get().at(10, 7));

    public static final Muro MURO_VERTICAL = new Muro(WGame.get().at(8, 3), "Oficina/MuroVertical.png");
    public static final Muro MURO_VERTICAL2 = new Muro(WGame.get().at(8, 4));
    public static final Muro MURO_VERTICAL3 = new Muro(WGame.get().at(8, 5));
    public static final Muro MURO_VERTICAL4 = new Muro(WGame.get().at(8, 6));

    public static void agregarMurosHorizontales() {
        WGame.get().addVisual(MURO_HORIZONTAL);
        WGame.get().addVisual(MURO_HORIZONTAL2);
        WGame.get().addVisual(MURO_HORIZONTAL3);
        WGame.get().addVisual(MURO_HORIZONTAL4);
        WGame.get().addVisual(MURO_HORIZONTAL5);
    }

    public static void agregarMurosVerticales() {
        WGame.get().addVisual(MURO_VERTICAL);
        WGame.get().addVisual(MURO_VERTICAL2);
        WGame.get().addVisual(MURO_VERTICAL3);
        WGame.get().addVisual(MURO_VERTICAL4);
    }

    // ========== Cuadros ==========
    public static final StaticImage CARPINCHITO = new StaticImage(WGame.get().at(10, 10), "Oficina/Carpinchito.png");
    public static final StaticImage CARPINCHO_MATERO = new StaticImage(WGame.get().at(6, 10), "Oficina/CarpinchoMatero.png");

    public static class Cuadro extends GameObject {
        private final String imagen;
        private final Runnable mostrarImagenAccion;

        public Cuadro(Position position, String imagen, Runnable mostrarImagenAccion) {
            super(position);
            this.imagen = imagen;
            this.mostrarImagenAccion = mostrarImagenAccion;
        }

        @Override public String image() { return imagen; }
        @Override public void teEncontro() { /* no-op, ver interactuar() */ }

        @Override
        public void interactuar() {
            Niveles.Nivel actual = PantallaJuego.get().nivelActual();
            if (actual == Niveles.NIVEL1) {
                Jefe.JEFE1.esconderse();
            } else if (actual == Niveles.NIVEL2) {
                Jefe.JEFE2.esconderse();
            } else {
                Jefe.JEFE1.esconderse();
                Jefe.JEFE2.esconderse();
            }
            mostrarImagenAccion.run();
        }
    }

    public static final Cuadro CUADRITO1 = new Cuadro(WGame.get().at(10, 10), "Oficina/Cuadro1.png", () -> {
        WGame.get().addVisual(CARPINCHITO);
        WGame.get().schedule(4000, () -> WGame.get().removeVisual(CARPINCHITO));
    });

    public static final Cuadro CUADRITO2 = new Cuadro(WGame.get().at(6, 10), "Oficina/Cuadro2.png", () -> {
        WGame.get().addVisual(CARPINCHO_MATERO);
        WGame.get().schedule(4000, () -> WGame.get().removeVisual(CARPINCHO_MATERO));
    });

    /**
     * Deja a los compañeris y objetos personales como recién arrancado el
     * juego. Se llama al volver al menú de inicio después de game over o
     * victoria, para que una partida nueva no arranque con cosas ya
     * entregadas/guardadas de la partida anterior.
     */
    public static void reiniciarTodo() {
        for (Companieri c : java.util.List.of(COMPANIERI_AZUL, COMPANIERI_ROJO, COMPANIERI_VERDE)) {
            c.reiniciar();
        }
        for (ObjetoPersonal o : java.util.List.of(BILLETERA, CELULAR, CREDENCIAL, LAPTOP, LLAVES, AURICULARES)) {
            o.reiniciar();
        }
    }
}