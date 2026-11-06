package es.iesejemplo.sonoteca.repositorio.jdbc;

import es.iesejemplo.sonoteca.modelo.*;
import es.iesejemplo.sonoteca.repositorio.RepositorioSonoTeca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.sql.Types;
import java.util.ArrayList;

/**
 * UNIDAD 3 — Persistencia en base de datos relacional con JDBC.
 * <p>
 * Antes de nada, ejecuta el script {@code sonoteca.sql} proporcionado sobre tu
 * gestor (MariaDB/PostgreSQL) para crear el esquema.
 * <p>
 * Recuerda: usa siempre {@code PreparedStatement}, nunca concatenación de
 * cadenas para construir el SQL con datos introducidos por el usuario.
 */
public class RepositorioJDBC implements RepositorioSonoTeca {

    // ---------- Ejercicio 2: alta en cascada ----------

    @Override
    public void altaCancion(Cancion cancion) {
        String sqlCancion = "INSERT INTO canciones (titulo, duracion_segundos, numero_pista, album_id) VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                int generoId = obtenerOCrearGenero(conexion, cancion.getAlbum().getGenero().getNombre());

                int artistaId = obtenerOCrearArtista(conexion, cancion.getAlbum().getArtista().getNombre(),
                        cancion.getAlbum().getArtista().getNacionalidad());

                int albumId = obtenerOCrearAlbum(conexion, cancion.getAlbum(), artistaId, generoId);

                try (PreparedStatement ps = conexion.prepareStatement(sqlCancion)) {
                    ps.setString(1, cancion.getTitulo());
                    ps.setInt(2, cancion.getDuracionSegundos());
                    ps.setInt(3, cancion.getNumeroPista());
                    ps.setInt(4, albumId);
                    ps.executeUpdate();
                }

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                e.printStackTrace();
                throw new RuntimeException("Error al dar de alta la canción en cascada", e);
            } finally {
                conexion.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al conectar con la base de datos", e);
        }
    }

    // ---------- Ejercicio 3: listar con datos relacionados ----------

    private Cancion mapearCancion(ResultSet rs) throws SQLException {
        Genero genero = new Genero();
        genero.setId(rs.getInt("genero_id"));
        genero.setNombre(rs.getString("nombre_genero"));

        Artista artista = new Artista();
        artista.setId(rs.getInt("artista_id"));
        artista.setNombre(rs.getString("nombre_artista"));
        artista.setNacionalidad(rs.getString("nacionalidad"));

        Album album = new Album();
        album.setId(rs.getInt("album_id"));
        album.setTitulo(rs.getString("titulo_album"));

        int anio = rs.getInt("anio");
        if (!rs.wasNull()) {
            album.setAnio(anio);
        }

        album.setArtista(artista);
        album.setGenero(genero);

        Cancion cancion = new Cancion();
        cancion.setId(rs.getInt("cancion_id"));
        cancion.setTitulo(rs.getString("titulo_cancion"));
        cancion.setDuracionSegundos(rs.getInt("duracion_segundos"));

        int numeroPista = rs.getInt("numero_pista");
        if (!rs.wasNull()) {
            cancion.setNumeroPista(numeroPista);
        }
        cancion.setAlbum(album);

        return cancion;
    }

    @Override
    public List<Cancion> listarCanciones() {
        String sql = "SELECT " +
                "c.id AS cancion_id, c.titulo AS titulo_cancion, c.duracion_segundos, c.numero_pista, " +
                "al.id AS album_id, al.titulo AS titulo_album, al.anio, " +
                "ar.id AS artista_id, ar.nombre AS nombre_artista, ar.nacionalidad, " +
                "g.id AS genero_id, g.nombre AS nombre_genero " +
                "FROM canciones c " +
                "JOIN albumes al ON c.album_id = al.id " +
                "JOIN artistas ar ON al.artista_id = ar.id " +
                "JOIN generos g ON al.genero_id = g.id " +
                "ORDER BY ar.nombre, al.anio, c.numero_pista";

        List<Cancion> lista = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearCancion(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar canciones", e);
        }
        return lista;
    }

    /**
     * Igual que {@link #listarCanciones()}, pero devolviendo directamente la
     * información aplanada en {@link CancionCompleta} (título de la canción,
     * álbum, artista, género y año), tal como pide el ejercicio 3.
     */
    public List<CancionCompleta> listarCancionesCompletas() {
        String sql = "SELECT " +
                "c.titulo AS titulo_cancion, " +
                "al.titulo AS titulo_album, al.anio, " +
                "ar.nombre AS nombre_artista, " +
                "g.nombre AS nombre_genero " +
                "FROM canciones c " +
                "JOIN albumes al ON c.album_id = al.id " +
                "JOIN artistas ar ON al.artista_id = ar.id " +
                "JOIN generos g ON al.genero_id = g.id " +
                "ORDER BY ar.nombre, al.anio, c.numero_pista";

        List<CancionCompleta> lista = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String tituloCancion = rs.getString("titulo_cancion");
                String tituloAlbum = rs.getString("titulo_album");
                String nombreArtista = rs.getString("nombre_artista");
                String nombreGenero = rs.getString("nombre_genero");
                int anio = rs.getInt("anio");

                if (rs.wasNull()) {
                    anio = 0;
                }

                lista.add(new CancionCompleta(tituloCancion, tituloAlbum, nombreArtista, nombreGenero, anio));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar CancionCompleta", e);
        }
        return lista;
    }

    // ---------- Ejercicio 4: búsqueda parametrizada ----------

    @Override
    public List<Cancion> buscarCanciones(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return listarCanciones();
        }

        String sql = "SELECT " +
                "c.id AS cancion_id, c.titulo AS titulo_cancion, c.duracion_segundos, c.numero_pista, " +
                "al.id AS album_id, al.titulo AS titulo_album, al.anio, " +
                "ar.id AS artista_id, ar.nombre AS nombre_artista, ar.nacionalidad, " +
                "g.id AS genero_id, g.nombre AS nombre_genero " +
                "FROM canciones c " +
                "JOIN albumes al ON c.album_id = al.id " +
                "JOIN artistas ar ON al.artista_id = ar.id " +
                "JOIN generos g ON al.genero_id = g.id " +
                "WHERE c.titulo ILIKE ? OR al.titulo ILIKE ? OR ar.nombre ILIKE ? " +
                "ORDER BY ar.nombre, al.anio, c.numero_pista";

        List<Cancion> lista = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            String parametro = "%" + texto.trim() + "%";
            ps.setString(1, parametro);
            ps.setString(2, parametro);
            ps.setString(3, parametro);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCancion(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar canciones", e);
        }
        return lista;
    }

    // ---------- Ejercicio 5: actualizar / eliminar ----------

    @Override
    public void actualizarCancion(Cancion cancion) {
        throw new UnsupportedOperationException("TODO: implementar (unidad 3, ejercicio 5)");
    }

    @Override
    public void eliminarCancion(int idCancion) {
        throw new UnsupportedOperationException("TODO: implementar (unidad 3, ejercicio 5)");
    }

    /**
     * Elimina un álbum. Si tiene canciones asociadas, la base de datos rechazará
     * el borrado por la restricción de clave ajena: captura ese error y muestra
     * un mensaje claro en lugar de dejar que se propague la excepción SQL en crudo.
     */
    public void eliminarAlbum(int idAlbum) {
        throw new UnsupportedOperationException("TODO: implementar (unidad 3, ejercicio 5)");
    }

    // ---------- Ejercicio 6: importación masiva con transacción ----------

    /**
     * Importa una biblioteca completa desde un fichero JSON (unidad 1/2) dentro
     * de una única transacción: usa executeBatch() para cada tabla y, si algo
     * falla, deshaz toda la importación con rollback().
     */
    public void importarDesdeJson(String rutaFichero) {
        throw new UnsupportedOperationException("TODO: implementar (unidad 3, ejercicio 6)");
    }

    // ---------- Resto de la interfaz ----------

    @Override
    public List<Genero> listarGeneros() {
        String sql = "SELECT id, nombre FROM generos ORDER BY nombre";
        List<Genero> lista = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Genero(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar generos", e);
        }
        return lista;
    }

    @Override
    public List<Artista> listarArtistas() {
        String sql = "SELECT id, nombre, nacionalidad FROM artistas ORDER BY nombre";
        List<Artista> lista = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Artista(rs.getInt("id"), rs.getString("nombre"), rs.getString("nacionalidad")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar artistas", e);
        }
        return lista;
    }

    @Override
    public List<Album> listarAlbumes() {
        String sql = "SELECT al.id AS album_id, al.titulo AS titulo_album, al.anio, " +
                "ar.id AS artista_id, ar.nombre AS nombre_artista, ar.nacionalidad, " +
                "g.id AS genero_id, g.nombre AS nombre_genero " +
                "FROM albumes al " +
                "JOIN artistas ar ON al.artista_id = ar.id " +
                "JOIN generos g ON al.genero_id = g.id " +
                "ORDER BY al.titulo";
        List<Album> lista = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Genero g = new Genero(rs.getInt("genero_id"), rs.getString("nombre_genero"));
                Artista a = new Artista(rs.getInt("artista_id"), rs.getString("nombre_artista"), rs.getString("nacionalidad"));

                Album album = new Album();
                album.setId(rs.getInt("album_id"));
                album.setTitulo(rs.getString("titulo_album"));

                int anio = rs.getInt("anio");
                if (!rs.wasNull()) {
                    album.setAnio(anio);
                }

                album.setArtista(a);
                album.setGenero(g);

                lista.add(album);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar albumes", e);
        }
        return lista;
    }

    @Override
    public List<ListaReproduccion> listarListas() {
        String sql = "SELECT id, nombre FROM listas_reproduccion ORDER BY nombre";
        List<ListaReproduccion> lista = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ListaReproduccion lr = new ListaReproduccion(rs.getString("nombre"));
                lr.setId(rs.getInt("id"));
                lista.add(lr);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar listas de reproduccion", e);
        }
        return lista;
    }

    @Override
    public void altaLista(ListaReproduccion lista) {
        throw new UnsupportedOperationException("TODO: implementar (unidad 3)");
    }

    @Override
    public void anyadirCancionALista(int idLista, int idCancion, int posicion) {
        throw new UnsupportedOperationException("TODO: implementar (unidad 3)");
    }

    @Override
    public List<Cancion> cancionesDeLista(int idLista) {
        throw new UnsupportedOperationException("TODO: implementar (unidad 3)");
    }

    private int obtenerOCrearGenero(Connection conexion, String nombre) throws SQLException {
        String sqlSelect = "SELECT id FROM generos WHERE LOWER(nombre) = LOWER(?)";
        String sqlInsert = "INSERT INTO generos (nombre) VALUES (?) RETURNING id";

        try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        try (PreparedStatement ps = conexion.prepareStatement(sqlInsert)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("id");
            }
        }
    }

    private int obtenerOCrearArtista(Connection conexion, String nombre, String nacionalidad) throws SQLException {
        String sqlSelect = "SELECT id FROM artistas WHERE LOWER(nombre) = LOWER(?)";
        String sqlInsert = "INSERT INTO artistas (nombre, nacionalidad) VALUES (?, ?) RETURNING id";

        try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        try (PreparedStatement ps = conexion.prepareStatement(sqlInsert)) {
            ps.setString(1, nombre);
            if (nacionalidad != null && !nacionalidad.isEmpty()) {
                ps.setString(2, nacionalidad);
            } else {
                ps.setNull(2, java.sql.Types.VARCHAR);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("id");
            }
        }
    }

    private int obtenerOCrearAlbum(Connection conexion, Album album, int artistaId, int generoId) throws SQLException {
        String sqlSelect = "SELECT id FROM albumes WHERE LOWER(titulo) = LOWER(?) AND artista_id = ?";
        String sqlInsert = "INSERT INTO albumes (titulo, anio, artista_id, genero_id) VALUES (?, ?, ?, ?) RETURNING id";

        try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
            ps.setString(1, album.getTitulo());
            ps.setInt(2, artistaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        try (PreparedStatement ps = conexion.prepareStatement(sqlInsert)) {
            ps.setString(1, album.getTitulo());
            if (album.getAnio() > 0) {
                ps.setInt(2, album.getAnio());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setInt(3, artistaId);
            ps.setInt(4, generoId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("id");
            }
        }
    }
}
