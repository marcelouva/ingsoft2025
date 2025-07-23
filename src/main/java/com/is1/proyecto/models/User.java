package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.HasMany;
import org.javalite.activejdbc.annotations.Table;


//@HasMany(child = Profile.class, foreignKeyName = "user_id") // importa HasMany
@Table("users")
public class User extends Model {
    
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

    public Profile getProfile() {
        return Profile.findFirst("user_id = ?", getId());
    }


    public void setProfile(Profile newProfile) {
        Profile current = getProfile();
       if (current != null && !current.equals(newProfile)) {
             current.delete();  // o current.set("user_id", null).saveIt() si querés preservarlo
       }
       newProfile.set("user_id", getId());
       newProfile.saveIt();
    }


}
