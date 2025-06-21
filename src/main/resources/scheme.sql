-- Elimina la base de datos si ya existe para empezar desde cero
DROP DATABASE IF EXISTS academica_db;

-- Crea la base de datos students_db
CREATE DATABASE academica_db;

-- Selecciona la base de datos students_db para trabajar en ella
USE academica_db;

-- Crea la tabla 'accounts'
CREATE TABLE accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

