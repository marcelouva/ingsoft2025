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
            Map<String, Object> model = new HashMap<>();

            // --- Manejo de mensajes de éxito y error (mantener) ---
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            // --- Fin manejo de mensajes ---

            // --- Lógica para precargar los datos de la Persona del usuario ---
            Object userId = req.session().attribute("userId");

            if (userId == null) {
                // Si no hay userId en la sesión, el usuario no está logueado o la sesión expiró.
                res.status(401); // Unauthorized
                model.put("errorMessage", "Debe iniciar sesión para acceder.");
                return new ModelAndView(model, "login.mustache");
            }

            // Aquí asumo que 'User.findFirst("id = ?", userId)' es un método válido para tu ORM
            // y que 'User' tiene un método 'getPerson()' que devuelve un objeto 'Person'.
            User us = User.findFirst("id = ?", userId);

            if (us != null) {
               Person p = us.getPerson(); // Obtiene la persona asociada al usuario

                if (p != null) {

                    // Si ya existe la info de persona asociada al usuario
                    model.put("person", p); // Pasa el objeto 'Person' al modelo
                    model.put("isEditing", true); // Indica que se están editando datos existentes
                    model.put("formTitle", "Modificar mis Datos Personales"); // Título para edición
                } else {
                    // El usuario existe, pero no tiene datos de Persona aún
                    model.put("isEditing", false); // Indica que es un nuevo registro
                    model.put("formTitle", "Completar mis Datos Personales"); // Título para nuevo registro
                }
            } else {
                // Esto podría indicar un problema si el userId está en sesión pero el User no se encuentra
                res.status(404); // Not Found o algún otro error apropiado
                model.put("errorMessage", "Error: Usuario no encontrado para el ID de sesión.");
                return new ModelAndView(model, "error.mustache"); // Redirigir a una página de error o login
            }
            // --- Fin lógica de precarga ---

            return new ModelAndView(model, "person.mustache");
        }, new MustacheTemplateEngine());

       


       post("/person/new", (req, res) -> {
            Object userId = req.session().attribute("userId");

            // --- Nuevos campos para manejar la edición ---
            String personIdParam = req.queryParams("id"); // Obtiene el ID de la persona si está editando
            boolean isEditing = (personIdParam != null && !personIdParam.isEmpty());
            // --- Fin nuevos campos ---

            String name = req.queryParams("name");
            String dni = req.queryParams("dni");
            String birth_date = req.queryParams("birth_date");

            if (name == null || name.isEmpty() || dni == null || dni.isEmpty()) {
                res.status(400);
                res.redirect("/person?error=Falta tu nombre o DNI.");
                return "";
            }

            try {
                Person p;
                if (isEditing) {
                    // Si estamos editando, cargamos la persona existente por su ID
                    p = Person.findById(Long.parseLong(personIdParam));
                    if (p == null) {
                        res.status(404); // Not Found si no se encuentra la persona
                        res.redirect("/person?error=Persona no encontrada para actualizar.");
                        return "";
                    }
                    // Opcional: Asegurarse de que el usuario actual es dueño de esta persona
                    // Esto es crucial para seguridad en un entorno real
                    if (!p.get("user_id").equals(userId)) {
                        res.status(403); // Forbidden
                        res.redirect("/dashboard?error=Acceso denegado.");
                        return "";
                    }

                } else {
                    // Si NO estamos editando, creamos una nueva instancia de Persona
                    p = new Person();
                    // IMPORTANTE: Asegúrate de que no haya ya una persona vinculada a este userId
                    // Dado que user_id es UNIQUE, si ya existe una persona para este userId,
                    // saveIt() lanzará una excepción (violación de unicidad).
                    // Esto es lo que queremos para un 1:1.
                    p.set("user_id", userId); // Vincula la nueva persona con el ID de usuario.
                }

                p.set("name", name);
                p.set("dni", dni);
                p.set("birth_date", birth_date);

                p.saveIt(); // Guarda (inserta o actualiza) los datos.

                res.status(201); // O 200 OK si es una actualización
                String message = isEditing ? "Datos actualizados exitosamente." : "Datos registrados exitosamente.";
                res.redirect("/person?message=" + message + " " + name + "!");
                return "";
            } catch (NumberFormatException e) {
                res.status(400); // Bad Request si el ID no es un número válido
                res.redirect("/person?error=ID de persona inválido.");
                return "";
            } catch (Exception e) {
                System.err.println("Error al procesar el perfil de la cuenta: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                res.redirect("/person?error=Error al procesar los datos.");
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