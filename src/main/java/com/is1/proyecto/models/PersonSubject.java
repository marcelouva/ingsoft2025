package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("people_subjects")
public class PersonSubject extends Model {

  
    public void setPerson(Person person) {
        set("person_id", person.getId());
    }

    public void setSubject(Subject subject) {
        set("subject_id", subject.getId());
    }

    public Person getPerson() {
        return Person.findById(get("person_id"));
    }

    public Subject getSubject() {
        return Subject.findById(get("subject_id"));
    }
}
