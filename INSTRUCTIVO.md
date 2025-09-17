# Instructivo de uso del proyecto `ingsoft2025`

Este documento explica paso a paso cómo utilizar el proyecto de la asignatura de **Ingeniería de Software**.  
El proyecto está desarrollado en **Java**, y utiliza **Spark** (framework web), **Mustache** (motor de plantillas), **SQLite** (base de datos) y **ActiveJDBC** (ORM).  
La compilación y gestión de dependencias se realiza con **Apache Maven**.  

---

## 1. Requisitos previos

Este proyecto utiliza **Java 11** como versión de compilación y depende de varias librerías que se descargarán automáticamente con **Maven**.  
Antes de comenzar, asegurate de tener instalado:

- **Java JDK 11**  
  > El proyecto está configurado en el `pom.xml` para compilar con **Java 11** (`maven.compiler.source` y `target`).  
  > Verificar con:  
  > ```bash
  > java -version
  > ```

- **Apache Maven 3.6+**  
  > Necesario para compilar, ejecutar y gestionar dependencias.  
  > Verificar con:  
  > ```bash
  > mvn -v
  > ```

- **Git**  
  > Para clonar el repositorio y trabajar con control de versiones.  
  > Verificar con:  
  > ```bash
  > git --version
  > ```

- **SQLite 3**  
  > El proyecto utiliza una base de datos SQLite, configurada en el `pom.xml` mediante la propiedad:  
  > `db.url=jdbc:sqlite:./db/dev.db`  
  > Verificar con:  
  > ```bash
  > sqlite3 --version
  > ```

- **Dependencias incluidas en Maven (se descargan automáticamente):**
  - Spark Java (`spark-core` 2.9.4)  
  - Motor de plantillas Mustache (`spark-template-mustache` 2.7.1)  
  - Jackson (`jackson-databind` 2.17.1) para manejo de JSON  
  - BCrypt (`jbcrypt` 0.4) para encriptación de contraseñas  
  - Jakarta Persistence API 3.1.0  
  - ActiveJDBC 3.4-j11 para mapeo objeto-relacional  
  - SQLite JDBC 3.45.1.0  
  - SLF4J 1.7.36 para logging  
  - JUnit 4.13.2 y JUnit Jupiter 5.10.0 para testing  

> 💡 No hace falta instalar estas librerías manualmente: Maven las descargará al momento de compilar.

---

## 2. Forkear el repositorio en GitHub

Cada estudiante debe crear su propia copia del repositorio para trabajar de forma independiente:

1. Ir al repositorio original:  
   👉 [https://github.com/marcelouva/ingsoft2025](https://github.com/marcelouva/ingsoft2025)  
2. Hacer clic en el botón **Fork** (arriba a la derecha).  
3. Seleccionar la cuenta personal de GitHub.  

Esto generará un fork en:  

https://github.com/TU_USUARIO/ingsoft2025


> 💡 El fork permite trabajar sin modificar directamente el proyecto original.

---

## 3. Clonar el fork en la computadora

Para obtener una copia local del fork:

```bash
cd ~/proyectos   # o la carpeta donde quieras guardar el código
git clone https://github.com/TU_USUARIO/ingsoft2025.git
cd ingsoft2025

Cambiar TU_USUARIO por tu nombre de usuario en GitHub.
Si preferís usar SSH, el comando sería:

git clone git@github.com:TU_USUARIO/ingsoft2025.git



## 6. Compilar el proyecto con Maven

Para compilar el código fuente y descargar las dependencias necesarias:

mvn clean package


Este comando genera el JAR ejecutable dentro de la carpeta target/.

## 7. Ejecutar la aplicación

El pom.xml está configurado con el plugin Maven Shade para generar un JAR ejecutable, cuya clase principal es com.is1.proyecto.App.

Ejecutar desde el JAR generado:

java -jar target/proye-is-1.0-SNAPSHOT.jar


-

# 📘 Guía: Cómo agregar una nueva entidad al modelo (ejemplo: `Student`)

En este proyecto trabajamos con **Java + ActiveJDBC + SQLite**.  
Cada vez que agregamos una nueva entidad al modelo, necesitamos reflejar el cambio **tanto en la base de datos como en el código Java**.  
A continuación se explica el flujo paso a paso.

---


## 1. Definir la tabla en la base de datos

El ORM **ActiveJDBC** no crea automáticamente las tablas.  
Por lo tanto, cada nueva entidad requiere que creemos la tabla correspondiente en **SQL**.

### 1.1. Editar `schema.sql`

- Abrí el archivo `src/main/resources/db/schema.sql` (si no existe, crealo).  
- Agregá la definición de la tabla `student` en **singular**, porque ActiveJDBC trabaja con el nombre de la clase en singular y la tabla en plural por convención.  
  > Ejemplo: la clase `Student` se mapea a la tabla `students`.

```sql
-- Crear tabla students
CREATE TABLE students (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    user_id INTEGER UNIQUE, -- relación 1 a 1 con User
    FOREIGN KEY (user_id) REFERENCES users(id)
);


Regla importante:

El nombre de la tabla va en plural (students).

La clase Java va en singular (Student).

2. Actualizar la base de datos dev.db

Una vez modificado el schema.sql, necesitamos aplicar los cambios en la base de datos de desarrollo:

sqlite3 db/dev.db < src/main/resources/db/schema.sql


Esto sobrescribirá o creará la tabla según lo definido en el SQL.
Si querés probar que existe, entrá a la consola de SQLite:

sqlite3 db/dev.db
.tables


Deberías ver la tabla students.

3. Crear el modelo en Java

En el proyecto, las clases que representan entidades se guardan en la carpeta src/main/java/com/is1/proyecto/models/.

3.1. Crear Student.java
package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;

public class Student extends Model {
    // No es necesario agregar atributos aquí.
    // ActiveJDBC mapea automáticamente las columnas de la tabla.
}


