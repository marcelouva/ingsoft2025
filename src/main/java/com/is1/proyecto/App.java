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


        get("/adm", (req, res) -> {
            System.out.println(">> GET /");
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "dashboardadm.mustache");
        }, new MustacheTemplateEngine());



post("/admin/relate", (request, response) -> {
    Map<String, Object> model = new HashMap<>();
    String action = request.queryParams("action");

    // =========================================================================
    // 1. Persistir el estado actual del formulario
    //    Esto es crucial para que los campos no se "borren" en cada POST.
    // =========================================================================
    String teacherIdInput = request.queryParams("teacherIdInput");
    String subjectIdInput = request.queryParams("subjectIdInput");
    String selectedTeacherId = request.queryParams("selectedTeacherId");
    String selectedSubjectId = request.queryParams("selectedSubjectId");
    
    // Si ya hay un docente seleccionado, lo volvemos a cargar en el modelo
    if (selectedTeacherId != null && !selectedTeacherId.isEmpty()) {
        try {
            Professor teacher = Professor.findById(Long.parseLong(selectedTeacherId));
            if (teacher != null) {
                model.put("teacherName", teacher.get("name"));
                model.put("selectedTeacherId", teacher.get("id"));
            }
        } catch (NumberFormatException e) {
            // Ignorar el error, se limpiará el campo
            selectedTeacherId = null;
        }
    }

    // Si ya hay una materia seleccionada, la volvemos a cargar en el modelo
    if (selectedSubjectId != null && !selectedSubjectId.isEmpty()) {
        try {
            Subject subject = Subject.findById(Long.parseLong(selectedSubjectId));
            if (subject != null) {
                model.put("subjectName", subject.get("name"));
                model.put("selectedSubjectId", subject.get("id"));
            }
        } catch (NumberFormatException e) {
            // Ignorar el error, se limpiará el campo
            selectedSubjectId = null;
        }
    }
    
    // También mantenemos el valor de los inputs para las búsquedas
    model.put("teacherIdInput", teacherIdInput);
    model.put("subjectIdInput", subjectIdInput);


    // =========================================================================
    // 2. Ejecutar la acción solicitada por el usuario
    // =========================================================================

    if ("searchTeacher".equals(action)) {
        try {
            long teacherId = Long.parseLong(teacherIdInput);
            Professor teacher = Professor.findById(teacherId);

            if (teacher != null) {
                model.put("teacherName", teacher.get("name"));
                model.put("selectedTeacherId", teacher.get("id"));
                model.put("successMessage", "Docente encontrado: " + teacher.get("name"));
            } else {
                model.put("errorMessage", "No se encontró un docente con el ID: " + teacherIdInput);
                model.remove("teacherName"); // Limpiamos el nombre si no se encuentra
                model.remove("selectedTeacherId"); // Limpiamos el ID si no se encuentra
            }
        } catch (NumberFormatException e) {
            model.put("errorMessage", "El ID del docente debe ser un número.");
        }
    }
    else if ("searchSubject".equals(action)) {
        try {
            long subjectId = Long.parseLong(subjectIdInput);
            Subject subject = Subject.findById(subjectId);
            
            if (subject != null) {
                model.put("subjectName", subject.get("name"));
                model.put("selectedSubjectId", subject.get("id"));
                model.put("successMessage", "Materia encontrada: " + subject.get("name"));
            } else {
                model.put("errorMessage", "No se encontró una materia con el ID: " + subjectIdInput);
                model.remove("subjectName"); // Limpiamos el nombre si no se encuentra
                model.remove("selectedSubjectId"); // Limpiamos el ID si no se encuentra
            }
        } catch (NumberFormatException e) {
             model.put("errorMessage", "El ID de la materia debe ser un número.");
        }
    }
    else if ("relate".equals(action)) {
        if (selectedTeacherId != null && selectedSubjectId != null) {
            try {
                long teacherId = Long.parseLong(selectedTeacherId);
                long subjectId = Long.parseLong(selectedSubjectId);

                Professor teacher = Professor.findById(teacherId);
                Subject subject = Subject.findById(subjectId);
                
                if (teacher != null && subject != null) {
                    teacher.add(subject);
                    model.put("successMessage", "Relación creada con éxito entre " + teacher.get("name") + " y " + subject.get("name"));
                    
                    // Opcional: Limpiar los campos para una nueva relación
                    model.remove("teacherName");
                    model.remove("selectedTeacherId");
                    model.remove("subjectName");
                    model.remove("selectedSubjectId");
                    model.remove("teacherIdInput");
                    model.remove("subjectIdInput");
                } else {
                     model.put("errorMessage", "Error: Docente o materia no encontrados.");
                }

            } catch (NumberFormatException e) {
                model.put("errorMessage", "IDs de docente o materia no válidos.");
            }
        } else {
            model.put("errorMessage", "Debe seleccionar un docente y una materia para relacionarlos.");
        }
    }

    return new ModelAndView(model, "dashboardadm.mustache");
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
                        res.redirect("/adm");
                        break;
                    default:

                        if (user.getStudent()==null){
                            res.redirect("/student");
                        }else{
                            res.redirect("/dashboard");
                        }    
                        break; 
                }

            } else {
                res.status(401);
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
            }

            return new ModelAndView(model, "message.mustache");
        }, new MustacheTemplateEngine());







// El siguiente post asocia un profesor a una materia. 
// curl -X POST http://localhost:8080/professors/1/subjects/3

post("/professors/:professor_id/subjects/:subject_id", (req, res) -> {
   

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

