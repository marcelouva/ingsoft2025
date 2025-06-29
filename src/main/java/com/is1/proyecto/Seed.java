package com.is1.proyecto;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import com.is1.proyecto.models.Subject;
import com.is1.proyecto.config.DBConfigSingleton; // Importar el Singleton de configuración de DB

public class Seed {

    public static void main(String[] args) {
        // Obtener la instancia del Singleton de configuración de DB
        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();
        Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());

            // Materia: Matemática
            Subject mat = Subject.findOrCreateIt("code = ?", "MAT101");
            mat.set("name", "Matemática", "description", "Primer año").saveIt();
            System.out.println("Materia creada/actualizada: " + mat.getString("name"));
        Base.close();
    }
            
}
