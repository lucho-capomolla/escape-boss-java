package juego;

import wgame.GameObject;
import wgame.Position;
import wgame.WGame;

import java.util.List;

/**
 * Puerto de tarjetas.wlk (real, subido).
 */
public class Tarjetas {

    private static final Position POSICION_TARJETA = WGame.get().at(15, 10);

    public static class Tarjeta extends GameObject {
        private final String nombreTarjeta;

        public Tarjeta(String nombreTarjeta) {
            super(POSICION_TARJETA);
            this.nombreTarjeta = nombreTarjeta;
        }

        @Override public String image() { return "Tarjetas/" + nombreTarjeta + ".png"; }

        public void producirEfecto() { /* no-op por defecto */ }
    }

    // ========== mazoTarjeta (singleton, "el mazo visible en el tablero") ==========
    public static class MazoTarjeta extends GameObject {
        private List<Tarjeta> mazoTarjetas = List.of();

        private MazoTarjeta() { super(POSICION_TARJETA); }

        @Override public String image() { return "Tarjetas/Mazo.png"; }

        public void mazoTarjetas(List<Tarjeta> mazo) { this.mazoTarjetas = mazo; }

        public void ponerTarjeta() {
            if (mazoTarjetas.isEmpty()) return; // defensivo: en Wollok anyOne() de una lista vacía rompe
            Tarjeta elegida = mazoTarjetas.get((int) (Math.random() * mazoTarjetas.size()));
            WGame.get().addVisual(elegida);
            Sonidos.get().reproducir("Alerta.wav");
            elegida.producirEfecto();
            WGame.get().schedule(2000, () -> {
                if (WGame.get().hasVisual(elegida)) WGame.get().removeVisual(elegida);
            });
        }
    }

    public static final MazoTarjeta MAZO_TARJETA = new MazoTarjeta();

    // ========== cartas concretas ==========

    public static final Tarjeta JEFE_A_UNA_IMPRESORA = new Tarjeta("JefeAImpresora") {
        private final List<Position> posImpresora = List.of(
                Oficina.IMPRESORA_AZUL.position(), Oficina.IMPRESORA_ROJO.position(), Oficina.IMPRESORA_VERDE.position());

        @Override public void producirEfecto() {
            Jefe.JEFE1.position(posImpresora.get((int) (Math.random() * posImpresora.size())));
        }
    };

    public static abstract class JefeACompanieri extends Tarjeta {
        protected final List<Position> posCompanieri = List.of(
                Oficina.COMPANIERI_AZUL.position(), Oficina.COMPANIERI_ROJO.position(), Oficina.COMPANIERI_VERDE.position());

        protected JefeACompanieri(String nombreTarjeta) { super(nombreTarjeta); }

        protected Position companieriAlAzar() { return posCompanieri.get((int) (Math.random() * posCompanieri.size())); }
    }

    public static final JefeACompanieri JEFE1_A_COMPANIERI = new JefeACompanieri("JefeACompanieri") {
        @Override public void producirEfecto() { Jefe.JEFE1.position(companieriAlAzar()); }
    };

    public static final JefeACompanieri JEFE2_A_COMPANIERI = new JefeACompanieri("JefeACompanieri") {
        @Override public void producirEfecto() { Jefe.JEFE2.position(companieriAlAzar()); }
    };

    public static final Tarjeta AGREGAR_ENERGIA = new Tarjeta("AgregarEnergia") {
        private int cantidad = 15;
        public void cantidad(int unaCantidad) { this.cantidad = unaCantidad; }
        @Override public void producirEfecto() { Jugador.get().aumentarEnergia(cantidad); }
    };

    public static final Tarjeta RESTAURAR_ENERGIA = new Tarjeta("RestaurarEnergia") {
        @Override public void producirEfecto() { Jugador.get().energia(100); }
    };

    public static final Tarjeta PERDER_ENERGIA = new Tarjeta("PerderEnergia") {
        private int porcentaje = 15;
        public void porcentaje(int unPorcentaje) { this.porcentaje = unPorcentaje; }

        @Override public void producirEfecto() {
            // NOTA: el wlk original usa el literal 15 acá en vez de la variable
            // "porcentaje" (que se puede configurar con porcentaje(...) pero
            // nunca se usa realmente en producirEfecto). Lo porto tal cual,
            // bug incluido, para que se comporte igual que el original.
            int aRestar = (Jugador.get().energia() * 15) / 100;
            Jugador.get().disminuirEnergia(aRestar);
        }
    };

