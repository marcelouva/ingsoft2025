-- Elimina la tabla 'users' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS users;

-- Crea la tabla 'users' con los campos originales, adaptados para SQLite
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,      -- Nombre de usuario para login
    password TEXT NOT NULL        -- Contraseña hasheada
);



-- Elimina la tabla 'students' si ya existe
DROP TABLE IF EXISTS students;

-- Crea la tabla 'students'
CREATE TABLE students (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER UNIQUE NOT NULL,    -- Clave foránea para vincular a la tabla 'users'
    first_name TEXT NOT NULL,           -- Nombre del estudiante
    last_name TEXT NOT NULL,            -- Apellido del estudiante
    student_id_number TEXT UNIQUE NOT NULL, -- Identificador único del estudiante (ej: número de legajo)
    career_name TEXT NOT NULL,          -- Nombre de la carrera que cursa

    -- Definición de la clave foránea
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ---

-- Elimina la tabla 'professors' si ya existe
DROP TABLE IF EXISTS professors;

-- Crea la tabla 'professors'
CREATE TABLE professors (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER UNIQUE NOT NULL,    -- Clave foránea para vincular a la tabla 'users'
    first_name TEXT NOT NULL,           -- Nombre del profesor
    last_name TEXT NOT NULL,            -- Apellido del profesor
    employee_id_number TEXT UNIQUE NOT NULL, -- Identificador único del empleado/profesor (ej: número de legajo o de personal)
    department TEXT,                    -- Departamento o área de especialización (opcional)

    -- Definición de la clave foránea
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

