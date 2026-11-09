package juego;

import wgame.Log;
import wgame.Sound;
import wgame.WGame;

import java.util.List;
import java.util.Set;

/**
 * Puerto de niveles.wlk (real, subido).
 */
public class Niveles {

    /** Equivale a "const musicaJuego = sonido.sonido('MisionImposible.wav')" (nivel de módulo). */
    public static final Sound MUSICA_JUEGO = Sonidos.get().sonido("MisionImposible.wav");

    public static abstract class Nivel {
        private boolean nivelPasado = false;

        public boolean nivelPasado() { return nivelPasado; }
        protected void pasarDeNivel() {
            nivelPasado = true;
            Log.info("Niveles", "pasarDeNivel(): nivel completado");
        }

        /** Deja el nivel como recién arrancado el juego (no pasado todavía). */
        public void reiniciar() { nivelPasado = false; }

        /** Comportamiento común a los 3 niveles; cada uno la pisa y llama super.cargarNivel() en su punto correspondiente. */
        public void cargarNivel() {
            Log.debug("Niveles", "cargarNivel() común: muros, comida, compañeris, plantas, tarjetas, colisiones");
            Oficina.agregarMurosHorizontales();
            Oficina.agregarMurosVerticales();

            WGame.get().addVisual(Oficina.CAFE_CON_LECHE);
            WGame.get().addVisual(Oficina.CHOCOLATE);
            WGame.get().addVisual(Oficina.HAMBURGUESA);
            WGame.get().addVisual(Oficina.ROSQUILLA);
            WGame.get().addVisual(Oficina.BEBIDA);
            WGame.get().addVisual(Oficina.PIZZA);
            WGame.get().addVisual(Oficina.COMPANIERI_AZUL);
            WGame.get().addVisual(Oficina.COMPANIERI_ROJO);
            WGame.get().addVisual(Oficina.COMPANIERI_VERDE);
            WGame.get().addVisual(Oficina.PLANTA1);
            WGame.get().addVisual(Oficina.PLANTA2);
            WGame.get().addVisual(Oficina.CUADRITO1);
            WGame.get().addVisual(Jugador.get());

            WGame.get().showAttributes(Jugador.get());
            WGame.get().addVisual(EnergiaJugador.INSTANCE);

            WGame.get().addVisual(Tarjetas.MAZO_TARJETA);
            Configuraciones.get().configurarColisiones();
            Configuraciones.get().cambiarEstado(EstadoJuego.get());

            WGame.get().onTick(10000, "Sacar tarjeta", Tarjetas.MAZO_TARJETA::ponerTarjeta);
        }

        /** Sin cuerpo en el original (cada nivel la define entera). */
        public abstract void finalizarNivel();

        protected void reiniciarElementos() {
            Jefe.JEFE1.volverAlInicio();
            Jefe.JEFE2.volverAlInicio();
            WGame.get().removeTickEvent("Sacar tarjeta");
            Tarjetas.MAZO_TARJETA.mazoTarjetas(List.of());
            Jugador.get().moverAlInicio();
            Jugador.get().aumentarEnergia(100);
        }
    }

    // ========== nivel1 ==========
    public static final Nivel NIVEL1 = new Nivel() {
        private final Set<Objetivos.Tarea> tareasNecesarias =
                Set.of(Objetivos.TAREA_AZUL, Objetivos.TAREA_VERDE, Objetivos.TAREA_ROJO);

        @Override
        public void cargarNivel() {
            Log.info("Niveles", "=== Cargando nivel1 ===");
            MUSICA_JUEGO.play();
            WGame.get().addVisual(PantallaJuego.FONDO_ULTIMO_NIVEL);
            WGame.get().addVisual(Oficina.ASCENSOR2);

            Tarjetas.MAZO_TARJETA.mazoTarjetas(Tarjetas.MAZO1);

            WGame.get().addVisual(Oficina.IMPRESORA_AZUL);
            WGame.get().addVisual(Oficina.IMPRESORA_ROJO);
            WGame.get().addVisual(Oficina.IMPRESORA_VERDE);

            WGame.get().addVisual(Objetivos.TAREA_AZUL);
            WGame.get().addVisual(Objetivos.TAREA_ROJO);
            WGame.get().addVisual(Objetivos.TAREA_VERDE);

            super.cargarNivel();

            WGame.get().addVisual(Jefe.JEFE1);
            Jefe.JEFE1.moverse();
        }

        @Override
        public void finalizarNivel() {
            if (tareasNecesarias.equals(Jugador.get().tareasRealizadas())) {
                Log.info("Niveles", "nivel1 completo (3 tareas entregadas) -> avanzando a nivel2");
                for (var v : List.copyOf(WGame.get().allVisuals())) WGame.get().removeVisual(v);
                reiniciarElementos();
                pasarDeNivel();
                PantallaJuego.get().avanzarNivel(NIVEL2);
            } else {
                Log.debug("Niveles", "nivel1 finalizarNivel(): todavía faltan tareas ("
                        + Jugador.get().tareasRealizadas().size() + "/3)");
                WGame.get().say(Oficina.ASCENSOR2, "Te faltan más tareas, apurate!");
            }
        }
    };

