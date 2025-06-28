package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("people")
public class Person extends Model {

    // Define la relación inversa: una persona pertenece a un usuario.
    // ActiveJDBC usará el campo 'user_id' para encontrar el usuario relacionado.
    public User getUser() {
        return this.parent(User.class); // 'parent' es equivalente a 'belongsTo'
    }

    // Métodos de conveniencia para acceder a los campos
    public String getName() {
        return getString("name");
    }

    public String getDni() {
        return getString("dni"); // Método para acceder al DNI
    }

    public String getBirthDate() {
        return getString("birth_date");
    }

    // Puedes añadir lógica de validación o negocio aquí si es necesario
    // Por ejemplo:
    // public void setDni(String dni) {
    //     if (dni == null || dni.trim().isEmpty()) {
    //         throw new IllegalArgumentException("DNI cannot be empty");
    //     }
    //     // Agrega más validación de formato aquí si es necesario
    //     set("dni", dni);
    // }
}