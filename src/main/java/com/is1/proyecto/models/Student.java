package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.BelongsTo; // Importa BelongsTo

// Similar al modelo Professor, la anotación BelongsTo es para la claridad
// y para asegurar que ActiveJDBC reconozca la relación con la tabla User.
@BelongsTo(parent = User.class, foreignKeyName = "user_id")
public class Student extends Model {

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public String getLastName() {
        return getString("last_name");
    }

    public void setLastName(String lastName) {
        set("last_name", lastName);
    }

    public Integer getStudentIdNumber() {
        return getInteger("student_id");
    }

    public void setStudentIdNumber(String student_id) {
        set("student_id_number", student_id);
    }

    public String getCareerName() {
        return getString("career_name");
    }

    public void setCareerName(String careerName) {
        set("career_name", careerName);
    }

  
    public Integer getYearOfEntry() {
        return getInteger("year_of_entry");
    }

    public void setYearOfEntry(Integer year_of_entry) {
        set("year_of_entry", year_of_entry);
    }


    public Long getUserId() {
        return getLong("user_id");
    }

    public void setUserId(Long userId) {
        set("user_id", userId);
    }

    public User getUser(){
        return(parent(User.class));
    }
    
    // --- Relaciones ---
    // ActiveJDBC manejará automáticamente la relación One-to-One con User.
    // Para acceder al User asociado:
    // User associatedUser = studentInstance.parent(User.class);
}