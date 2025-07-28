package com.is1.proyecto;

import static spark.Spark.*;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.mindrot.jbcrypt.BCrypt;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.is1.proyecto.config.DBConfigSingleton;
import com.is1.proyecto.models.Professor;
import com.is1.proyecto.models.Student;

import com.is1.proyecto.models.User;

// ─── APLICACIÓN PRINCIPAL ─────────────────────────────────────────────────────
public class App {

    public static void main(String[] args) {

        // ── Configuración del puerto ──────────────────────────────────────────
        port(8080);

        // ── Singleton de configuración de base de datos ──────────────────────
        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

        // ── Filtro BEFORE: abre conexión BD por petición ──────────────────────
        before((req, res) -> {
            try {
                Base.open(
                    dbConfig.getDriver(),
                    dbConfig.getDbUrl(),
                    dbConfig.getUser(),
                    dbConfig.getPass()
                );
            } catch (Exception e) {
                System.err.println("Error al abrir conexión: " + e.getMessage());
                halt(500,
                    "{\"error\": \"Error interno: fallo al conectar a la BD.\"}"
                );
            }
        });

        // ── Filtro AFTER: cierra conexión BD ─────────────────────────────────
        after((req, res) -> {
            try {
                Base.close();
            } catch (Exception e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        });

        // ── GET /user/new : muestra formulario de alta de usuario ────────────
        get("/user/new", (req, res) -> {
            System.out.println(">> GET /user/new");
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "user_form.mustache");
        }, new MustacheTemplateEngine());

        // ── GET /logout : cierra sesión y muestra mensaje ────────────────────
        get("/logout", (req, res) -> {
            System.out.println(">> GET /logout");
            Map<String, Object> model = new HashMap<>();
            req.session().invalidate();
            model.put("successMessage", "Usuario deslogeado!!.");
            model.put("returnUrl", "/");
            return new ModelAndView(model, "message.mustache");
        }, new MustacheTemplateEngine());

        // ── GET / : login por defecto ────────────────────────────────────────
        get("/", (req, res) -> {
            System.out.println(">> GET /");
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "login.mustache");
        }, new MustacheTemplateEngine());

        // ── POST /user/new : procesa alta de usuario ─────────────────────────
        post("/user/new", (req, res) -> {
            System.out.println(">> POST /user/new");

            Map<String, Object> model = new HashMap<>();
            String name     = req.queryParams("name");
            String password = req.queryParams("password");

            // Validación de campos vacíos
            if (name == null || name.isEmpty() ||
                password == null || password.isEmpty()) {

                res.status(400);
                model.put("errorMessage", "Algún campo vacío.");
                model.put("returnUrl", "/user/new");
                return new ModelAndView(model, "message.mustache");
            }

            try {
                User user = new User();
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                user.set("username", name);
                user.set("password", hashedPassword);
                user.saveIt();

                res.status(201);
                model.put("successMessage", "La cuenta fue creada exitosamente.");
                model.put("returnUrl", "/");
            } catch (Exception e) {
                System.err.println("Error al registrar: " + e.getMessage());
                res.status(500);
                model.put("errorMessage", "Algo anduvo mal!!");
                model.put("returnUrl", "/user/new");
            }

            return new ModelAndView(model, "message.mustache");
        }, new MustacheTemplateEngine());


        // ________________________________________________________________________________
        //este endpoint es para cargar mediante curl un User con su relacion con Student



// Endpoint para cargar un nuevo User con su relación con Student
//  curl -X POST http://localhost:8080/cargastudiante

        post("/cargastudiante", (req, res) -> {
            System.out.println(">> POST /api/register-profile");
            Map<String, Object> model = new HashMap<>();
            res.type("application/json"); // Aseguramos que la respuesta sea JSON

            String username = "dmaradona";
            String password = "111";
            String firstName = "Diego";
            String lastName = "Maradona";
            String careerName = "Analista en Computación";
            String studentIdNumber = "1200";
            try  {
                // 1. Crear y guardar el Usuario
                User user = new User();
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                user.set("username", username);
                user.set("password", hashedPassword);
                user.saveIt(); // Guardamos el usuario para obtener su ID

                Integer userId = user.getId(); // Obtenemos el ID del usuario recién creado
                Student student = new Student();
                student.set("user_id", userId);
                student.set("first_name", firstName);
                student.set("last_name", lastName);
                student.set("student_id_number", studentIdNumber);
                student.set("career_name", careerName);
                student.saveIt();

                res.status(201);
                model.put("successMessage", "Usuario logeado!!.");
            } 
            
             catch (DBException e) {
            
                res.status(401);
                model.put("errorMessage", "Ha ocurrido un problema.");
                System.err.println("Error al guardar: " + e.getMessage());

            }

            return new ModelAndView(model, "message.mustache");
        }, new MustacheTemplateEngine());


