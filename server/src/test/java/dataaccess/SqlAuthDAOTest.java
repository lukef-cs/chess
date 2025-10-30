package dataaccess;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dataaccess.auth.SqlAuthDAO;
import dataaccess.user.SqlUserDAO;
import model.AuthData;
import model.UserData;
import passoff.model.TestAuthResult;
import passoff.model.TestUser;
import passoff.server.TestServerFacade;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

public class SqlAuthDAOTest {

    private SqlAuthDAO authDAO;
    private SqlUserDAO userDAO;

    @BeforeEach
    public void setUp() throws DataAccessException{
        authDAO = new SqlAuthDAO();
        userDAO = new SqlUserDAO();

        authDAO.clear();
        userDAO.clear();

        UserData testUser = new UserData("testUser", "validPassword", "tu@luke.com");
        AuthData authData = new AuthData("validAuthToken", "testUser");
        userDAO.createUser(testUser);
        authDAO.createAuth(authData);
    }

    @Test
    public void testClearPositive() throws DataAccessException {
        authDAO.clear();

        AuthData retrieved = authDAO.getAuth("validAuthToken");
        assertNull(retrieved, "Auth token should be null after clear");
    }

    @Test
    public void testGetAuthPositive() throws DataAccessException {
        AuthData retrieved = authDAO.getAuth("validAuthToken");

        assertNotNull(retrieved, "Auth token should exist");
        assertEquals("validAuthToken", retrieved.authToken(), "Auth tokens should be the same");
        assertEquals("testUser", retrieved.username(), "Usernames should match");
    }

    @Test
    public void testGetAuthNegative() throws DataAccessException {
        AuthData retrieved =  authDAO.getAuth("invalidAuthToken");
        assertNull(retrieved, "Auth token should be null for invalid token");
    };

    @Test
    public void testCreateAuthPositive() throws DataAccessException {
        AuthData newAuth = new AuthData("newValidAuthToken", "testUser");
        authDAO.createAuth(newAuth);

        AuthData retrieved = authDAO.getAuth("newValidAuthToken");
        assertNotNull(retrieved, "Auth token should exist after creation");
        assertEquals("newValidAuthToken", retrieved.authToken(), "Auth tokens should be the same");
        assertEquals("testUser", retrieved.username(), "Usernames should match");
    }

    @Test
    public void testCreateAuthNegative(){

        AuthData auth = new AuthData("bogus", "nobody");
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(auth), "Creating auth for bogus user should throw exception");
    }

    @Test
    public void testDeleteAuthPositive() throws DataAccessException {
        authDAO.deleteAuth("validAuthToken");

        AuthData retrieved = authDAO.getAuth("validAuthToken");
        assertNull(retrieved, "Auth token should be null after deletion");
    }

    @Test
    public void testDeleteAuthNegative() {
        assertDoesNotThrow(() -> authDAO.deleteAuth("invalidAuthToken"), "Deleting non-existent auth token should not throw exception");
    }
}