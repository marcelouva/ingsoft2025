package com.is1.proyecto.models;

import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("users")
public class User extends Model {

    public Person getPerson() {
        // Opción 1: Usar findFirst en Person para buscar por user_id
        // El 'id' de este User es la clave foránea 'user_id' en la tabla 'people'.
        return Person.findFirst("user_id = ?", this.getId());
    }

    // Método para asociar una Person a este User
    public void setPerson(Person person) {
        // Cuando haces add(person), ActiveJDBC asignará el id de este User
        // al campo 'user_id' de 'person' internamente.
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



    public List<Subject> getSubjects() {
        return getAll(Subject.class);
    }

    // Opcionalmente, un método para añadir una materia a este usuario
    public void addSubject(Subject subject) {
        add(subject); // ActiveJDBC automáticamente establecerá el user_id en la materia
    }






}