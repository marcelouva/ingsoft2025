package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("people")
public class Person extends Model {

    public User getUser() {
        // Usa 'parent()' para obtener el User al que pertenece esta Person.
        // ActiveJDBC usará el 'user_id' de la tabla 'people'.
        return this.parent(User.class);
    }

    // Método para asociar esta Person a un User
    public void setUser(User user) {
        this.setParent(user); // Esto establecerá el 'user_id' en esta persona
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

    public String getBirthDate() {
        return getString("birth_date");
    }

    public void setBirthDate(String birthDate) {
        set("birth_date", birthDate);
    }
}