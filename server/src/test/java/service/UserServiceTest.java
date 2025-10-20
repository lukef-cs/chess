package service;

import dataaccess.auth.AuthDAO;
import dataaccess.auth.MemoryAuthDAO;
import dataaccess.user.UserDAO;
import dataaccess.user.MemoryUserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserService class.
 * Tests register, login, and logout operations.
 */
public class UserServiceTest {
    private UserService userService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    // Register Tests

    @Test
    public void testRegisterPositive() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("testuser", "password123", "test@example.com");

        // Call the function
        RegisterResult result = userService.register(request);

        // Assert - make sure everything checks out
        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isEmpty());

        // Verify user was added to database
        UserData user = userDAO.getUser("testuser");
        assertNotNull(user);
        assertEquals("testuser", user.username());
        assertEquals("password123", user.password());
        assertEquals("test@example.com", user.email());
    }

    @Test
    public void testRegisterNegativeDuplicateUsername() {
        // Arrange
        RegisterRequest request1 = new RegisterRequest("testuser", "password123", "test@example.com");
        RegisterRequest request2 = new RegisterRequest("testuser", "different", "other@example.com");

        // Call the function & Assert
        try {
            userService.register(request1);
            userService.register(request2);
            fail("Expected ServiceException for duplicate username");
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("already taken"));
        }
    }

    // login requests

    @Test
    public void testLoginPositive() throws Exception {
        // Arrange - first register a user
        userService.register(new RegisterRequest("testuser", "password123", "test@example.com"));

        // Call the function
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        LoginResult result = userService.login(loginRequest);

        // Assert - we good?
        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isEmpty());

        // Verify auth token was created
        AuthData auth = authDAO.getAuth(result.authToken());
        assertNotNull(auth);
        assertEquals("testuser", auth.username());
    }

    @Test
    public void testLoginNegativeWrongPassword() throws Exception {
        // Arrange - first register a user
        userService.register(new RegisterRequest("testuser", "password123", "test@example.com"));

        // Call the function & Assert
        try {
            LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");
            userService.login(loginRequest);
            fail("Expected ServiceException for wrong password");
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("unauthorized"));
        }
    }

    // Logout Tests

    @Test
    public void testLogoutPositive() throws Exception {
        // Arrange - register and login to get auth token
        userService.register(new RegisterRequest("testuser", "password123", "test@example.com"));
        LoginResult loginResult = userService.login(new LoginRequest("testuser", "password123"));
        String authToken = loginResult.authToken();

        // Verify auth token exists
        AuthData authBefore = authDAO.getAuth(authToken);
        assertNotNull(authBefore);

        // call the function
        LogoutRequest logoutRequest = new LogoutRequest(authToken);
        userService.logout(logoutRequest);

        // Assert - auth token should be deleted
        AuthData authAfter = authDAO.getAuth(authToken);
        assertNull(authAfter);
    }

    @Test
    public void testLogoutNegativeInvalidToken() {
        // Arrange
        LogoutRequest logoutRequest = new LogoutRequest("invalid-token-12345");

        // Call the function & Assert
        try {
            userService.logout(logoutRequest);
            // No error means we failed
            fail("Expected ServiceException for invalid auth token");
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("unauthorized"));
        }
    }

}
