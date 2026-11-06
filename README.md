# SonoTeca - Gestor de Biblioteca Musical

Proyecto de la asignatura de **Acceso a Datos** (2º DAM).
Esta aplicación de escritorio en Java (Swing) permite gestionar una biblioteca musical completa, incluyendo géneros, artistas, álbumes, canciones y listas de reproducción.

## 👨‍💻 Desarrollador
**CARLOS RODRIGO BELTRÁ** - Alumno de 2º DAM.

## 🚀 Tecnologías y Estado de Persistencia

El proyecto implementa distintos enfoques de persistencia de datos según las unidades del módulo:

* **Unidad 1 (JSON):** Implementada serialización y adaptadores personalizados (Gson) para tipos como `LocalDate`.
* **Unidad 2 (Ficheros):** Implementada persistencia en ficheros binarios, lectura de cabeceras ID3v1 de MP3 y sincronización en tiempo real tras modificaciones en memoria.
* **Unidad 3 (JDBC):** Integración activa con base de datos **PostgreSQL** para alta en cascada y operaciones CRUD.

## 📁 Estructura del Proyecto

- **`modelo`** — Clases del dominio (`Genero`, `Artista`, `Album`, `Cancion`, `ListaReproduccion`, `Biblioteca`).
- **`front`** — Interfaz gráfica basada en Java Swing.
- **`repositorio`** — Implementaciones de la interfaz `RepositorioSonoTeca` para los distintos orígenes de datos (Memoria, Ficheros binarios, JDBC, etc.).
- **`util`** — Adaptadores y utilidades de conversión.

## ⚙️ Cómo ejecutar la aplicación

Para compilar y lanzar el proyecto desde la terminal usando Maven:

```bash
mvn compile exec:java -Dexec.mainClass=es.iesejemplo.sonoteca.Main