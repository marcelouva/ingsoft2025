-- Elimina la tabla 'users' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS users;

-- Crea la tabla 'users' con los campos originales, adaptados para SQLite
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,      -- Nombre de usuario para login
    password TEXT NOT NULL,        -- Contraseña hasheada
    email TEXT UNIQUE,                  -- Opcional: correo electrónico del usuario
    is_active BOOLEAN DEFAULT 1,        -- Opcional: para habilitar/deshabilitar cuentas
    admin BOOLEAN DEFAULT 0,            -- Nuevo campo: rol de administrador, por defecto falso (0 para booleano en SQLite)
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE people (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    dni TEXT UNIQUE NOT NULL, -- Nuevo campo DNI: TEXT, ÚNICO y NO NULO
    birth_date TEXT,
    user_id INTEGER UNIQUE, 
    FOREIGN KEY (user_id) REFERENCES users(id)

);


CREATE TABLE IF NOT EXISTS students (
    id INTEGER PRIMARY KEY, -- La clave primaria será también clave foránea a people.id
    student_code TEXT UNIQUE NOT NULL,
    FOREIGN KEY (id) REFERENCES people(id) -- Referencia a la persona base
);

CREATE TABLE IF NOT EXISTS professors (
    id INTEGER PRIMARY KEY, -- La clave primaria será también clave foránea a people.id
    admission_year INTEGER NOT NULL,
    FOREIGN KEY (id) REFERENCES people(id) -- Referencia a la persona base
);



CREATE TABLE subjects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT
);

CREATE TABLE people_subjects (
    person_id INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    PRIMARY KEY (person_id, subject_id),
    FOREIGN KEY (person_id) REFERENCES people(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
);






