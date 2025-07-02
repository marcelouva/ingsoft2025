package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.BelongsTo;
import org.javalite.activejdbc.annotations.Table;

@Table("students")
@BelongsTo(parent = Person.class, foreignKeyName = "id") // 'id' de students referencia a 'id' de people
public class Student extends Model {

    
    public Student() {
    }

    public String getStudentCode() {
        return getString("student_code");
    }

    public void setStudentCode(String studentCode) {
        set("student_code", studentCode);
    }

    // Obtener la Person asociada a este Student
    public Person getPerson() {
        // El 'id' de Student es el mismo 'id' que en la tabla people
        return Person.findById(getId());
    }

    // --- Métodos para crear y manejar un Student completo (Person + Student) ---
    // ActiveJDBC no maneja esto automáticamente en TPS, lo haremos manualmente
    public static Student createStudent(String name, String dni, String birthDate, Integer userId, String studentCode) {
        // 1. Crear la entrada en la tabla 'people'
        Person person = new Person();
        person.set("name", name, "dni", dni, "birth_date", birthDate, "user_id", userId);
        person.saveIt();

        // 2. Crear la entrada en la tabla 'students', usando el mismo ID de la persona
        Student student = new Student();
        student.set("id", person.getId(), "student_code", studentCode); // Aquí usamos el ID de la persona
        student.saveIt();

        return student;
    }

    // --- Otros métodos de ejemplo para cargar ---
    public static Student findStudentById(Long id) {
        return Student.findById(id);
    }
}