package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.BelongsTo; // Importa BelongsTo

// Similar al modelo Professor, la anotación BelongsTo es para la claridad
// y para asegurar que ActiveJDBC reconozca la relación con la tabla User.
@BelongsTo(parent = User.class, foreignKeyName = "user_id")
public class Student extends Model {

    // --- Métodos de Conveniencia (Getters y Setters) ---

    public String getFirstName() {
        return getString("first_name");
    }

    public void setFirstName(String firstName) {
        set("first_name", firstName);
    }

    public String getLastName() {
        return getString("last_name");
    }

    public void setLastName(String lastName) {
        set("last_name", lastName);
    }

    public String getStudentIdNumber() {
        return getString("student_id_number");
    }

    public void setStudentIdNumber(String studentIdNumber) {
        set("student_id_number", studentIdNumber);
    }

    public String getCareerName() {
        return getString("career_name");
    }

    public void setCareerName(String careerName) {
        set("career_name", careerName);
    }

    public Long getUserId() {
        return getLong("user_id");
    }

    public void setUserId(Long userId) {
        set("user_id", userId);
    }

    // --- Relaciones ---
    // ActiveJDBC manejará automáticamente la relación One-to-One con User.
    // Para acceder al User asociado:
    // User associatedUser = studentInstance.parent(User.class);
}