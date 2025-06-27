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