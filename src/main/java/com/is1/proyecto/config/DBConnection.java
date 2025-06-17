package com.is1.proyecto.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Idealmente, estas credenciales deberían venir de un archivo de configuración,
    // variables de entorno, o un servicio de secretos en producción.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/students_db";
    private static final String USER = "muva";
    private static final String PASS = "muva";

    // NOTA: Para una aplicación web real, se recomienda usar un Pool de Conexiones
    // (HikariCP, Apache DBCP) en lugar de una única conexión estática.
    // Esto es una simplificación para empezar.

    public static Connection getConnection() throws SQLException {
        // Cada vez que se pide una conexión, la creamos y la devolvemos.
        // Si usaras un pool, el pool gestionaría la obtención/creación.
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // Ya no necesitamos un closeConnection estático si abrimos y cerramos por cada request
    // o si usamos un pool que gestiona el cierre.
    // Las conexiones obtenidas del pool se "devuelven" al pool.
    // Aquí, simplemente cerramos la que obtuvimos.
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}