        // ________________________________________________________________________________


 // Nuevo Endpoint GET /api/users-with-profiles : Retorna todos los usuarios con sus perfiles (estudiante/profesor) en JSON
         // ________________________________________________________________________________

 get("/listadeusuarios", (req, res) -> {
            System.out.println(">> GET /listadeusuarios"); // Log actualizado para coincidir con la URL
            res.type("application/json"); // Aseguramos que la respuesta sea JSON
            List<Map<String, Object>> usersWithProfiles = new ArrayList<>();

            try {
                // Obtener todos los usuarios
                List<User> users = User.findAll();

                for (User user : users) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    // No incluir la contraseña hasheada por seguridad en la respuesta JSON

                    // Intentar obtener el perfil de estudiante
                    // Usamos los métodos getStudent() y getProfessor() que definiste en tu modelo User
                    Student studentProfile = user.getStudent();
                    if (studentProfile != null) {
                        Map<String, Object> studentMap = new HashMap<>();
                        studentMap.put("id", studentProfile.getId());
                        studentMap.put("firstName", studentProfile.getFirstName());
                        studentMap.put("lastName", studentProfile.getLastName());
                        studentMap.put("studentIdNumber", studentProfile.getStudentIdNumber());
                        studentMap.put("careerName", studentProfile.getCareerName());
                        userMap.put("profileType", "student"); // Añadir el tipo de perfil
                        userMap.put("profile", studentMap);     // Añadir los datos del perfil
                    } else {
                        // Si no es estudiante, intentar obtener el perfil de profesor
                        Professor professorProfile = user.getProfessor(); // Usamos el método getProfessor()
                        if (professorProfile != null) {
                            Map<String, Object> professorMap = new HashMap<>();
                            professorMap.put("id", professorProfile.getId());
                            professorMap.put("firstName", professorProfile.getFirstName());
                            professorMap.put("lastName", professorProfile.getLastName());
                            professorMap.put("employeeIdNumber", professorProfile.getEmployeeIdNumber());
                            professorMap.put("department", professorProfile.getDepartment());
                            userMap.put("profileType", "professor"); // Añadir el tipo de perfil
                            userMap.put("profile", professorMap);     // Añadir los datos del perfil
                        } else {
                            // Usuario sin perfil específico (solo cuenta de usuario)
                            userMap.put("profileType", "none");
                            userMap.put("profile", null);
                        }
                    }
                    usersWithProfiles.add(userMap);
                }

                res.status(200); // 200 OK
                return usersWithProfiles; // Spark serializará la lista a JSON

            } catch (Exception e) {
                System.err.println("Error al obtener usuarios con perfiles: " + e.getMessage());
                res.status(500); // 500 Internal Server Error
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Error interno del servidor al obtener los perfiles.");
                return errorResponse; // Retornar un JSON de error
            }
        });



        // ________________________________________________________________________________


        // ── POST /login : procesa inicio de sesión ───────────────────────────
        post("/login", (req, res) -> {
            System.out.println(">> POST /login");

            Map<String, Object> model = new HashMap<>();
            String username          = req.queryParams("username");
            String plainTextPassword = req.queryParams("password");
            model.put("returnUrl", "/");

            // Validación de campos vacíos
            if (username == null || username.isEmpty() ||
                plainTextPassword == null || plainTextPassword.isEmpty()) {

                res.status(400);
                model.put("errorMessage",
                          "El nombre de usuario y la contraseña son requeridos.");
                return new ModelAndView(model, "message.mustache");
            }

            // Búsqueda de usuario
            User user = User.findFirst("username = ?", username);
            if (user == null) {
                res.status(401);
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
                return new ModelAndView(model, "message.mustache");
            }

            // Verificación de contraseña
            if (BCrypt.checkpw(plainTextPassword, user.getString("password"))) {
                res.status(200);
                req.session(true).attribute("currentUserUsername", username);
                req.session().attribute("userId", user.getId());
                req.session().attribute("loggedIn", true);

                model.put("successMessage", "Usuario logeado!!.");
            } else {
                res.status(401);
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
            }

            return new ModelAndView(model, "message.mustache");
        }, new MustacheTemplateEngine());
    }
}

