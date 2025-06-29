package com.is1.proyecto;

import com.fasterxml.jackson.databind.ObjectMapper;
import static spark.Spark.*;

import org.javalite.activejdbc.Base;
import org.mindrot.jbcrypt.BCrypt;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import com.is1.proyecto.config.DBConfigSingleton;
import com.is1.proyecto.models.Person;
import com.is1.proyecto.models.User;


/**
 * Clase principal de la aplicación Spark.
 * Configura las rutas, filtros y el inicio del servidor web.
 */
public class App {


    private static final ObjectMapper objectMapper = new ObjectMapper();



    // private static final String SCHEMA_SQL_FILE = "scheme.sql"; // Ya no se usa directamente aquí
    public static void main(String[] args) {
        port(8080); // Configura el puerto en el que la aplicación Spark escuchará las peticiones.

        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

        // El filtro 'before' ahora solo abre la conexión a la base de datos.
        // Asume que el esquema ya ha sido inicializado por DBInitializer al inicio de la aplicación o manualmente.
        before((req, res) -> {
            try {
                // Abre una conexión a la base de datos utilizando las credenciales del singleton.
                Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());
            } catch (Exception e) {
                System.err.println("Error al abrir conexión con ActiveJDBC: " + e.getMessage());
                e.printStackTrace(); // Imprimir el stack trace para depuración
                halt(500, "{\"error\": \"Error interno del servidor: Fallo al conectar a la base de datos.\"}" + e.getMessage());
            }
        });

        // El filtro 'after' cierra la conexión después de cada petición HTTP
        after((req, res) -> {
            try {
                Base.close();
            } catch (Exception e) {
                System.err.println("Error al cerrar conexión con ActiveJDBC: " + e.getMessage());
            }
        });


        // --- Rutas GET para renderizar formularios y páginas HTML ---
        get("/user/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            return new ModelAndView(model, "user_form.mustache");
        }, new MustacheTemplateEngine());

        get("/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String currentUsername = req.session().attribute("currentUserUsername");
            Boolean loggedIn = req.session().attribute("loggedIn");

            if (currentUsername == null || loggedIn == null || !loggedIn) {
                System.out.println("DEBUG: Acceso no autorizado a /dashboard. Redirigiendo a /login.");
                res.redirect("/login?error=Debes iniciar sesión para acceder a esta página.");
                return null;
            }
            model.put("username", currentUsername);
            return new ModelAndView(model, "dashboard.mustache");
        }, new MustacheTemplateEngine());

        get("/logout", (req, res) -> {
            req.session().invalidate();
            System.out.println("DEBUG: Sesión cerrada. Redirigiendo a /login.");
            res.redirect("/");
            return null;
        });
        

        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            return new ModelAndView(model, "login.mustache");
        }, new MustacheTemplateEngine());

        get("/user/new", (req, res) -> {
            return new ModelAndView(new HashMap<>(), "user_form.mustache");
        }, new MustacheTemplateEngine());


        // --- Rutas POST para manejar envíos de formularios y APIs ---

        post("/user/new", (req, res) -> {
            String name = req.queryParams("name");
            String password = req.queryParams("password");

            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.status(400);
                res.redirect("/user/create?error=Nombre y contraseña son requeridos.");
                return "";
            }

            try {
                User ac = new User();
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                ac.set("username", name);
                ac.set("password", hashedPassword);
                ac.saveIt();

                res.status(201);
                res.redirect("/user/create?message=Cuenta creada exitosamente para " + name + "!");
                return "";

            } catch (Exception e) {
                System.err.println("Error al registrar la cuenta: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                res.redirect("/user/create?error=Error interno al crear la cuenta. Intente de nuevo.");
                return "";
            }
        });



get("/person", (req, res) -> {
    Object userId = req.session().attribute("userId");
    Map<String, Object> model = new HashMap<>();
    String successMessage = req.queryParams("message");
    if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
    }
    String errorMessage = req.queryParams("error");
    if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
    }
    return new ModelAndView(model, "person.mustache");
}, new MustacheTemplateEngine());


post("/person/new", (req, res) -> {
    Object userId = req.session().attribute("userId");
    
    String name = req.queryParams("name");
    String dni = req.queryParams("dni");
    String birt_date = req.queryParams("birt_date");

    if (name == null || name.isEmpty() || dni == null || dni.isEmpty()) {
                res.status(400);
                res.redirect("/person?error=Falta tu nombre o DNI.");
                return " ";
    }
    try {// si los datos están ok creamos la Person y la vinculamos con el User
                Person p = new Person();
                p.set("name", name);
                p.set("dni", dni);
                p.set("birt_date", birt_date);
 
                p.set("user_id",userId);
                p.saveIt();

                res.status(201);
                res.redirect("/person?message=Datos registrados exitosamente." + name + "!");
                return "";
       } catch (Exception e) {
                System.err.println("Error al registrar el perfil de la cuenta: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                res.redirect("/person?error=Error al registrar los datos.");
                return "";
         }
    });








  /**    if (userId != null) {
        Person person = Person.findFirst("user_id = ?", userId);
        if (person != null) {
            model.put("person", person.toMap()); // convierte los campos a map
            model.put("id", person.getId());     // para el botón Editar
        }
    }
  
    return new ModelAndView(model, "person.mustache");
}, new MustacheTemplateEngine());
*/












        post("/login", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            String username = req.queryParams("username");
            String plainTextPassword = req.queryParams("password");

            if (username == null || username.isEmpty() || plainTextPassword == null || plainTextPassword.isEmpty()) {
                res.status(400);
                model.put("errorMessage", "El nombre de usuario y la contraseña son requeridos.");
                return new ModelAndView(model, "login.mustache");
            }

            User ac = User.findFirst("username = ?", username);

            if (ac == null) {
                res.status(401);
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
                return new ModelAndView(model, "login.mustache");
            }

            String storedHashedPassword = ac.getString("password");

            if (BCrypt.checkpw(plainTextPassword, storedHashedPassword)) {
                res.status(200);

                req.session(true).attribute("currentUserUsername", username);
                req.session().attribute("userId", ac.getId());
                req.session().attribute("loggedIn", true);

                System.out.println("DEBUG: Login exitoso para la cuenta: " + username);
                System.out.println("DEBUG: ID de Sesión: " + req.session().id());


                model.put("username", username);
                return new ModelAndView(model, "dashboard.mustache");
            } else {
                res.status(401);
                System.out.println("DEBUG: Intento de login fallido para: " + username);
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
                return new ModelAndView(model, "login.mustache");
            }
        }, new MustacheTemplateEngine());


        post("/add_users", (req, res) -> {
            res.type("application/json");

            String name = req.queryParams("name");
            String password = req.queryParams("password");

            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.status(400);
                return objectMapper.writeValueAsString(Map.of("error", "Nombre y contraseña son requeridos."));
            }

            try {
                User newUser = new User();
                // ¡ADVERTENCIA DE SEGURIDAD CRÍTICA!
                // Si este endpoint crea usuarios que luego inician sesión, DEBES hashear la contraseña aquí también.
                // String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                newUser.set("name", name);
                newUser.set("password", password); // Si no se hashea aquí, la contraseña se guarda en texto plano
                newUser.saveIt();

                res.status(201);
                return objectMapper.writeValueAsString(Map.of("message", "Usuario '" + name + "' registrado con éxito.", "id", newUser.getId()));

            } catch (Exception e) {
                System.err.println("Error al registrar usuario: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return objectMapper.writeValueAsString(Map.of("error", "Error interno al registrar usuario: " + e.getMessage()));
            }
        });

    } // Cierre del método main


} // Cierre de la clase App