    public static final Tarjeta PERDER_TAREA = new Tarjeta("PerderTarea") {
        @Override public void producirEfecto() {
            var tareas = Jugador.get().tareasRealizadas();
            if (!tareas.isEmpty()) {
                Objetivos.Tarea tareaAPerder = tareas.iterator().next();
                Jugador.get().quitarTarea(tareaAPerder);
            }
        }
    };

    public static final Tarjeta VOLVER_AL_INICIO = new Tarjeta("VolverAlInicio") {
        @Override public void producirEfecto() { Jugador.get().moverAlInicio(); }
    };

    public static final Tarjeta PERDER_OBJETO = new Tarjeta("PerderObjeto") {
        @Override public void producirEfecto() {
            var objetos = Jugador.get().objetosEnMochila();
            if (!objetos.isEmpty()) {
                Oficina.ObjetoPersonal objetoAPerder = objetos.iterator().next();
                Jugador.get().quitarObjeto(objetoAPerder);
            }
        }
    };

    /** No se usa en ningún mazo1/2/3 del original tampoco (queda definida, sin uso). */
    public static final Tarjeta PERDER_PEDIDO = new Tarjeta("PerderPedido") {
        @Override public void producirEfecto() {
            var companieris = Jugador.get().companierisAyudados();
            if (!companieris.isEmpty()) {
                Oficina.Companieri companieriPerdido = companieris.iterator().next();
                Jugador.get().quitarPedido(companieriPerdido);
            }
        }
    };

    /** No se usa en ningún mazo1/2/3 del original tampoco (queda definida, sin uso). */
    public static final Tarjeta JEFE_A_MAQUINA = new Tarjeta("JefeAMaquina") {
        private final List<Position> posMaquinas = List.of(Oficina.MAQUINA_CAFE.position(), Oficina.HELADERA.position());
        @Override public void producirEfecto() {
            Jefe.JEFE2.position(posMaquinas.get((int) (Math.random() * posMaquinas.size())));
        }
    };

    /** Tampoco se usan en ningún mazo del original. */
    public static abstract class JefeAObjeto extends Tarjeta {
        protected final List<Position> posObjetos = List.of(
                Oficina.BILLETERA.position(), Oficina.CREDENCIAL.position(), Oficina.LLAVES.position(),
                Oficina.LAPTOP.position(), Oficina.AURICULARES.position(), Oficina.CELULAR.position());

        protected JefeAObjeto(String nombreTarjeta) { super(nombreTarjeta); }

        protected Position objetoAlAzar() { return posObjetos.get((int) (Math.random() * posObjetos.size())); }
    }

    public static final JefeAObjeto JEFE1_A_OBJETO = new JefeAObjeto("JefeAObjeto") {
        @Override public void producirEfecto() {
            Position objetoVigilado = objetoAlAzar();
            // NOTA: el original llama game.hasVisual(objetoVigilado) pasándole una
            // Position (posObjetos son posiciones, no visuals) — no calza con la
            // firma real de hasVisual(visual) y parece un bug del wlk original.
            // Lo traduzco a la intención más probable: "¿todavía hay algo en esa celda?".
            if (!WGame.get().visualsAt(objetoVigilado).isEmpty()) {
                Jefe.JEFE1.position(objetoVigilado);
            }
        }
    };

    public static final JefeAObjeto JEFE2_A_OBJETO = new JefeAObjeto("JefeAObjeto") {
        @Override public void producirEfecto() {
            Position objetoVigilado = objetoAlAzar();
            if (!WGame.get().visualsAt(objetoVigilado).isEmpty()) {
                Jefe.JEFE2.position(objetoVigilado);
            }
        }
    };

    // ========== mazos (declarados al final: en Java no pueden referenciar
    // hacia adelante campos static de la misma clase) ==========
    public static final List<Tarjeta> MAZO1 = List.of(
            JEFE_A_UNA_IMPRESORA, JEFE1_A_COMPANIERI, AGREGAR_ENERGIA, RESTAURAR_ENERGIA, PERDER_ENERGIA, PERDER_TAREA, VOLVER_AL_INICIO);

    public static final List<Tarjeta> MAZO2 = List.of(
            JEFE2_A_COMPANIERI, AGREGAR_ENERGIA, RESTAURAR_ENERGIA, PERDER_ENERGIA, VOLVER_AL_INICIO);

    public static final List<Tarjeta> MAZO3 = List.of(
            JEFE1_A_COMPANIERI, JEFE2_A_COMPANIERI, AGREGAR_ENERGIA, RESTAURAR_ENERGIA, PERDER_ENERGIA, VOLVER_AL_INICIO, PERDER_OBJETO);
}
