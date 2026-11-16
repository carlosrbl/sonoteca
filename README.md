# SonoTeca - Gestor de Biblioteca Musical

Proyecto de la asignatura de **Acceso a Datos** (2º DAM).
Esta aplicación de escritorio en Java (Swing) permite gestionar una biblioteca musical completa, incluyendo géneros, artistas, álbumes, canciones y listas de reproducción.

## 👨‍💻 Desarrollador
**CARLOS RODRIGO BELTRÁ** - Alumno de 2º DAM.

## 🚀 Tecnologías y Estado de Persistencia

El proyecto implementa distintos enfoques de persistencia de datos según las unidades del módulo:

* **Unidad 1 (JSON):** Implementada serialización y adaptadores personalizados (Gson) para tipos como `LocalDate` y estructuras complejas.
* **Unidad 2 (Ficheros):** Implementada persistencia en ficheros binarios, lectura de cabeceras ID3v1 de MP3 y sincronización en tiempo real tras modificaciones en memoria.
* **Unidad 3 (JDBC - PostgreSQL):** Integración activa con base de datos **PostgreSQL 18** mediante JDBC:
  * **Ejercicio 1:** Conexión mediante `config.properties` y gestión con `ConexionBD`.
  * **Ejercicio 2:** Alta en cascada de canciones, creando/vinculando automáticamente género, artista y álbum si no existen.
  * **Ejercicio 3:** Consultas relacionales avanzadas con `JOIN` (`listarCancionesCompletas`) mapeadas a DTOs.
  * **Ejercicio 4:** Búsquedas parametrizadas seguras mediante `PreparedStatement` e `ILIKE`.
  * **Ejercicio 5:** Operaciones de actualización y borrado con captura y tratamiento de excepciones de restricción de clave ajena (`SQLState 23503`).
  * **Ejercicio 6:** Importación masiva e integral desde ficheros JSON procesada en **lote** (`addBatch` / `executeBatch`) y delimitada dentro de una **única transacción** (`setAutoCommit(false)`, `commit()`, `rollback()`), incluyendo vaciado preventivo y sincronización automática de secuencias de PostgreSQL (`setval`).

---

## 🗄️ Esquema de la Base de Datos (Unidad 3 - PostgreSQL)

La persistencia relacional se apoya en el script `sonoteca.sql`, con las siguientes tablas e índices:

* **`generos`**: Contiene la lista de géneros musicales (`id`, `nombre` UNIQUE).
* **`artistas`**: Registra los intérpretes/grupos (`id`, `nombre`, `nacionalidad`).
* **`albumes`**: Almacena producciones discográficas (`id`, `titulo`, `anio`, `artista_id`, `genero_id`) con FK a `artistas` y `generos`.
* **`canciones`**: Registra las pistas musicales (`id`, `titulo`, `duracion_segundos`, `numero_pista`, `album_id`) con FK a `albumes`.
* **`listas_reproduccion`** y **`lista_cancion`**: Tablas de soporte para listas musicales (N:M).
* **Índices de rendimiento**: `idx_albumes_artista`, `idx_canciones_album`, `idx_canciones_titulo`.

---

## 📁 Estructura del Proyecto

```text
es.iesejemplo.sonoteca
├── modelo          # Entidades del dominio (Genero, Artista, Album, Cancion, ListaReproduccion, Biblioteca, CancionCompleta)
├── repositorio     # Interfaz RepositorioSonoTeca e implementaciones de persistencia:
│   └── jdbc        # RepositorioJDBC, ConexionBD (Persistencia relacional con PostgreSQL)
├── util            # Utilidades de conversión y serialización (JsonUtils, etc.)
└── front           # Interfaz gráfica de usuario basada en Java Swing
```

---

## ⚙️ Configuración y Ejecución

### 1. Requisitos Previos
* Java JDK 17 o superior.
* Apache Maven 3.x.
* Servidor PostgreSQL activo.

### 2. Configuración de la Base de Datos
1. Crea la base de datos `sonoteca` y el usuario correspondiente en PostgreSQL.
2. Ejecuta el script SQL para inicializar el esquema:
   ```bash
   psql -h localhost -U sonoteca -d sonoteca -f sonoteca.sql
   ```
3. Configura tus credenciales de acceso. Copia la plantilla de ejemplo `src/main/resources/config.properties.ejemplo` como `config.properties` y ajusta los parámetros de conexión:
   ```properties
   bd.url=jdbc:postgresql://localhost:5432/sonoteca
   bd.usuario=sonoteca
   bd.password=tu_contrasena
   ```

### 3. Compilar y Ejecutar
Para compilar el proyecto y lanzar la aplicación GUI desde la terminal usando Maven:

```bash
mvn compile exec:java -Dexec.mainClass=es.iesejemplo.sonoteca.Main
```
