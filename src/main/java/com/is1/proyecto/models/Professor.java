package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.BelongsTo; // Importa BelongsTo

@BelongsTo(parent = User.class, foreignKeyName = "user_id")
public class Professor extends Model {

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

    public String getEmployeeIdNumber() {
        return getString("employee_id_number");
    }

    public void setEmployeeIdNumber(String employeeIdNumber) {
        set("employee_id_number", employeeIdNumber);
    }

    public String getDepartment() {
        return getString("department");
    }

    public void setDepartment(String department) {
        set("department", department);
    }

    public Long getUserId() {
        return getLong("user_id");
    }

    public void setUserId(Long userId) {
        set("user_id", userId);
    }

 
}