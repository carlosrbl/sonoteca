package es.iesejemplo.sonoteca.util;

/**
 * UNIDAD 1 - EJERCICIO 2
 * <p>
 * Utilidad para convertir una duración en segundos a formato "mm:ss" y viceversa.
 * Complétala siguiendo las indicaciones de la práctica y comprueba tu solución
 * ejecutando los tests ya incluidos en el proyecto ({@code DuracionTest}).
 */
public class Duracion {

    private final int segundosTotales;

    /**
     * @param segundosTotales duración total en segundos; debe ser >= 0.
     * @throws IllegalArgumentException si segundosTotales es negativo.
     */
    public Duracion(int segundosTotales) {
        if (segundosTotales < 0) {
            throw new IllegalArgumentException("El segundo del duracion es negativo");
        }
        this.segundosTotales = segundosTotales;
    }

    public int getSegundosTotales() {
        return segundosTotales;
    }

    /**
     * @return la duración con formato "mm:ss" (por ejemplo, 185 segundos -> "3:05").
     *         Los segundos siempre se muestran con dos dígitos.
     */
    public String aTexto() {
        int minutos = getSegundosTotales() / 60;
        int segundos = getSegundosTotales() % 60;
        return String.format("%d:%02d", minutos, segundos);
    }

    /**
     * Proceso inverso a {@link #aTexto()}.
     *
     * @param texto una duración con formato "mm:ss".
     * @return el objeto Duracion equivalente.
     */
    public static Duracion desdeTexto(String texto) {
        String[] partes = texto.split(":");
        int minutos = Integer.parseInt(partes[0]);
        int segundos = Integer.parseInt(partes[1]);
        int segundosTotales = (minutos * 60) +  segundos;
        return new Duracion(segundosTotales);
    }

    @Override
    public String toString() {
        return aTexto();
    }
}
