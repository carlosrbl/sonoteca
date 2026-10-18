package es.iesejemplo.sonoteca.repositorio.ficheros;

import es.iesejemplo.sonoteca.modelo.*;
import es.iesejemplo.sonoteca.repositorio.RepositorioSonoTeca;

import java.io.File;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.io.IOException;

/**
 * UNIDAD 2 — Persistencia en ficheros.
 * <p>
 * Guarda y recupera la biblioteca completa de un fichero binario (serialización)
 * y sabe escanear una carpeta de MP3 para construir la biblioteca inicial a
 * partir de sus cabeceras ID3v1.
 * <p>
 * Complétala siguiendo los ejercicios de la práctica de la unidad 2. Todos los
 * métodos de {@link RepositorioSonoTeca} deben quedar implementados para que la
 * aplicación funcione con ella (cambia la línea correspondiente en {@code Main}).
 */
public class RepositorioFicheros implements RepositorioSonoTeca {

    private final String rutaFicheroBiblioteca;
    private Biblioteca biblioteca;

    /**
     * @param rutaFicheroBiblioteca ruta del fichero binario donde se guarda la biblioteca
     *                              (por ejemplo, "sonoteca.dat"). Si no existe todavía,
     *                              se debe partir de una biblioteca vacía.
     */
    public RepositorioFicheros(String rutaFicheroBiblioteca) {
        this.rutaFicheroBiblioteca = rutaFicheroBiblioteca;
        try {
            this.biblioteca = cargarBiblioteca(rutaFicheroBiblioteca);
        } catch (UnsupportedOperationException e) {
            // Todavía no has hecho el ejercicio 3 (cargarBiblioteca): empezamos con una
            // biblioteca vacía en memoria para poder probar ya los ejercicios 1 y 2.
            System.out.println("(cargarBiblioteca aún no implementado; empezando con una biblioteca vacía en memoria)");
            this.biblioteca = new Biblioteca();
        }
    }

    // ---------- Ejercicio 1: escanear un directorio en busca de MP3 ----------

