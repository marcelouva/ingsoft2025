# Base de datos en SQLite

Este documento explica cómo configurar y preparar la base de datos SQLite para el proyecto.

## Configuración de la Base de Datos SQLite

Para que la aplicación funcione correctamente, es necesario tener la base de datos SQLite configurada con la estructura de tablas adecuada. 

### 1. Instalación de SQLite 

Antes de ejecutar el script de la base de datos, asegúrate de tener la herramienta de línea de comandos de SQLite (`sqlite3`) instalada en tu sistema.

* **En sistemas basados en Debian/Ubuntu (Linux):**
    Generalmente, SQLite ya está preinstalado. Si no lo está, o para asegurarte, puedes instalarlo con:
    ```bash
    sudo apt-get update
    sudo apt-get install sqlite3
    ```

* **En macOS (usando Homebrew):**
    ```bash
    brew install sqlite3
    ```

* **En Windows:**
    1.  Descarga los binarios precompilados desde el sitio oficial de SQLite: [https://www.sqlite.org/download.html](https://www.sqlite.org/download.html). Busca la sección "Precompiled Binaries for Windows" y descarga el archivo `sqlite-tools-win-x64-XXXXXXX.zip` (o la versión adecuada para tu arquitectura).
    2.  Descomprime el archivo `.zip` en una ubicación de fácil acceso (por ejemplo, `C:\sqlite`).
    3.  Para usar `sqlite3` desde cualquier directorio en tu terminal, considera añadir la ruta de la carpeta donde lo descomprimiste (ej. `C:\sqlite`) a la variable de entorno `PATH` de tu sistema.

### 2. Creación del Archivo de Base de Datos y Ejecución del Script de Tablas

Con la herramienta `sqlite3` instalada, puedes proceder a crear el archivo de la base de datos y su esquema:

1.  **Navega al directorio raíz de tu proyecto Java** en tu terminal. Este es el directorio donde se encuentran carpetas como `src`, `pom.xml`, etc. Es importante que el archivo `mi_proyecto.db` (el nombre de tu base de datos SQLite, configurado en `DBConfigSingleton.java`) se cree en esta ubicación o en una ruta relativa desde aquí.

2.  **Crea el archivo de la base de datos SQLite (si no existe)**:
    Puedes ejecutar tu aplicación Java al menos una vez para que ActiveJDBC intente conectarse y cree el archivo `mi_proyecto.db` automáticamente. Luego puedes detener la aplicación. Alternativamente, puedes simplemente ejecutar:
    ```bash
    sqlite3 mi_proyecto.db ".quit"
    ```
    Esto creará un archivo `mi_proyecto.db` vacío si no existe.

3.  **Ejecuta el script SQL para crear las tablas:**
    Una vez que tengas el archivo `mi_proyecto.db` y estés en la terminal en el directorio raíz de tu proyecto, ejecuta el siguiente comando:
    ```bash
    sqlite3 mi_proyecto.db ".read src/main/resources/scheme.sql"
    ```
    * Este comando abre la base de datos `mi_proyecto.db`.
    * El comando interno `.read src/main/resources/scheme.sql` le indica a SQLite que ejecute todas las sentencias SQL contenidas en tu archivo `scheme.sql`.
    * Tu `scheme.sql` debe incluir `DROP TABLE IF EXISTS accounts;` antes de `CREATE TABLE accounts (...)` para permitir re-ejecuciones sin errores en desarrollo.

4.  **Verificación (Opcional): Consultar las tablas creadas**
    Para confirmar que las tablas se crearon correctamente y para probar algunas consultas, puedes volver a abrir la base de datos con `sqlite3` y usar sus comandos:
    ```bash
    sqlite3 mi_proyecto.db
    ```
    Una vez dentro del prompt `sqlite3>`, puedes usar:
    * `.tables` - Para listar todas las tablas en la base de datos.
    * `.schema accounts` - Para ver la definición (esquema) de la tabla `accounts`.
    * `SELECT * FROM accounts;` - Para consultar todos los registros de la tabla `accounts`. (¡No olvides el punto y coma al final de las sentencias SQL!)
    * `.quit` - Para salir del prompt de SQLite.

---

**Consideraciones Adicionales para tu `App.java` (si has seguido mi sugerencia anterior):**

Si has incluido la lógica para ejecutar `scheme.sql` al inicio de tu `App.java`, este paso manual (Paso 2) se vuelve opcional. La aplicación lo hará por ti la primera vez que se ejecute y cada vez que inicies la aplicación (lo cual es conveniente para desarrollo, pero ten en cuenta que borrará y recreará las tablas cada vez si tu `scheme.sql` incluye el `DROP TABLE IF EXISTS`). Para producción, se usaría solo la ejecución manual o una herramienta de migración.