    // ========== nivel2 ==========
    public static final Nivel NIVEL2 = new Nivel() {
        private final Set<Oficina.Companieri> companierisAyudados =
                Set.of(Oficina.COMPANIERI_ROJO, Oficina.COMPANIERI_AZUL, Oficina.COMPANIERI_VERDE);

        @Override
        public void cargarNivel() {
            Log.info("Niveles", "=== Cargando nivel2 ===");
            WGame.get().addVisual(PantallaJuego.FONDO_NIVEL_2);
            WGame.get().addVisual(Oficina.ASCENSOR1);

            Tarjetas.MAZO_TARJETA.mazoTarjetas(Tarjetas.MAZO2);

            inicializarPedidos();

            WGame.get().addVisual(Oficina.MAQUINA_CAFE);
            WGame.get().addVisual(Oficina.HELADERA);

            super.cargarNivel();

            WGame.get().addVisual(Jefe.JEFE2);
            Jefe.JEFE2.moverse();
        }

        @Override
        public void finalizarNivel() {
            if (companierisAyudados.equals(Jugador.get().companierisAyudados())) {
                Log.info("Niveles", "nivel2 completo (3 compañeris ayudados) -> avanzando a nivel3");
                for (var v : List.copyOf(WGame.get().allVisuals())) WGame.get().removeVisual(v);
                reiniciarElementos();
                pasarDeNivel();
                PantallaJuego.get().avanzarNivel(NIVEL3);
            } else {
                Log.debug("Niveles", "nivel2 finalizarNivel(): todavía faltan compañeris ("
                        + Jugador.get().companierisAyudados().size() + "/3)");
                WGame.get().say(Oficina.ASCENSOR1, "Segui ayudando a tus compañeris!");
            }
        }

        private void inicializarPedidos() {
            Oficina.COMPANIERI_ROJO.generarPedido();
            WGame.get().addVisual(Objetivos.PEDIDO_ROJO);
            Oficina.COMPANIERI_AZUL.generarPedido();
            WGame.get().addVisual(Objetivos.PEDIDO_AZUL);
            Oficina.COMPANIERI_VERDE.generarPedido();
            WGame.get().addVisual(Objetivos.PEDIDO_VERDE);
        }
    };

    // ========== nivel3 ==========
    public static final Nivel NIVEL3 = new Nivel() {
        private final Set<Oficina.ObjetoPersonal> objetosNecesarios = Set.of(
                Oficina.LLAVES, Oficina.LAPTOP, Oficina.CREDENCIAL, Oficina.CELULAR, Oficina.AURICULARES, Oficina.BILLETERA);

        @Override
        public void cargarNivel() {
            Log.info("Niveles", "=== Cargando nivel3 ===");
            WGame.get().addVisual(Oficina.PUERTA);
            Tarjetas.MAZO_TARJETA.mazoTarjetas(Tarjetas.MAZO3);
            WGame.get().addVisual(Oficina.CUADRITO2);
            WGame.get().addVisual(Objetivos.CONTENIDO_MOCHILA);
            prepararObjetos();

            super.cargarNivel();

            WGame.get().addVisual(Jefe.JEFE1);
            WGame.get().addVisual(Jefe.JEFE2);
            Jefe.JEFE1.moverse();
            Jefe.JEFE2.moverse();
        }

        @Override
        public void finalizarNivel() {
            if (objetosNecesarios.equals(Jugador.get().objetosEnMochila())) {
                Log.info("Niveles", "nivel3 completo (6 objetos recuperados) -> terminando juego");
                pasarDeNivel();
                PantallaJuego.get().terminarJuego();
            } else {
                Log.debug("Niveles", "nivel3 finalizarNivel(): todavía faltan objetos ("
                        + Jugador.get().objetosEnMochila().size() + "/6)");
                WGame.get().say(Oficina.PUERTA, "Todavía te faltan cosas, apurate!");
            }
        }

        private void prepararObjetos() {
            WGame.get().addVisual(Oficina.MOCHILA);
            WGame.get().addVisual(Oficina.LLAVES);
            WGame.get().addVisual(Oficina.LAPTOP);
            WGame.get().addVisual(Oficina.CREDENCIAL);
            WGame.get().addVisual(Oficina.CELULAR);
            WGame.get().addVisual(Oficina.AURICULARES);
            WGame.get().addVisual(Oficina.BILLETERA);

            WGame.get().addVisual(Objetivos.AURICULARES_ESTADO);
            WGame.get().addVisual(Objetivos.LLAVES_ESTADO);
            WGame.get().addVisual(Objetivos.LAPTOP_ESTADO);
            WGame.get().addVisual(Objetivos.CREDENCIAL_ESTADO);
            WGame.get().addVisual(Objetivos.CELULAR_ESTADO);
            WGame.get().addVisual(Objetivos.BILLETERA_ESTADO);
        }
    };
}