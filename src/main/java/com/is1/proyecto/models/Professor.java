package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.BelongsTo; // Importa BelongsTo

@BelongsTo(parent = User.class, foreignKeyName = "user_id")
public class Professor extends Model {

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public String getEmail() {
        return getString("email");
    }

    public void setEmail(String email) {
        set("email", email);
    }

    
    public String getLastName() {
        return getString("last_name");
    }

    public void setLastName(String email) {
        set("last_name", email);
    }




    public Integer getIdEmployee() {
        return getInteger("id_employee");
    }


    public void setUserId(Long userId) {
        set("user_id", userId);
    }


    public Integer getUserId() {
        return getInteger("user_id");
    }


 public User getUser() {
        return parent(User.class);
    }

 
}