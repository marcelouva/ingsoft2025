package com.is1.proyecto.config; 

import org.javalite.activejdbc.Base;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Clase de utilidad para inicializar el esquema de la base de datos
 * de forma independiente a la aplicación principal.
 */
public class DBInitializer {

    private static final String SCHEMA_SQL_FILE = "scheme.sql";

    public static void main(String[] args) {
        System.out.println("Iniciando la inicialización de la base de datos a través de Maven...");
        // Como DBInitializer está en el mismo paquete que DBConfigSingleton,
        // no necesitas la importación completa si ya está en el mismo paquete,
        // pero para mayor claridad y si en el futuro se movieran, se mantiene.
        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance(); 

        try {
            // Abrimos la conexión a la base de datos
            Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());

            // Verificamos si la tabla 'users' ya existe
            Integer tableCount = (Integer) Base.firstCell("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='users'");
            boolean usersTableExists = (tableCount != null && tableCount > 0);

            if (!usersTableExists) {
                System.out.println("Base de datos: Tabla 'users' no encontrada. Ejecutando esquema...");
                runSchemaScript();
                System.out.println("Base de datos: Esquema ejecutado correctamente.");
            } else {
                System.out.println("Base de datos: La tabla 'users' ya existe. No se requiere inicialización del esquema.");
            }

        } catch (Exception e) {
            System.err.println("Error durante la inicialización de la base de datos: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // Salir con código de error si falla la inicialización crítica
        } finally {
            // Asegurarse de cerrar la conexión de la base de datos
            try {
                Base.close(); 
                System.out.println("Base de datos: Conexión cerrada.");
            } catch (Exception e) {
                System.err.println("Error al cerrar conexión de la base de datos: " + e.getMessage());
            }
        }
        System.out.println("Proceso de inicialización de la base de datos finalizado.");
    }

    /**
     * Lee el archivo scheme.sql y ejecuta sus sentencias.
     * Asume que Base.open() ya fue llamado y una conexión está disponible.
     */
    private static void runSchemaScript() throws IOException, SQLException {
        InputStream is = DBInitializer.class.getClassLoader().getResourceAsStream(SCHEMA_SQL_FILE);
        if (is == null) {
            throw new IOException("Archivo de esquema SQL no encontrado en los recursos: " + SCHEMA_SQL_FILE);
        }

        String schemaSql;
        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            schemaSql = scanner.useDelimiter("\\A").next();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar InputStream de scheme.sql: " + e.getMessage());
                }
            }
        }

        Connection connection = Base.connection();
        try (Statement statement = connection.createStatement()) {
            statement.execute(schemaSql);
        }
    }
}