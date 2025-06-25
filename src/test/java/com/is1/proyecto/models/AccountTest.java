package com.is1.proyecto.models;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Esta clase contiene los tests para la entidad Account.
 * Usa JUnit 5 y ActiveJDBC.
 */
public class AccountTest {

    /**
     * Antes de cada test:
     * - Abrimos una conexión a SQLite.
     * - Ejecutamos el esquema SQL para resetear la base.
     */
    @BeforeEach
    void setup() throws Exception {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:test.db", "", "");
        // Limpiamos y creamos la base de datos con el esquema
        String sql = new String(Files.readAllBytes(Paths.get("src/main/resources/scheme.sql")));
        Base.exec(sql);
    }

    /**
     * Después de cada test:
     * - Cerramos la conexión a la base.
     */
    @AfterEach
    void tearDown() {
        Base.close();
    }

    /**
     * Test que verifica que los setters y getters funcionan correctamente.
     */
    @Test
    void testSettersAndGetters() {
        Account account = new Account();
        account.setName("Bob");
        account.setPassword("pass456");

        Assertions.assertEquals("Bob", account.getName());
        Assertions.assertEquals("pass456", account.getPassword());
    }

    /**
     * Test que verifica que un Account puede guardarse en la base
     * y luego recuperarse correctamente.
     */
    @Test
    void testSaveAndFindAccount() {
        Account account = new Account();
        account.setName("Alice");
        account.setPassword("secret123");
        account.saveIt();

        Account found = Account.findFirst("name = ?", "Alice");
        Assertions.assertNotNull(found);
        Assertions.assertEquals("Alice", found.getName());
        Assertions.assertEquals("secret123", found.getPassword());
    }

    /**
     * Test que verifica que no se pueden insertar dos Accounts
     * con el mismo nombre de usuario (nombre único en la base).
     */
    @Test
    void testCannotInsertDuplicateAccountName() {
        Account acc1 = new Account();
        acc1.setName("Charlie");
        acc1.setPassword("pass123");
        acc1.saveIt();

        Account acc2 = new Account();
        acc2.setName("Charlie"); // mismo nombre que acc1
        acc2.setPassword("anotherPass");

        // Como el esquema usa UNIQUE en name, debería lanzar DBException
        Assertions.assertThrows(
            DBException.class,
            acc2::saveIt,
            "Se esperaba que insertar un nombre duplicado lanzara una DBException"
        );
    }
}
