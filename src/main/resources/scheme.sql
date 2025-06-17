-- Elimina la base de datos si ya existe para empezar desde cero
DROP DATABASE IF EXISTS academica_db;

-- Crea la base de datos students_db
CREATE DATABASE academica_db;

-- Selecciona la base de datos students_db para trabajar en ella
USE academica_db;

-- Crea la tabla 'usuarios'
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Opcional: Inserta algunos datos de ejemplo (puedes borrar estas líneas si no los necesitas)
INSERT INTO users (name, password) VALUES ('Alice', 'pass123');
INSERT INTO users (name, password) VALUES ('Bob', 'securepwd');
INSERT INTO users (name, password) VALUES ('Charlie', 'mysecret');

-- Muestra las tablas para verificar que 'usuarios' fue creada (solo para confirmación)
SHOW TABLES;

-- Crear tabla teams
CREATE TABLE IF NOT EXISTS teams (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

-- Agregar columna team_id a users y establecer la clave foránea
ALTER TABLE users
ADD COLUMN team_id INT,
ADD CONSTRAINT fk_team FOREIGN KEY (team_id) REFERENCES teams(id);

-- Insertar algunos equipos de ejemplo
INSERT INTO teams (name, description) VALUES ('Frontend', 'Equipo de desarrollo de interfaz de usuario');
INSERT INTO teams (name, description) VALUES ('Backend', 'Equipo encargado de la lógica del servidor y base de datos');
INSERT INTO teams (name, description) VALUES ('QA', 'Equipo de pruebas de calidad y control de errores');
INSERT INTO teams (name, description) VALUES ('DevOps', 'Equipo de integración y despliegue continuo');

-- Crear tabla de tareas
CREATE TABLE IF NOT EXISTS tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    goal TEXT
);



-- Tabla intermedia para la relación muchos a muchos entre teams y tasks
CREATE TABLE IF NOT EXISTS teams_tasks (
    team_id INT NOT NULL,
    task_id INT NOT NULL,
    PRIMARY KEY (team_id, task_id),
    FOREIGN KEY (team_id) REFERENCES teams(id),
    FOREIGN KEY (task_id) REFERENCES tasks(id)
);


-- Tareas de ejemplo
INSERT INTO tasks (name, goal) VALUES ('Diseñar UI', 'Crear una interfaz de usuario moderna');
INSERT INTO tasks (name, goal) VALUES ('Implementar backend', 'Desarrollar la lógica y base de datos');
INSERT INTO tasks (name, goal) VALUES ('Realizar testing', 'Verificar que las funcionalidades estén completas');
INSERT INTO tasks (name, goal) VALUES ('Desplegar app', 'Publicar la aplicación en producción');

