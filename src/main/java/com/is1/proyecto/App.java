package com.is1.proyecto;

// Importaciones necesarias para la aplicación Spark, manejo de JSON, base de datos y seguridad.
import com.fasterxml.jackson.databind.ObjectMapper; // Para serializar/deserializar objetos JSON.
import static spark.Spark.*; // Importa los métodos estáticos de Spark para definir rutas y configuraciones.

import org.javalite.activejdbc.Base; // Para la conexión y operaciones con la base de datos usando ActiveJDBC.
import org.mindrot.jbcrypt.BCrypt; // Para el hashing de contraseñas de forma segura.

import spark.ModelAndView; // Para renderizar plantillas con un modelo de datos.
import spark.template.mustache.MustacheTemplateEngine; // Motor de plantillas Mustache para la interfaz de usuario.

import java.util.HashMap; // Para crear mapas de datos que se pasan a las plantillas.
import java.util.Map; // Interfaz Map.

import com.is1.proyecto.config.DBConfigSingleton; // Configuración Singleton para la base de datos.
import com.is1.proyecto.models.Person; // Modelo de datos para la entidad Persona.
import com.is1.proyecto.models.User; // Modelo de datos para la entidad Usuario.


/**
 * Clase principal de la aplicación Spark.
 * Configura las rutas, filtros y el inicio del servidor web.
 * Esta clase es el punto de entrada para la aplicación web.
 */
public class App {

    // Instancia de ObjectMapper para convertir objetos Java a JSON y viceversa.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // private static final String SCHEMA_SQL_FILE = "scheme.sql"; // Esta línea está comentada y no se usa directamente aquí.
                                                                 // Sugiere que la inicialización del esquema se maneja de otra forma.

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
        get("/user/create", (req, res) -> {
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

        // Ruta para mostrar el panel de control (dashboard). Requiere inicio de sesión.
        get("/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            // Recupera el nombre de usuario y el estado de inicio de sesión de la sesión.
            String currentUsername = req.session().attribute("currentUserUsername");
            Boolean loggedIn = req.session().attribute("loggedIn");

            // Si el usuario no está logueado, redirige a la página de login con un mensaje de error.
            if (currentUsername == null || loggedIn == null || !loggedIn) {
                System.out.println("DEBUG: Acceso no autorizado a /dashboard. Redirigiendo a /login.");
                res.redirect("/login?error=Debes iniciar sesión para acceder a esta página.");
                return null; // Importante retornar null después de una redirección para evitar procesamiento adicional.
            }
            // Si está logueado, añade el nombre de usuario al modelo y renderiza el dashboard.
            model.put("username", currentUsername);
            return new ModelAndView(model, "dashboard.mustache");
        }, new MustacheTemplateEngine());

        // Ruta para cerrar la sesión del usuario.
        get("/logout", (req, res) -> {
            req.session().invalidate(); // Invalida la sesión actual, eliminando todos sus atributos.
            System.out.println("DEBUG: Sesión cerrada. Redirigiendo a /login.");
            res.redirect("/"); // Redirige a la página de inicio (login).
            return null;
        });

        // Ruta para la página de inicio, que es el formulario de login.
        get("/", (req, res) -> {
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
            return new ModelAndView(new HashMap<>(), "user_form.mustache");
        }, new MustacheTemplateEngine());


        // --- Rutas POST para manejar envíos de formularios y APIs ---

        // Ruta para procesar el envío del formulario de creación de un nuevo usuario.
        post("/user/new", (req, res) -> {
            String name = req.queryParams("name"); // Obtiene el nombre de usuario del formulario.
            String password = req.queryParams("password"); // Obtiene la contraseña del formulario.

            // Valida que el nombre y la contraseña no estén vacíos.
            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.status(400); // Bad Request.
                res.redirect("/user/create?error=Nombre y contraseña son requeridos."); // Redirige con mensaje de error.
                return "";
            }

            try {
                User ac = new User();
                // Hashea la contraseña usando BCrypt antes de almacenarla, por seguridad.
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                ac.set("username", name); // Establece el nombre de usuario.
                ac.set("password", hashedPassword); // Establece la contraseña hasheada.
                ac.saveIt(); // Guarda el nuevo usuario en la base de datos.

                res.status(201); // Created.
                res.redirect("/user/create?message=Cuenta creada exitosamente para " + name + "!"); // Redirige con mensaje de éxito.
                return "";

            } catch (Exception e) {
                // Manejo de errores en caso de fallo al registrar la cuenta.
                System.err.println("Error al registrar la cuenta: " + e.getMessage());
                e.printStackTrace();
                res.status(500); // Internal Server Error.
                res.redirect("/user/create?error=Error interno al crear la cuenta. Intente de nuevo."); // Redirige con mensaje de error genérico.
                return "";
            }
        });

        // Ruta GET para mostrar el formulario de registro de datos personales (Person).
        get("/person", (req, res) -> {
            Object userId = req.session().attribute("userId"); // Obtiene el ID de usuario de la sesión.
            Map<String, Object> model = new HashMap<>();
            // Manejo de mensajes de éxito y error.
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            // Retorna la vista "person.mustache" con el modelo.
            return new ModelAndView(model, "person.mustache");
        }, new MustacheTemplateEngine());


        // Ruta POST para procesar el envío del formulario de creación de datos personales (Person).
        post("/person/new", (req, res) -> {
            Object userId = req.session().attribute("userId"); // Obtiene el ID de usuario de la sesión para vincular la persona.

            String name = req.queryParams("name"); // Nombre de la persona.
            String dni = req.queryParams("dni"); // DNI de la persona.
            String birth_date = req.queryParams("birth_date"); // Fecha de nacimiento de la persona.

            // Valida que el nombre y DNI no estén vacíos.
            if (name == null || name.isEmpty() || dni == null || dni.isEmpty()) {
                res.status(400); // Bad Request.
                res.redirect("/person?error=Falta tu nombre o DNI."); // Redirige con mensaje de error.
                return " ";
            }
            try { // Si los datos están ok, creamos la Person y la vinculamos con el User
                Person p = new Person();
                p.set("name", name); // Establece el nombre.
                p.set("dni", dni); // Establece el DNI.
                p.set("birth_date", birth_date); // Establece la fecha de nacimiento.

                p.set("user_id", userId); // Vincula esta persona con el ID de usuario de la sesión.
                p.saveIt(); // Guarda los datos de la persona en la base de datos.

                res.status(201); // Created.
                res.redirect("/person?message=Datos registrados exitosamente." + name + "!"); // Redirige con mensaje de éxito.
                return "";
            } catch (Exception e) {
                // Manejo de errores en caso de fallo al registrar el perfil de la persona.
                System.err.println("Error al registrar el perfil de la cuenta: " + e.getMessage());
                e.printStackTrace();
                res.status(500); // Internal Server Error.
                res.redirect("/person?error=Error al registrar los datos."); // Redirige con mensaje de error genérico.
                return "";
            }
        });

        // Ruta POST para manejar el inicio de sesión.
        post("/login", (req, res) -> {
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
                return new ModelAndView(model, "dashboard.mustache"); // Redirige al dashboard.
            } else {
                res.status(401); // Unauthorized.
                System.out.println("DEBUG: Intento de login fallido para: " + username);
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
                return new ModelAndView(model, "login.mustache"); // Retorna la vista de login con mensaje de error.
            }
        }, new MustacheTemplateEngine());


    } // Cierre del método main

} // Cierre de la clase App