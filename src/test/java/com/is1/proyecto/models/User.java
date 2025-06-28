package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table; // Importa Table si no lo tienes

@Table("users") // Asegúrate de que esta anotación esté presente y apunte a tu tabla "users"
public class User extends Model {

    // Si tenías un método setName, cámbialo a setUsername
    public void setUsername(String username) {
        set("username", username); // <-- Aquí el cambio clave: "name" a "username"
    }

    // Si tenías un método getName, cámbialo a getUsername
    public String getUsername() {
        return getString("username"); // <-- Aquí el cambio clave: "name" a "username"
    }

    // Asegúrate de que los demás setters/getters para email, password_hash, is_active, admin
    // también usen los nombres correctos de los atributos de la base de datos.
    public void setEmail(String email) {
        set("email", email);
    }

    public String getEmail() {
        return getString("email");
    }

    public void setPasswordHash(String passwordHash) {
        set("password_hash", passwordHash); // Coherente con password_hash en la DB
    }

    public String getPasswordHash() {
        return getString("password_hash");
    }

    public void setIsActive(boolean isActive) {
        set("is_active", isActive);
    }

    public boolean getIsActive() {
        return getBoolean("is_active");
    }

    public void setAdmin(boolean admin) {
        set("admin", admin);
    }

    public boolean getAdmin() {
        return getBoolean("admin");
    }

    // ... otros métodos de la clase User
}
