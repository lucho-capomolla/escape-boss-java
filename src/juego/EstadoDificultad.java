package juego;

public class EstadoDificultad extends Estado {

    private static final EstadoDificultad INSTANCE = new EstadoDificultad();
    public static EstadoDificultad get() { return INSTANCE; }
    private EstadoDificultad() {}

    private Dificultad dificultad;

    private void confirmarDificultad() {
        Jefe.JEFE1.elegirDificultad(dificultad);
        Jefe.JEFE2.elegirDificultad(dificultad);
        MenuInicio.get().mostrarInstrucciones();
        Sonidos.get().reproducir("Confirmar.wav");
    }

    @Override public void alPresionar1() { dificultad = Dificultad.FACIL; confirmarDificultad(); }
    @Override public void alPresionar2() { dificultad = Dificultad.NORMAL; confirmarDificultad(); }
    @Override public void alPresionar3() { dificultad = Dificultad.DIFICIL; confirmarDificultad(); }
}
