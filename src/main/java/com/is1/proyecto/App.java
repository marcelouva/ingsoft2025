package com.is1.proyecto;

import static spark.Spark.*; // Importa los métodos estáticos de Spark para definir rutas y configuraciones.

import org.javalite.activejdbc.Base; // Para la conexión y operaciones con la base de datos usando ActiveJDBC.
import org.mindrot.jbcrypt.BCrypt; // Para el hashing de contraseñas de forma segura.

import spark.ModelAndView; // Para renderizar plantillas con un modelo de datos.
import spark.template.mustache.MustacheTemplateEngine; // Motor de plantillas Mustache para la interfaz de usuario.

import java.util.HashMap; // Para crear mapas de datos que se pasan a las plantillas.
import java.util.Map; // Interfaz Map.

import com.is1.proyecto.config.DBConfigSingleton; // Configuración Singleton para la base de datos.
import com.is1.proyecto.models.User; // Modelo de datos para la entidad Usuario.


public class App {
    public static void main(String[] args) {
        port(8080); // Configura el puerto en el que la aplicación Spark escuchará las peticiones HTTP (puerto 8080).

        // Obtiene la instancia única de la configuración de la base de datos.
        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

        // --- Filtros de Request (before y after) ---
        // Este filtro se ejecuta ANTES de que cualquier ruta sea procesada.
        // Se encarga de abrir una conexión a la base de datos para cada petición HTTP.
        before((req, res) -> {
            try {
                // Abre una conexión a la base de datos utilizando las credenciales obtenidas del singleton.
                Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());
            } catch (Exception e) {
                // En caso de error al abrir la conexión, registra el error y detiene la petición con un estado 500.
                System.err.println("Error al abrir conexión con ActiveJDBC: " + e.getMessage());
                e.printStackTrace(); // Imprimir el stack trace para depuración.
                halt(500, "{\"error\": \"Error interno del servidor: Fallo al conectar a la base de datos.\"}" + e.getMessage());
            }
        });

        // Este filtro se ejecuta DESPUÉS de que cualquier ruta haya sido procesada.
        // Se encarga de cerrar la conexión a la base de datos después de cada petición.
        after((req, res) -> {
            try {
                Base.close(); // Cierra la conexión de la base de datos.
            } catch (Exception e) {
                // Registra cualquier error que ocurra al cerrar la conexión.
                System.err.println("Error al cerrar conexión con ActiveJDBC: " + e.getMessage());
            }
        });


        // --- Rutas GET para renderizar formularios y páginas HTML ---

        // Ruta para mostrar el formulario de creación de usuario.
        get("/user/new", (req, res) -> {
            System.out.println(">>   get /user/new");
            Map<String, Object> model = new HashMap<>();
            // Verifica si hay un mensaje de éxito en los parámetros de la URL y lo añade al modelo.
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            // Verifica si hay un mensaje de error en los parámetros de la URL y lo añade al modelo.
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            // Retorna la vista "user_form.mustache" con el modelo.
            return new ModelAndView(model, "user_form.mustache");
        }, new MustacheTemplateEngine());


        // Ruta para cerrar la sesión del usuario.
        get("/logout", (req, res) -> {
            System.out.println(">> get   /logout");
            req.session().invalidate(); // Invalida la sesión actual, eliminando todos sus atributos.
            System.out.println("DEBUG: Sesión cerrada. Redirigiendo a /login.");
            res.redirect("/"); // Redirige a la página de inicio (login).
            return null;
        });






        // Ruta para la página de inicio, que es el formulario de login.
        get("/", (req, res) -> {
            System.out.println(">>   get /");
            Map<String, Object> model = new HashMap<>();
            // Manejo de mensajes de error y éxito pasados como parámetros de la URL.
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            // Retorna la vista "login.mustache" con el modelo.
            return new ModelAndView(model, "login.mustache");
        }, new MustacheTemplateEngine());

