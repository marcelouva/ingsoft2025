package com.is1.proyecto.models;

import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.javalite.activejdbc.annotations.Many2Many; // ¡Asegúrate de importar esta anotación!

@Table("users")
@Many2Many(other = Subject.class, join = "users_subjects", sourceFKName = "user_id", targetFKName = "subject_id") // Línea clave actualizada
public class User extends Model {

    public Person getPerson() {
        return Person.findFirst("user_id = ?", this.getId());
    }

    public void setPerson(Person person) {
        this.add(person);
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

    // Este método es para obtener todas las materias asociadas a este usuario
    public List<Subject> getSubjects() {
        return getAll(Subject.class);
    }

    // Este método es para asociar una materia a este usuario
    // ActiveJDBC usará la tabla de unión "users_subjects" automáticamente
    public void addSubject(Subject subject) {
        this.add(subject);
    }

    // Opcional: Método para desasociar una materia de este usuario
    public void removeSubject(Subject subject) {
        this.remove(subject);
    }
}
