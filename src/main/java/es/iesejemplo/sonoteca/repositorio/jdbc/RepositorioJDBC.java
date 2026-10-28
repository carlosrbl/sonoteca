package es.iesejemplo.sonoteca.repositorio.jdbc;

import es.iesejemplo.sonoteca.modelo.*;
import es.iesejemplo.sonoteca.repositorio.RepositorioSonoTeca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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

    @Override
    public List<Cancion> listarCanciones() {
        // TODO: consulta que combine canciones + álbumes + artistas + géneros
        // (con JOIN en el SQL, o con varias consultas encadenadas).
        throw new UnsupportedOperationException("TODO: implementar (unidad 3, ejercicio 3)");
    }

    /**
     * Igual que {@link #listarCanciones()}, pero devolviendo directamente la
     * información aplanada en {@link CancionCompleta} (título de la canción,
     * álbum, artista, género y año), tal como pide el ejercicio 3.
     */
    public List<CancionCompleta> listarCancionesCompletas() {
        // TODO: implementar con una consulta JOIN (o varias consultas encadenadas),
        // construyendo un CancionCompleta por cada fila del resultado.
        throw new UnsupportedOperationException("TODO: implementar (unidad 3, ejercicio 3)");
    }

    // ---------- Ejercicio 4: búsqueda parametrizada ----------

    @Override
    public List<Cancion> buscarCanciones(String texto) {
        // TODO: PreparedStatement con LIKE sobre título/artista/álbum.
        throw new UnsupportedOperationException("TODO: implementar (unidad 3, ejercicio 4)");
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
        throw new UnsupportedOperationException("TODO: implementar (unidad 3)");
    }

    @Override
    public List<Artista> listarArtistas() {
        throw new UnsupportedOperationException("TODO: implementar (unidad 3)");
    }

    @Override
    public List<Album> listarAlbumes() {
        throw new UnsupportedOperationException("TODO: implementar (unidad 3)");
    }

    @Override
    public List<ListaReproduccion> listarListas() {
        throw new UnsupportedOperationException("TODO: implementar (unidad 3)");
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
