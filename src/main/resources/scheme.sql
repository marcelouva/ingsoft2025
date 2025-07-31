-- Elimina la tabla 'users' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS users;

-- Crea la tabla 'users' con los campos originales, adaptados para SQLite
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,      -- Nombre de usuario para login
    password TEXT NOT NULL        -- Contraseña hasheada
);

- Elimina la tabla 'professors' si ya existe
DROP TABLE IF EXISTS professors;

-- Crea la tabla 'professors'
CREATE TABLE professors (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    id_employee INTEGER NOT NULL UNIQUE, -- Número de empleado, debe ser único
    user_id INTEGER NOT NULL UNIQUE,       -- Clave foránea para referenciar a la tabla users
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

