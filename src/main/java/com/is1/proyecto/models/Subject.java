package com.is1.proyecto.models;

import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.javalite.activejdbc.annotations.Many2Many; // ¡Asegúrate de importar esta anotación!

@Table("subjects")
@Many2Many(other = User.class, join = "users_subjects", sourceFKName = "subject_id", targetFKName = "user_id") // Línea clave actualizada
public class Subject extends Model {

    public String getCode() {
        return getString("code");
    }

    public void setCode(String code) {
        set("code", code);
    }

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public String getDescription() {
        return getString("description");
    }

    public void setDescription(String description) {
        set("description", description);
    }

    // Opcional: Este método es para obtener todos los usuarios asociados a esta materia
    public List<User> getUsers() {
        return getAll(User.class);
    }
}
