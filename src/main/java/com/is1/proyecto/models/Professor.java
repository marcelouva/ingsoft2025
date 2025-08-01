package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.BelongsTo;
import org.javalite.activejdbc.annotations.Many2Many; // Asegúrate de que esta importación esté

import java.util.List; // Para el método getSubjects()

@BelongsTo(parent = User.class, foreignKeyName = "user_id")
// Relación N:N con Subject a través de la tabla de unión professors_subjects
@Many2Many(other = Subject.class, join = "professors_subjects", sourceFKName = "professor_id", targetFKName = "subject_id")
public class Professor extends Model {

    public String getName() {
        return getString("name");
    }

    // CORREGIDO: Ahora busca la columna 'last_name'
    public String getLastName() {
        return getString("last_name");
    }

    public String getEmail() {
        return getString("email");
    }

    // CORREGIDO: Ahora busca la columna 'id_employee' y el tipo de retorno es String
    public String getIdEmployee() { // Cambiado el nombre del método para reflejar 'id_employee'
        return getString("id_employee");
    }

    public void setName(String name) {
        set("name", name);
    }

    // CORREGIDO: Setter para 'last_name'
    public void setLastName(String lastName) {
        set("last_name", lastName);
    }

    public void setEmail(String email) {
        set("email", email);
    }

    // CORREGIDO: Setter para 'id_employee'
    public void setIdEmployee(String idEmployee) {
        set("id_employee", idEmployee);
    }

    public void setUserId(Long userId) {
        set("user_id", userId);
    }

    // Método para obtener el User asociado (relación 1:1)
    public User getUser() {
        return parent(User.class);
    }

    // Método para obtener la lista de materias asociadas a este profesor (relación N:N)
    public List<Subject> getSubjects() {
        return getAll(Subject.class);
    }

    // Puedes añadir otros métodos de conveniencia si los necesitas
}
