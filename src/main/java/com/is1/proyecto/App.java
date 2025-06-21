package com.is1.proyecto; // Asegúrate de que el paquete coincide con tu groupId y estructura de carpetas

import com.fasterxml.jackson.databind.ObjectMapper; // Necesario para convertir objetos Java a JSON

import static spark.Spark.*; // Importa los métodos estáticos de Spark (get, post, before, after, etc.)

import org.eclipse.jetty.server.Authentication.User;
// Importaciones de ActiveJDBC
import org.javalite.activejdbc.Base; // Clase central de ActiveJDBC para gestión de DB
import org.mindrot.jbcrypt.BCrypt;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.Map; // Para usar Map.of() en las respuestas JSON


import com.is1.proyecto.config.DBConfigSingleton; // Importa el singleton
import com.is1.proyecto.models.Account;


public class App {

    // ObjectMapper es una clase de Jackson para serializar/deserializar JSON.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        port(8080); // La aplicación Spark escuchará en el puerto 8080

      


        // Obtener la instancia del singleton de configuración
        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

        before((req, res) -> {
            try {
                // Usar las credenciales del singleton
                Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());
            } catch (Exception e) {
                System.err.println("Error al abrir conexión con ActiveJDBC: " + e.getMessage());
                halt(500, "{\"error\": \"Error interno del servidor: Fallo al conectar a la base de datos.\"}" + e.getMessage());
            }
        });

        after((req, res) -> {
            try {
                Base.close();
            } catch (Exception e) {
                System.err.println("Error al cerrar conexión con ActiveJDBC: " + e.getMessage());
            }
        });



get("/account/create", (req, res) -> {
    Map<String, Object> model = new HashMap<>();

    // Obtener mensaje de éxito de los query parameters
    String successMessage = req.queryParams("message");
    if (successMessage != null && !successMessage.isEmpty()) {
        model.put("successMessage", successMessage);
    }

    // Obtener mensaje de error de los query parameters
    String errorMessage = req.queryParams("error");
    if (errorMessage != null && !errorMessage.isEmpty()) {
        model.put("errorMessage", errorMessage);
    }

    // Puedes añadir otros datos al modelo si es necesario (ej. lista de equipos)

    // Renderiza la plantilla 'account_form.mustache' con los datos del modelo
    return new ModelAndView(model, "account_form.mustache");
}, new MustacheTemplateEngine());








  get("/dashboard", (req, res) -> {
    Map<String, Object> model = new HashMap<>();

            // Obtener el nombre de usuario de la sesión
    String currentUsername = req.session().attribute("currentAccountUsername");
    Boolean loggedIn = req.session().attribute("loggedIn"); // Obtener la bandera de logueado

            // 1. Verificar si el usuario ha iniciado sesión
    if (currentUsername == null || loggedIn == null || !loggedIn) {
                // Si no hay un nombre de usuario en la sesión o la bandera es falsa,
                // significa que el usuario no está logueado o su sesión expiró.
        System.out.println("DEBUG: Acceso no autorizado a /dashboard. Redirigiendo a /login.");
        res.redirect("/login?error=Debes iniciar sesión para acceder a esta página.");
        return null; // Importante retornar null después de una redirección
    }

            // 2. Si el usuario está logueado, añadir el nombre de usuario al modelo
    model.put("username", currentUsername);

            // 3. Renderizar la plantilla del dashboard
    return new ModelAndView(model, "dashboard.mustache");
}, new MustacheTemplateEngine());


  // Ruta para cerrar sesión
        get("/logout", (req, res) -> {
            // Invalidar completamente la sesión
            // Esto elimina todos los atributos guardados en la sesión y la marca como inválida.
            // La cookie JSESSIONID en el navegador del usuario también se eliminará o marcará como expirada.
            req.session().invalidate();

            System.out.println("DEBUG: Sesión cerrada. Redirigiendo a /login.");

            // Redirigir al usuario a la página de login (o a la página principal)
            // Puedes añadir un parámetro de consulta 'message' para mostrar un mensaje en el login.
            res.redirect("/login?message=Has cerrado sesión exitosamente.");

            // Es importante retornar null después de una redirección
            return null;
        });






        // --- Endpoint de prueba simple (ruta raíz) ---
        get("/login", (req, res) -> {
                       return new ModelAndView(null, "login.mustache");
        }, new MustacheTemplateEngine());

       get("/account/new", (req, res) -> {
                       return new ModelAndView(null, "account_form.mustache");
        }, new MustacheTemplateEngine());




        post("/account/new",(req,res) ->{
            String name = req.queryParams("name");
            String password = req.queryParams("password");
            // Validaciones básicas
            if(name==null || name.isEmpty() || password==null || password.isEmpty()){
                res.status(400); // Código de estado HTTP 400 (Bad Request)
                       return new ModelAndView(null, "error.mustache");
            }


            try {
                // --- Creación y guardado del usuario usando el modelo ActiveJDBC ---
                Account ac = new Account(); // Crea una nueva instancia del modelo Account.
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

              
                ac.set("name", name); // Asigna el nombre al campo 'name'.
                ac.set("password", hashedPassword); // Asigna la contraseña al campo 'password'.
                ac.saveIt(); // Guarda el nuevo usuario en la tabla 'users'.

                res.status(201); // Código de estado HTTP 201 (Created) para una creación exitosa.
                // Devuelve una respuesta JSON con el mensaje y el ID del nuevo usuario.
                res.redirect("/account/create?message=Cuenta creada exitosamente para " + name + "!");
            return "";    



            } catch (Exception e) {
                // Si ocurre cualquier error durante la operación de DB, se captura aquí.
                System.err.println("Error al registrar la cuenta: " + e.getMessage());
                e.printStackTrace(); // Imprime el stack trace para depuración.
                res.status(500); // Código de estado HTTP 500 (Internal Server Error).
                res.redirect("/account/create?error=Error interno al crear la cuenta. Intente de nuevo.");
                return ""; 
            }
            
        });


        



        post("/login", (req, res) -> {
            Map<String, Object> model = new HashMap<>();


             String username = req.queryParams("username");
             String plainTextPassword = req.queryParams("password");

        // 1. Buscar la cuenta en la base de datos
             Account ac = Account.findFirst("name = ?", username);

             if (ac == null) {
                   res.status(401); // Unauthorized
                   model.put("errorMessage", "Por favor, ingresa tu nombre de usuario y contraseña."); // Set error message
                   return new ModelAndView(model, "login.mustache"); // Render template with error
            
            }

    // 2. Obtener la contraseña hasheada almacenada
    String storedHashedPassword = ac.getString("password");

    // 3. Comparar la contraseña ingresada con la hasheada
    // BCrypt.checkpw(plainTextPassword, storedHashedPassword)
    // Esto hashea la plainTextPassword con el salt extraído de storedHashedPassword
    // y luego compara los dos hashes.
    if (BCrypt.checkpw(plainTextPassword, storedHashedPassword)) {
        res.status(200); // OK



        req.session(true).attribute("currentAccountUsername", username); // Guarda el nombre de usuario
        req.session().attribute("accountId", ac.getId()); // Guarda el ID de la cuenta (útil para consultas de DB)
        req.session().attribute("loggedIn", true); // Una bandera para verificar rápidamente si está logueado




        model.put("username",username); 
        return new ModelAndView(model, "dashboard.mustache"); // Render template with error
    } else {

        model.put("errorMessage", "Acceso no autorizado."); 
        return new ModelAndView(model, "login.mustache");  
    }
}, new MustacheTemplateEngine());











