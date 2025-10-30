package service;

import dataaccess.auth.AuthDAO;
import dataaccess.auth.MemoryAuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.game.MemoryGameDAO;
import dataaccess.user.UserDAO;
import dataaccess.user.MemoryUserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.requests.CreateGameRequest;
import service.requests.RegisterRequest;
import service.requests.LoginRequest;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {
    private ClearService clearService;
    private UserDAO userDAO;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private UserService userService;
    private GameService gameService;

    @BeforeEach
    public void setUp() throws Exception {
        userDAO = new MemoryUserDAO();
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();

        clearService = new ClearService(userDAO, gameDAO, authDAO);
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);

        // Add some test data
        userService.register(new RegisterRequest("hello", "pass1", "hello@example.com"));
        userService.register(new RegisterRequest("there", "pass2", "there@example.com"));

        String user1Token = userService.login(new LoginRequest("hello", "pass1")).authToken();
        gameService.createGame(new CreateGameRequest("Game 1"), user1Token);
        gameService.createGame(new CreateGameRequest("Game 2"), user1Token);
    }

    @Test
    public void testClearPositive() throws Exception {
        String validToken = userService.login(new LoginRequest("hello", "pass1")).authToken();


        assertNotNull(userDAO.getUser("hello"));
        assertNotNull(userDAO.getUser("there"));
        assertEquals(2, gameService.listGames(validToken).games().size());

        clearService.clear();

        assertNull(userDAO.getUser("hello"));
        assertNull(userDAO.getUser("there"));

        assertTrue(gameDAO.listGames().isEmpty());
    }

    @Test
    public void testClearRemovesAuthTokens() throws Exception {
        String authToken = userService.login(new LoginRequest("hello", "pass1")).authToken();
        assertNotNull(authDAO.getAuth(authToken));

        clearService.clear();

        assertNull(authDAO.getAuth(authToken));
    }

}
