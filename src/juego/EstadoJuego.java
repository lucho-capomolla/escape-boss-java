package juego;

import juego.Direcciones.Direccion;

public class EstadoJuego extends Estado {

    private static final EstadoJuego INSTANCE = new EstadoJuego();
    public static EstadoJuego get() { return INSTANCE; }
    private EstadoJuego() {}

    @Override public void alPresionarUp()    { Jugador.get().moverse(Direccion.ARRIBA); }
    @Override public void alPresionarDown()  { Jugador.get().moverse(Direccion.ABAJO); }
    @Override public void alPresionarRight() { Jugador.get().moverse(Direccion.DERECHA); }
    @Override public void alPresionarLeft()  { Jugador.get().moverse(Direccion.IZQUIERDA); }
    @Override public void alPresionarE()     { Jugador.get().accionar(); }
}