//**************** */










        post("/add_users", (req, res) -> {
            res.type("application/json"); // La respuesta será en formato JSON.

            // Obtiene los parámetros 'name' y 'password' de la solicitud.
            String name = req.queryParams("name");
            String password = req.queryParams("password");

            // --- Validaciones básicas ---
            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.status(400); // Código de estado HTTP 400 (Bad Request).
                return objectMapper.writeValueAsString(Map.of("error", "Nombre y contraseña son requeridos."));
            }

            try {
                // --- Creación y guardado del usuario usando el modelo ActiveJDBC ---
                Account newUser = new Account(); // Crea una nueva instancia de tu modelo User.
                newUser.set("name", name); // Asigna el nombre al campo 'name'.
                newUser.set("password", password); // Asigna la contraseña al campo 'password'.
                // ¡ADVERTENCIA DE SEGURIDAD CRÍTICA!
                // En una aplicación real, las contraseñas DEBEN ser hasheadas (ej. con BCrypt)
                // ANTES de guardarse en la base de datos, NUNCA en texto plano.

                newUser.saveIt(); // Guarda el nuevo usuario en la tabla 'users'.

                res.status(201); // Código de estado HTTP 201 (Created) para una creación exitosa.
                // Devuelve una respuesta JSON con el mensaje y el ID del nuevo usuario.
                return objectMapper.writeValueAsString(Map.of("message", "Usuario '" + name + "' registrado con éxito.", "id", newUser.getId()));

            } catch (Exception e) {
                // Si ocurre cualquier error durante la operación de DB, se captura aquí.
                System.err.println("Error al registrar usuario: " + e.getMessage());
                e.printStackTrace(); // Imprime el stack trace para depuración.
                res.status(500); // Código de estado HTTP 500 (Internal Server Error).
                return objectMapper.writeValueAsString(Map.of("error", "Error interno al registrar usuario: " + e.getMessage()));
            }
        });

    }
}
