package com.is1.proyecto;

import static spark.Spark.*;

import org.javalite.activejdbc.Base;
import org.mindrot.jbcrypt.BCrypt;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import com.is1.proyecto.config.DBConfigSingleton;
import com.is1.proyecto.models.Profile;
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

//**********************************






get("/profile/new", (req, res) -> {
    Map<String, Object> model = new HashMap<>();
    // Puedes agregar mensajes de éxito o error si vienen de una redirección
    // model.put("successMessage", req.session().attribute("success"));
    // model.put("errorMessage", req.session().attribute("error"));
    return new ModelAndView(model, "profile.mustache"); // Asumiendo que guardaste el HTML como profile_form.html
}, new MustacheTemplateEngine());




get("/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            // 1. Obtener el nombre de usuario de la sesión
            String userName = req.session().attribute("currentUserUsername");

            // 2. Verificar si el nombre de usuario existe en la sesión
            if (userName != null) {
                model.put("userName", userName); // Si existe, agrégalo al modelo
            } else {
                // Opcional: Si el usuario no está logueado o el nombre no está en sesión,
                // puedes redirigirlo a la página de login o mostrar un mensaje de error.
                // Por ejemplo:
                res.redirect("/login");
                return null; // Detener la ejecución del endpoint actual
            }

            // 3. Renderizar la plantilla con el modelo
            return new ModelAndView(model, "dashboard.mustache");
        }, new MustacheTemplateEngine());



        post("/user/p1", (req, res) -> {
            System.out.println(">> POST /user/new");
            Map<String, Object> model = new HashMap<>();



            
            return new ModelAndView(model, "message.mustache");
        }, new MustacheTemplateEngine());














//******************************


























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

                // con las dos lineas que siguen redirecciona al dashboard
                res.redirect("/dashboard");
                return null; // Detener la ejecución del endpoint actual

                //model.put("returnUrl", "/dashboard");
                //model.put("successMessage", "Usuario logeado!!.");
            } else {
                res.status(401);
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
            }

            return new ModelAndView(model, "message.mustache");
        }, new MustacheTemplateEngine());
    }
}

