package juego;

public class EstadoMenuInicio extends Estado {

    private static final EstadoMenuInicio INSTANCE = new EstadoMenuInicio();
    public static EstadoMenuInicio get() { return INSTANCE; }
    private EstadoMenuInicio() {}

    @Override
    public void alPresionarEnter() {
        MenuInicio.get().elegirDificultad();
        Sonidos.get().reproducir("Confirmar.wav");
    }
}