        // Ruta alternativa para mostrar el formulario de creación de usuario (redirecciona a /user/create).
        get("/user/new", (req, res) -> {
        System.out.println(">>   get /user/new");


            return new ModelAndView(new HashMap<>(), "user_form.mustache");
        }, new MustacheTemplateEngine());


        // --- Rutas POST para manejar envíos de formularios y APIs ---







  post("/user/new", (req, res) -> {
            System.out.println(">>   post /user/new");

            Map<String, Object> model = new HashMap<>();
            String name = req.queryParams("name"); // Obtiene el nombre de usuario del formulario.
            String password = req.queryParams("password"); // Obtiene la contraseña del formulario.

            // Valida que el nombre y la contraseña no estén vacíos.
            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.status(400); // Bad Request.
                model.put("errorMessage", "Algún campo vacio.");
                model.put("returnUrl", "/user/new");  

            }

            try {
                User user = new User();
                // Hashea la contraseña usando BCrypt antes de almacenarla, por seguridad.
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                user.set("username", name); // Establece el nombre de usuario.
                user.set("password", hashedPassword); // Establece la contraseña hasheada.
                user.saveIt(); // Guarda el nuevo usuario en la base de datos.

                res.status(201); // Created.

                model.put("successMessage", "La cuenta fue creada exitosamente.");
                model.put("returnUrl", "/");


            } catch (Exception e) {
                // Manejo de errores en caso de fallo al registrar la cuenta.
                System.err.println("Error al registrar la cuenta: " + e.getMessage());
                e.printStackTrace();
                res.status(500); // Internal Server Error.
                model.put("errorMessage", "Algo anduvo mal!!");
                model.put("returnUrl", "/user/new");  
            }
            
            return new ModelAndView(model, "message.mustache");

        }, new MustacheTemplateEngine());





        // Ruta POST para manejar el inicio de sesión.
        post("/login", (req, res) -> {
                 System.out.println(">>   post /login");
            Map<String, Object> model = new HashMap<>();

            String username = req.queryParams("username"); // Obtiene el nombre de usuario del formulario.
            String plainTextPassword = req.queryParams("password"); // Obtiene la contraseña en texto plano.

            // Valida que el nombre de usuario y la contraseña no estén vacíos.
            if (username == null || username.isEmpty() || plainTextPassword == null || plainTextPassword.isEmpty()) {
                res.status(400); // Bad Request.
                model.put("errorMessage", "El nombre de usuario y la contraseña son requeridos.");
                return new ModelAndView(model, "login.mustache"); // Retorna la vista de login con mensaje de error.
            }

            // Busca al usuario en la base de datos por su nombre de usuario.
            User ac = User.findFirst("username = ?", username);

            // Si no se encuentra el usuario.
            if (ac == null) {
                res.status(401); // Unauthorized.
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
                return new ModelAndView(model, "login.mustache"); // Retorna la vista de login con mensaje de error.
            }

            String storedHashedPassword = ac.getString("password"); // Obtiene la contraseña hasheada almacenada.

            // Verifica si la contraseña en texto plano coincide con la contraseña hasheada almacenada.
            if (BCrypt.checkpw(plainTextPassword, storedHashedPassword)) {
                res.status(200); // OK.

                // Establece atributos en la sesión para indicar que el usuario ha iniciado sesión.
                req.session(true).attribute("currentUserUsername", username); // Nombre de usuario actual.
                req.session().attribute("userId", ac.getId()); // ID del usuario.
                req.session().attribute("loggedIn", true); // Bandera de inicio de sesión.

                System.out.println("DEBUG: Login exitoso para la cuenta: " + username);
                System.out.println("DEBUG: ID de Sesión: " + req.session().id());

                model.put("username", username);
                model.put("successMessage", "Usuario logeado!!.");
                model.put("returnUrl", "/");

            } else {
                res.status(401); // Unauthorized.
                System.out.println("DEBUG: Intento de login fallido para: " + username);
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
                model.put("returnUrl", "/");
                
            }
            return new ModelAndView(model, "message.mustache");
        }, new MustacheTemplateEngine());











    } // Cierre del método main

} // Cierre de la clase App