Para correr la aplicación con la base de datos dev.db ejecuto:

mvn clean install exec:java

Para correr la aplicación con la base de datos de producción prod.db realizo los siguientes pasos:

Construir el JAR ejecutable: mvn clean package -P prod

Ejecutar el JAR pasando la URL de la base de datos: java -Ddb.url="jdbc:sqlite:./db/prod.db" -jar target/proye-is-1.0-SNAPSHOT.jar

Para que las pruebas se ejecuten con la base de datos de testing test.db:

mvn clean install -P test


Nota: al ejecutar los mvn clena install ... y al intentar accede a la base de dato, por ej. cuando se intenta crear una cuenta de usuario, el archivo se crea solo. Pero luego se debe crear la estructura de tablas que esté disponible en el scheme.sql, esto se realiza ejecutando 
sqlite3 dev.db < .../scheme.sql
__________

Guía técnica: ejecutar y desarrollar una app Java en Docker

## Requisitos

* Tener Docker instalado
* Clonar este repositorio

## 1. Construir la imagen Docker


docker build -t miapp-dev .

Esto crea una imagen con todo lo necesario para compilar, testear y ejecutar el proyecto sin instalar herramientas adicionales.

## 2. Ingresar al entorno de desarrollo


docker run -it --rm -v ${PWD}:/app -w /app -p 8080:8080 miapp-dev ./watch.sh

Este comando abre una terminal dentro del contenedor con Maven y Java configurados. Desde allí podés ejecutar:

mvn compile                         # Compilar el código
mvn exec:java -Dexec.mainClass="com.ejemplo.Main"   # Ejecutar la app (reemplazar con tu clase principal)
mvn test                            # Ejecutar los tests

**Nota:**

* En PowerShell, `${PWD}` funciona correctamente.
* En Git Bash o Linux/Mac, usá `$(pwd)` en lugar de `${PWD}`.

## 3. Acceder a la aplicación desde el navegador


docker run -it --rm -v ${PWD}:/app -w /app -p 8080:8080 miapp-dev

Esto permite acceder a la aplicación desde el navegador en:

http://localhost:8080

============================
Pasos para agregar relacion 1 a 1

Voy a agregar la relación 1 a 1 entre los modelos User y Person. Un usuario está relacionado con una persona por el campo id de usuario. 

1) En scheme.sql  debemos incluir la tabla people, donde esta tabla tiene una clave foránea user_id enlazada con id de users.  

2) Creamos el modelo Person.

