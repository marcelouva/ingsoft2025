-- Elimina la tabla 'users' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS users;

-- Crea la tabla 'users' con los campos originales, adaptados para SQLite
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,      -- Nombre de usuario para login
    password TEXT NOT NULL,        -- Contraseña hasheada
    email TEXT UNIQUE,                  -- Opcional: correo electrónico del usuario
    is_active BOOLEAN DEFAULT 1,        -- Opcional: para habilitar/deshabilitar cuentas
    rol INTEGER NOT NULL DEFAULT 0,            -- : rol 0 estudiante - 1 profesor  - 2 administrador
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




CREATE TABLE subjects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT
);


-- Tabla de unión para la relación muchos a muchos entre usuarios y materias
CREATE TABLE users_subjects (
    user_id INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, subject_id), -- Clave primaria compuesta para asegurar unicidad
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);


