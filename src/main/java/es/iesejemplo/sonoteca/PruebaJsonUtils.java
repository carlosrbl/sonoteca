package es.iesejemplo.sonoteca;

import es.iesejemplo.sonoteca.modelo.Cancion;
import es.iesejemplo.sonoteca.util.JsonUtils;

import java.util.Arrays;
import java.util.List;

public class PruebaJsonUtils {

    public static void main(String[] args) {
        Cancion cancion1 = new Cancion("Bohemian Rhapsody", 354, 1, null);
        Cancion cancion2 = new Cancion("Hotel California", 390, 2, null);
        Cancion cancion3 = new Cancion("Stairway to Heaven", 482, 3, null);

        List<Cancion> listaOriginal = Arrays.asList(cancion1, cancion2, cancion3);

        System.out.println("--- PRUEBA CANCIÓN SUELTA ---");
        String jsonCancion = JsonUtils.exportarCancion(cancion1);
        System.out.println("Canción exportada a JSON:\n" + jsonCancion);

        Cancion cancionRecuperada = JsonUtils.importarCancion(jsonCancion);
        System.out.println("\nCanción recuperada desde JSON:\n" + cancionRecuperada);

        System.out.println("\n--- PRUEBA LISTA DE CANCIONES ---");
        String jsonLista = JsonUtils.exportarLista(listaOriginal);
        System.out.println("Lista exportada a JSON:\n" + jsonLista);

        List<Cancion> listaRecuperada = JsonUtils.importarLista(jsonLista);
        System.out.println("\nLista recuperada desde JSON:");
        for (Cancion c : listaRecuperada) {
            System.out.println(c);
        }
    }
}
