package service;

import dataaccess.auth.AuthDAO;
import dataaccess.auth.MemoryAuthDAO;
import dataaccess.user.UserDAO;
import dataaccess.user.MemoryUserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.LoginRequest;
import requests.LogoutRequest;
import requests.RegisterRequest;
import results.LoginResult;
import results.RegisterResult;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

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


    @Test
    public void testRegisterPositive() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "password123", "test@example.com");

        RegisterResult result = userService.register(request);

        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isEmpty());

        UserData user = userDAO.getUser("testuser");
        assertNotNull(user);
        assertEquals("testuser", user.username());
        assertTrue(BCrypt.checkpw("password123", user.password()));
        assertEquals("test@example.com", user.email());
    }

    @Test
    public void testRegisterNegativeDuplicateUsername() {
        RegisterRequest request1 = new RegisterRequest("testuser", "password123", "test@example.com");
        RegisterRequest request2 = new RegisterRequest("testuser", "different", "other@example.com");

        try {
            userService.register(request1);
            userService.register(request2);
            fail("Expected ServiceException for duplicate username");
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("already exists"));
        }
    }


    @Test
    public void testLoginPositive() throws Exception {
        userService.register(new RegisterRequest("testuser", "password123", "test@example.com"));

        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        LoginResult result = userService.login(loginRequest);

        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isEmpty());

        AuthData auth = authDAO.getAuth(result.authToken());
        assertNotNull(auth);
        assertEquals("testuser", auth.username());
    }

    @Test
    public void testLoginNegativeWrongPassword() throws Exception {
        userService.register(new RegisterRequest("testuser", "password123", "test@example.com"));

        try {
            LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");
            userService.login(loginRequest);
            fail("Expected ServiceException for wrong password");
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("invalid credentials"));
        }
    }


    @Test
    public void testLogoutPositive() throws Exception {
        userService.register(new RegisterRequest("testuser", "password123", "test@example.com"));
        LoginResult loginResult = userService.login(new LoginRequest("testuser", "password123"));
        String authToken = loginResult.authToken();

        AuthData authBefore = authDAO.getAuth(authToken);
        assertNotNull(authBefore);

        LogoutRequest logoutRequest = new LogoutRequest(authToken);
        userService.logout(logoutRequest);

        AuthData authAfter = authDAO.getAuth(authToken);
        assertNull(authAfter);
    }

    @Test
    public void testLogoutNegativeInvalidToken() {
        LogoutRequest logoutRequest = new LogoutRequest("invalid-token-12345");

        try {
            userService.logout(logoutRequest);
            fail("Expected ServiceException for invalid auth token");
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("unauthorized"));
        }
    }

}
