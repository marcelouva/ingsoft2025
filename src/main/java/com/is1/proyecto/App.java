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
import com.is1.proyecto.models.Subject;

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

       
        get("/professor", (req, res) -> {
            System.out.println(">> GET /");
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "professor.mustache");
        }, new MustacheTemplateEngine());

        get("/profe", (req, res) -> {
            System.out.println(">> GET /");
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "profe.mustache");
        }, new MustacheTemplateEngine());


        get("/dashboard", (req, res) -> {
            System.out.println(">> GET /");
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "dashboard.mustache");
        }, new MustacheTemplateEngine());

       
        // ── POST /user/new : procesa alta de usuario ─────────────────────────
        post("/user/new", (req, res) -> {
            System.out.println(">> POST /user/new");

            Map<String, Object> model = new HashMap<>();
            String name     = req.queryParams("name");
            String password = req.queryParams("password");
            String role = req.queryParams("role");

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
                user.set("role", role);
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



        // ________________________________________________________________________________


    post("/professors", (req, res) -> {
    // Extraer los valores de los campos del formulario usando su atributo 'name'
        String name = req.queryParams("name");
        String last_name = req.queryParams("last_name");
        String email = req.queryParams("email");
        String id_employee = req.queryParams("id_employee"); // Lo extraemos como String primero
   
        Map<String, Object> model = new HashMap<>();
        //model.put("errorMessage", "Algún campo vacío.");
        //model.put("returnUrl", "/user/new");
        //        return new ModelAndView(model, "message.mustache");
   
        
            try {
                Professor profe = new Professor();

                profe.set("name", name);
                profe.set("last_name", last_name);
                profe.set("email", email);
                profe.set("id_employee", id_employee);

                profe.set("user_id",req.session().attribute("userId"));
                profe.saveIt();

                res.status(201);
                model.put("successMessage", "La cuenta fue creada exitosamente.");
                model.put("returnUrl", "/dashboard");
            } catch (Exception e) {
                System.err.println("Error al registrar: " + e.getMessage());
                res.status(500);
                model.put("errorMessage", "Algo anduvo mal!!");
                model.put("returnUrl", "/professors");
            }

            return new ModelAndView(model, "message.mustache");
        }, new MustacheTemplateEngine());
    

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
                req.session().attribute("role", user.getRole());

                req.session().attribute("loggedIn", true);
 
                model.put("successMessage", "Usuario logeado!!.");
               
                switch (user.getRole()) {
                    case 1:
                        if (user.getProfessor()==null){
                            res.redirect("/professor");
                        }else{
                            res.redirect("/dashboard");
                        }    
                        break;
                    case 2:
                        res.redirect("/administrador");
                        break;
                    default:
                        res.redirect("/profe");
                        break;
                }

            } else {
                res.status(401);
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
            }

            return new ModelAndView(model, "message.mustache");
        }, new MustacheTemplateEngine());







// El siguiente post asocia un profesor a una materia. 
// curl -X POST http://localhost:8080/professors/1/subjects/2

post("/professors/:professor_id/subjects/:subject_id", (req, res) -> {
    // Abrir conexión si no está abierta
    if (!Base.hasConnection()) Base.open("org.sqlite.JDBC", "jdbc:sqlite:db/database.db", "", "");

    Long professorId = Long.valueOf(req.params(":professor_id"));
    Long subjectId = Long.valueOf(req.params(":subject_id"));

    Professor professor = Professor.findById(professorId);
    Subject subject = Subject.findById(subjectId);

    if (professor == null || subject == null) {
        res.status(404);
        return "Profesor o materia no encontrada.";
    }

    // Verificar si ya están asociados
    if (!professor.getSubjects().contains(subject)) {
        professor.add(subject);  // Relación N:N
        res.status(201);
        return "Profesor asignado a la materia correctamente.";
    } else {
        res.status(200);
        return "El profesor ya está asignado a esta materia.";
    }
});











    }
}

