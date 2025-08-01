package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Many2Many; // Importa Many2Many
import org.javalite.activejdbc.annotations.Table;

import java.util.List; // Para el método getProfessors()

@Table("subjects") // Mapea este modelo a la tabla 'subjects'
@Many2Many(other = Professor.class, join = "professors_subjects", sourceFKName = "subject_id", targetFKName = "professor_id")
public class Subject extends Model {

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    // Método para obtener la lista de profesores asociados a esta materia
    public List<Professor> getProfessors() {
        return getAll(Professor.class);
    }
}
