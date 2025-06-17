package com.is1.proyecto; // Asegúrate de que el paquete coincide con tu groupId y estructura de carpetas

import com.fasterxml.jackson.databind.ObjectMapper; // Necesario para convertir objetos Java a JSON

import static spark.Spark.*; // Importa los métodos estáticos de Spark (get, post, before, after, etc.)

// Importaciones de ActiveJDBC
import org.javalite.activejdbc.Base; // Clase central de ActiveJDBC para gestión de DB
import com.is1.proyecto.models.User; // Tu modelo User para interactuar con la tabla 'users'

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.Map; // Para usar Map.of() en las respuestas JSON

public class App {

    // ObjectMapper es una clase de Jackson para serializar/deserializar JSON.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        port(8080); // La aplicación Spark escuchará en el puerto 8080

        // --- Configuración de filtros para abrir y cerrar conexiones de ActiveJDBC ---
        // Este filtro se ejecuta ANTES de que cualquier ruta sea procesada.
        // Aquí abrimos la conexión a la base de datos para la solicitud actual.
        before((req, res) -> {
            try {
                // Base.open() gestiona la conexión. Se conecta a la BD 'students_db' con el usuario 'muva'.
                // ¡ADVERTENCIA! Credenciales en código fuente NO SEGURAS para producción.
                Base.open("com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost:3306/students_db", "muva", "muva");
            } catch (Exception e) {
                System.err.println("Error al abrir conexión con ActiveJDBC: " + e.getMessage());
                // Si la conexión falla, detiene la solicitud y envía un error 500 al cliente.
                halt(500, "{\"error\": \"Error interno del servidor: Fallo al conectar a la base de datos.\"}" + e.getMessage());
            }
        });

        // Este filtro se ejecuta DESPUÉS de que cualquier ruta ha sido procesada (o si hubo un error).
        // Aquí cerramos la conexión de la base de datos, devolviéndola al pool de ActiveJDBC.
        after((req, res) -> {
            try {
                Base.close(); // Cierra la conexión de ActiveJDBC para esta solicitud.
            } catch (Exception e) {
                System.err.println("Error al cerrar conexión con ActiveJDBC: " + e.getMessage());
                // Este error es de limpieza, no debería detener la respuesta principal.
            }
        });
        // -------------------------------------------------------------------------

        // --- Endpoint de prueba simple (ruta raíz) ---
        get("/", (req, res) -> {
            res.type("application/json"); // La respuesta será en formato JSON.
            return objectMapper.writeValueAsString(Map.of("message", "¡Bienvenido a la API simplificada! Usa /users para crear usuarios."));
        });


get("/users/2form", (req, res) -> {
    return new ModelAndView(null, "user_form.mustache");
}, new MustacheTemplateEngine());




get("/users/altaform", (req, res) -> {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Alta usuario");
    return new ModelAndView(model, "user_form_con_titulo.mustache");
}, new MustacheTemplateEngine());




// --- NUEVO ENDPOINT: Formulario para crear un User ---
        get("/users/form", (req, res) -> {
            res.type("text/html"); // La respuesta será HTML

            // Construye la cadena HTML para el formulario de registro de usuario
            String htmlForm = "<!DOCTYPE html>\n" +
                              "<html lang=\"es\">\n" +
                              "<head>\n" +
                              "    <meta charset=\"UTF-8\">\n" +
                              "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                              "    <title>Registrar Nuevo Usuario</title>\n" +
                              "    <script src=\"https://cdn.tailwindcss.com\"></script>\n" +
                              "    <style>\n" +
                              "        body {\n" +
                              "            font-family: 'Inter', sans-serif;\n" +
                              "        }\n" +
                              "    </style>\n" +
                              "</head>\n" +
                              "<body class=\"bg-gray-100 flex items-center justify-center min-h-screen p-4\">\n" +
                              "    <div class=\"bg-white p-8 rounded-lg shadow-lg w-full max-w-md\">\n" +
                              "        <h1 class=\"text-3xl font-bold text-center text-gray-800 mb-6\">Registrar Nuevo Usuario</h1>\n" +
                              "        <form action=\"/users\" method=\"post\" class=\"space-y-4\">\n" +
                              "            <div>\n" +
                              "                <label for=\"name\" class=\"block text-gray-700 text-sm font-medium mb-1\">Nombre de Usuario:</label>\n" +
                              "                <input type=\"text\" id=\"name\" name=\"name\" required\n" +
                              "                       class=\"w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500\">\n" +
                              "            </div>\n" +
                              "            <div>\n" +
                              "                <label for=\"password\" class=\"block text-gray-700 text-sm font-medium mb-1\">Contraseña:</label>\n" +
                              "                <input type=\"password\" id=\"password\" name=\"password\" required\n" +
                              "                       class=\"w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500\">\n" +
                              "            </div>\n" +
                              "            <button type=\"submit\"\n" +
                              "                    class=\"w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2\">\n" +
                              "                Registrar Usuario\n" +
                              "            </button>\n" +
                              "            <div class=\"text-center mt-4\">\n" +
                              "                <a href=\"/\" class=\"text-blue-600 hover:underline text-sm\">Volver al inicio</a>\n" +
                              "            </div>\n" +
                              "        </form>\n" +
                              "    </div>\n" +
                              "</body>\n" +
                              "</html>";
            return htmlForm;
        });










        // --- Endpoint para dar de alta un nuevo User (método POST) ---
        // Espera parámetros 'name' y 'password' en el cuerpo de la solicitud (form-urlencoded).
        // Ejemplo de uso con curl:
        // curl -X POST -d "name=Alice&password=pass123" http://localhost:8080/users
        post("/users", (req, res) -> {
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
                User newUser = new User(); // Crea una nueva instancia de tu modelo User.
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

        // Este mensaje se mostrará en la consola cuando la aplicación se inicie.
        System.out.println("Servidor Spark simplificado iniciado en http://localhost:8080");
        System.out.println("Prueba POST a /users con 'name' y 'password' en el cuerpo.");
    }
}
