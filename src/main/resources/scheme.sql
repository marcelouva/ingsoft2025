-- Elimina la tabla 'accounts' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS accounts;

-- Crea la tabla 'accounts' con los campos originales, adaptados para SQLite
CREATE TABLE accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- Clave primaria autoincremental para SQLite
    name TEXT NOT NULL UNIQUE,          -- Nombre de usuario (TEXT es el tipo de cadena recomendado para SQLite), con restricción UNIQUE
    password TEXT NOT NULL           -- Contraseña hasheada (TEXT es el tipo de cadena recomendado para SQLite)
);