    /**
     * @return la lista de ficheros .mp3 encontrados recursivamente a partir de rutaBase.
     *         Si rutaBase no existe o no es un directorio, devuelve una lista vacía.
     */
    public List<File> escanearDirectorio(String rutaBase) {
        if (rutaBase == null || rutaBase.isBlank()) {
            return List.of();
        }

        Path base = Path.of(rutaBase);
        if (!Files.isDirectory(base)) {
            return List.of();
        }

        try (Stream<Path> rutas = Files.walk(base)) {
            return rutas
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".mp3"))
                    .sorted()
                    .map(Path::toFile)
                    .toList();
        } catch (IOException e) {
            System.err.println("Error al escanear el directorio: " + e.getMessage());
            return List.of();
        }
    }

    // ---------- Ejercicio 2: leer la cabecera ID3v1 ----------

    /**
     * Lee los últimos 128 bytes de ficheroMp3 (cabecera ID3v1) y construye la Cancion
     * correspondiente (creando también, si hace falta, Artista/Album/Genero).
     * Si el fichero no tiene cabecera ID3v1 válida, devuelve null.
     */
    public Cancion leerCabeceraID3(File ficheroMp3) {
        // TODO: implementar (unidad 2, ejercicio 2).
        throw new UnsupportedOperationException("Funcionalidad no implementada (unidad 2, ejercicio 2)");
    }

    // ---------- Ejercicio 3: serializar/deserializar la biblioteca ----------

    public void guardarBiblioteca(Biblioteca biblioteca, String rutaFichero) {
        // TODO: implementar con ObjectOutputStream (unidad 2, ejercicio 3).
        throw new UnsupportedOperationException("Funcionalidad no implementada (unidad 2, ejercicio 3)");
    }

    public Biblioteca cargarBiblioteca(String rutaFichero) {
        // TODO: implementar con ObjectInputStream (unidad 2, ejercicio 3).
        // Si el fichero no existe todavía, devolver "new Biblioteca()" en lugar de fallar.
        throw new UnsupportedOperationException("Funcionalidad no implementada (unidad 2, ejercicio 3)");
    }

    // ---------- Implementación de RepositorioSonoTeca (Ejercicio 0) ----------
    // A partir de aquí, apóyate en el objeto "biblioteca" cargado en memoria,
    // y recuerda volver a llamar a guardarBiblioteca(...) cada vez que la modifiques.

    @Override
    public List<Genero> listarGeneros() {
        return biblioteca.getGeneros();
    }

    @Override
    public List<Artista> listarArtistas() {
        return biblioteca.getArtistas();
    }

    @Override
    public List<Album> listarAlbumes() {
        return biblioteca.getAlbumes();
    }

    @Override
    public List<Cancion> listarCanciones() {
        return biblioteca.getCanciones();
    }

    @Override
    public List<Cancion> buscarCanciones(String texto) {
        String textoLower = texto.toLowerCase();
        return biblioteca.getCanciones().stream()
                .filter(c -> c.getTitulo().toLowerCase().contains(textoLower))
                .toList();
    }

    @Override
    public void altaCancion(Cancion cancion) {
        int nuevoIdCancion = biblioteca.getCanciones().stream().mapToInt(Cancion::getId).max().orElse(0) + 1;
        cancion.setId(nuevoIdCancion);

        if (cancion.getAlbum() != null) {
            Album albumOriginal = cancion.getAlbum();

            Genero genero = null;
            if (albumOriginal.getGenero() != null) {
                genero = obtenerCrearGenero(albumOriginal.getGenero().getNombre());
            }

            Artista artista = null;
            if (albumOriginal.getArtista() != null) {
                artista = obtenerCrearArtista(albumOriginal.getArtista().getNombre(), albumOriginal.getArtista().getNacionalidad());
            }

            Album album = obtenerCrearAlbum(albumOriginal.getTitulo(), albumOriginal.getAnio(), artista, genero);
            cancion.setAlbum(album);
        }

        biblioteca.getCanciones().add(cancion);
    }

    @Override
    public void actualizarCancion(Cancion cancion) {
        for (int i = 0; i < biblioteca.getCanciones().size(); i++) {
            if (biblioteca.getCanciones().get(i).getId() == cancion.getId()) {
                biblioteca.getCanciones().set(i, cancion);
                break;
            }
        }
    }

    @Override
    public void eliminarCancion(int idCancion) {
        biblioteca.getCanciones().removeIf(c -> c.getId() == idCancion);
    }

    @Override
    public List<ListaReproduccion> listarListas() {
        return biblioteca.getListasReproduccion();
    }

    @Override
    public void altaLista(ListaReproduccion lista) {
        int nuevoId = biblioteca.getListasReproduccion().stream().mapToInt(ListaReproduccion::getId).max().orElse(0) + 1;
        lista.setId(nuevoId);
        biblioteca.getListasReproduccion().add(lista);
    }

    @Override
    public void anyadirCancionALista(int idLista, int idCancion, int posicion) {
        ListaReproduccion lista = biblioteca.getListasReproduccion().stream()
                .filter(l -> l.getId() == idLista).findFirst().orElse(null);
        Cancion cancion = biblioteca.getCanciones().stream()
                .filter(c -> c.getId() == idCancion).findFirst().orElse(null);

        if (lista != null && cancion != null) {
            int indice = posicion - 1;
            if (indice >= 0 && indice <= lista.getCanciones().size()) {
                lista.getCanciones().add(indice, cancion);
            } else {
                lista.getCanciones().add(cancion);
            }
        }
    }

    @Override
    public List<Cancion> cancionesDeLista(int idLista) {
        ListaReproduccion lista = biblioteca.getListasReproduccion().stream()
                .filter(l -> l.getId() == idLista).findFirst().orElse(null);
        if (lista != null) {
            return lista.getCanciones();
        }
        return new java.util.ArrayList<>();
    }

    private Genero obtenerCrearGenero(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        for (Genero g : biblioteca.getGeneros()) {
            if (g.getNombre().equalsIgnoreCase(nombre)) {
                return g;
            }
        }
        int nuevoId = biblioteca.getGeneros().stream().mapToInt(Genero::getId).max().orElse(0) + 1;
        Genero nuevoGenero = new Genero(nuevoId, nombre);
        biblioteca.getGeneros().add(nuevoGenero);
        return nuevoGenero;
    }

    private Artista obtenerCrearArtista(String nombre, String nacionalidad) {
        if (nombre == null || nombre.isBlank()) return null;
        for (Artista a : biblioteca.getArtistas()) {
            if (a.getNombre().equalsIgnoreCase(nombre)) {
                return a;
            }
        }
        int nuevoId = biblioteca.getArtistas().stream().mapToInt(Artista::getId).max().orElse(0) + 1;
        Artista nuevoArtista = new Artista(nuevoId, nombre, nacionalidad);
        biblioteca.getArtistas().add(nuevoArtista);
        return nuevoArtista;
    }

    private Album obtenerCrearAlbum(String titulo, int anio, Artista artista, Genero genero) {
        if (titulo == null || titulo.isBlank()) return null;
        for (Album a : biblioteca.getAlbumes()) {
            if (a.getTitulo().equalsIgnoreCase(titulo)) {
                return a;
            }
        }
        int nuevoId = biblioteca.getAlbumes().stream().mapToInt(Album::getId).max().orElse(0) + 1;
        Album nuevoAlbum = new Album(titulo, anio, artista, genero);
        nuevoAlbum.setId(nuevoId);
        biblioteca.getAlbumes().add(nuevoAlbum);
        return nuevoAlbum;
    }
}
