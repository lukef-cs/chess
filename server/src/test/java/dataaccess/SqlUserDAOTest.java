package dataaccess;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import dataaccess.auth.SqlAuthDAO;
import dataaccess.user.SqlUserDAO;
import passoff.model.TestUser;
import passoff.server.TestServerFacade;
import server.Server;
import model.AuthData;
import model.UserData;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class SqlUserDAOTest {

    private SqlUserDAO userDAO;

    @BeforeEach
    public void setUp() throws DataAccessException{
        userDAO = new SqlUserDAO();

        userDAO.clear();

        String hashedPassword = BCrypt.hashpw("validPassword", BCrypt.gensalt());
        UserData testUser = new UserData("testUser", hashedPassword, "tu@luke.com");

        userDAO.createUser(testUser);
    }

    @Test
    public void testClearPositive() throws DataAccessException {
        userDAO.clear();

        UserData retrieved = userDAO.getUser("testUser");
        assertNull(retrieved, "User should be null after clear");
    }

    @Test
    public void testCreateUserPositive() throws DataAccessException {
        String hashedPassword = BCrypt.hashpw("newPassword", BCrypt.gensalt());
        UserData newUser = new UserData("newUser", hashedPassword, "nu@luke.com");
        userDAO.createUser(newUser);

        UserData retrieved = userDAO.getUser("newUser");
        assertNotNull(retrieved, "User should exist after creation");
        assertEquals("newUser", retrieved.username(), "Usernames should match");
        assertTrue(BCrypt.checkpw("newPassword", retrieved.password()), "Passwords should match");
        assertEquals("nu@luke.com", retrieved.email(), "Emails should match");
    }

    @Test
    public void testCreateUserNegative() {
        UserData duplicateUser = new UserData("testUser", "anotherPassword", "tu@luke.com");

        assertThrows(DataAccessException.class, () -> userDAO.createUser(duplicateUser), "Duplicate user should throw exception");
    }

    @Test
    public void testGetUserPositive() throws DataAccessException {
        UserData retrieved = userDAO.getUser("testUser");

        assertNotNull(retrieved, "User should exist");
        assertEquals("testUser", retrieved.username(), "Usernames should match");
    }

    @Test
    public void testGetUserNegative() throws DataAccessException {
        UserData retrieved =  userDAO.getUser("doink");
        assertNull(retrieved, "User should be null for non-existent username");
    }
}