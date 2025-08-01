-- Elimina la tabla 'users' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS users;

-- Crea la tabla 'users' con los campos originales, adaptados para SQLite
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,      -- Nombre de usuario para login
    password TEXT NOT NULL,        -- Contraseña hasheada
    role INTEGER DEFAULT 0 -- Atributo  role 2 adm, role 1 profesor y role 0 estudiante
);

-- Elimina la tabla 'professors' si ya existe
DROP TABLE IF EXISTS professors;

-- Crea la tabla 'professors'
CREATE TABLE professors (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    id_employee TEXT NOT NULL UNIQUE, -- Número de empleado, debe ser único
    user_id INTEGER NOT NULL UNIQUE,       -- Clave foránea para referenciar a la tabla users
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Elimina la tabla 'students' si ya existe
DROP TABLE IF EXISTS students;

-- Crea la tabla 'students'

CREATE TABLE students (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL UNIQUE,       -- Clave foránea para referenciar a la tabla users, debe ser única
    name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    year_of_entry INTEGER NOT NULL,    -- Año de ingreso a la carrera
    career_name TEXT NOT NULL,         -- Nombre de la carrera
    student_id INTEGER NOT NULL UNIQUE, -- ID del estudiante, debe ser único
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabla 'subjects'
-- Elimina la tabla 'subjects' si ya existe
DROP TABLE IF EXISTS subjects;

-- Crea la tabla 'subjects'
CREATE TABLE subjects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE -- Nombre de la materia
    -- Puedes añadir otros campos para la materia aquí si es necesario
);

-- Tabla de unión 'professors_subjects'
-- Elimina la tabla de unión si ya existe
DROP TABLE IF EXISTS professors_subjects;

-- Crea la tabla de unión para la relación N a N
CREATE TABLE professors_subjects (
    professor_id INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    PRIMARY KEY (professor_id, subject_id), -- Clave primaria compuesta para asegurar unicidad
    FOREIGN KEY (professor_id) REFERENCES professors(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);
