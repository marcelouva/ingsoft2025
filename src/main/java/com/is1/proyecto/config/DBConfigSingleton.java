// Archivo: com/is1/proyecto/config/DBConfigSingleton.java
package com.is1.proyecto.config;

public final class DBConfigSingleton {

    private static DBConfigSingleton instance;

    private final String dbUrl;
    private final String user;
    private final String pass;
    private final String driver;

    // Constructor privado para evitar instanciación directa
    private DBConfigSingleton() {
        // Idealmente, estas credenciales deberían venir de un archivo de configuración,
        // variables de entorno, o un servicio de secretos en producción.
        this.driver = "com.mysql.cj.jdbc.Driver"; // Opcional, ActiveJDBC lo puede deducir
        this.dbUrl = "jdbc:mysql://localhost:3306/academica_db";
        this.user = "muva";
        this.pass = "muva";
    }

    public static synchronized DBConfigSingleton getInstance() {
        if (instance == null) {
            instance = new DBConfigSingleton();
        }
        return instance;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public String getDriver() {
        return driver;
    }
}