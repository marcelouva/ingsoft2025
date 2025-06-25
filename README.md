Para correr la aplicación con la base de datos dev.db ejecuto:

mvn clean install exec:java

Para correr la aplicación con la base de datos de producción prod.db realizo los siguientes pasos:

Construir el JAR ejecutable: mvn clean package -P prod

Ejecutar el JAR pasando la URL de la base de datos: java -Ddb.url="jdbc:sqlite:./db/prod.db" -jar target/proye-is-1.0-SNAPSHOT.jar

Para que las pruebas se ejecuten con la base de datos de testing test.db:

mvn clean install -P test


Nota: al ejecutar los mvn clena install ... y al intentar accede a la base de dato, por ej. cuando se intenta crear una cuenta de usuario, el archivo se crea solo. Pero luego se debe crear la estructura de tablas que esté disponible en el scheme.sql, esto se realiza ejecutando 
sqlite3 dev.db < .../scheme.sql