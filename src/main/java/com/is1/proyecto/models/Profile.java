package com.is1.proyecto.models;


import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.BelongsTo;
import org.javalite.activejdbc.annotations.Table;


//@BelongsTo(parent = User.class)      // importa org.javalite.activejdbc.annotations.BelongsTo
@Table("profiles")

public class Profile extends Model {

    public String getEmail() {
        return getString("email");
    }

    public void setEmail(String email) {
        set("email", email);
    }
    public String getName() {
        return getString("name");
    }
    public void setName(String name) {
        set("name", name);
    }

    public String getDni() {
        return getString("dni");
    }

    public void setDni(String dni) {
        set("dni", dni);
    }

    public Long getUserId() {
        return getLong("user_id");
    }

    public void setUserId(Long userId) {
        set("user_id", userId);
    }

    // --- Métodos de Navegación de Relación ---

    public User getUser() {
        return this.parent(User.class);
    }

}







