package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("users")
public class User extends Model {
    public Profile getProfile() {
        return this.get(Profile.class); // Usa la clase directamente para mayor seguridad de tipo
    }

    public String getUsername() {
        return getString("username");
    }

    public void setUsername(String username) {
        set("username", username);
    }

    public String getPassword() {
        return getString("password");
    }

    public void setPassword(String password) {
        set("password", password);
    }

}
