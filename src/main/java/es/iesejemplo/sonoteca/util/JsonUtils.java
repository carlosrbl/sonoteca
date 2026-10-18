package es.iesejemplo.sonoteca.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonSyntaxException;
import es.iesejemplo.sonoteca.modelo.Biblioteca;
import es.iesejemplo.sonoteca.modelo.Cancion;

import java.lang.reflect.Type;
import java.util.List;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;

/**
 * UNIDAD 1 - EJERCICIO 3 (canciones sueltas) y UNIDAD 2 - EJERCICIO 4 (biblioteca completa).
 * <p>
 * Utilidades de conversión entre los objetos del modelo y JSON, usando Gson.
 */
public class JsonUtils {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter())
            .create();

    private JsonUtils() {
        // clase de utilidades: no se instancia
    }

    // ----- Unidad 1: una canción suelta -----

    public static String exportarCancion(Cancion cancion) {
        return GSON.toJson(cancion);
    }

    public static Cancion importarCancion(String json) {
        return GSON.fromJson(json, Cancion.class);
    }

    public static String exportarLista(List<Cancion> canciones) {
        return GSON.toJson(canciones);
    }

    public static List<Cancion> importarLista(String json) {
        Type tipoLista = new TypeToken<List<Cancion>>(){}.getType();
        return GSON.fromJson(json, tipoLista);
    }

    // ----- Unidad 2: la biblioteca completa -----

    /**
     * Escribe la biblioteca completa en un fichero de texto en formato JSON legible.
     */
    public static void exportarBibliotecaJson(Biblioteca biblioteca, String rutaFichero) {
        java.nio.file.Path ruta = java.nio.file.Path.of(rutaFichero).toAbsolutePath();
        System.out.println("Exportando biblioteca a JSON en: " + ruta);

        try {
            if (ruta.getParent() != null && java.nio.file.Files.notExists(ruta.getParent())) {
                java.nio.file.Files.createDirectories(ruta.getParent());
            }

            try (BufferedWriter escritor = java.nio.file.Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
                GSON.toJson(biblioteca, escritor);
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error al exportar a JSON en " + ruta + ": " + e.getMessage(), e);
        }
    }

    /**
     * Lee un fichero JSON con una biblioteca completa y la reconstruye.
     */
    public static Biblioteca importarBibliotecaJson(String rutaFichero) {
        java.nio.file.Path ruta = java.nio.file.Path.of(rutaFichero).toAbsolutePath();

        if (java.nio.file.Files.notExists(ruta)) {
            return new Biblioteca();
        }

        try (BufferedReader lector = java.nio.file.Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            Biblioteca b = GSON.fromJson(lector, Biblioteca.class);
            return b != null ? b : new Biblioteca();
        } catch (JsonSyntaxException | java.io.IOException e) {
            System.err.println("Error al importar JSON desde " + ruta + ": " + e.getMessage());
            return new Biblioteca();
        }
    }
}
