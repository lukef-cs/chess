package service;

import dataaccess.auth.AuthDAO;
import dataaccess.auth.MemoryAuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.game.MemoryGameDAO;
import dataaccess.user.UserDAO;
import dataaccess.user.MemoryUserDAO;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.requests.RegisterRequest;
import service.requests.LoginRequest;
import service.results.CreateGameResult;
import service.results.ListGamesResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GameService class.
 * Tests create game, list games, and join game operations.
 */
public class GameServiceTest {
    private GameService gameService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private UserService userService;
    private UserDAO userDAO;
    private String validAuthToken;

    @BeforeEach
    public void setUp() throws Exception {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        gameService = new GameService(gameDAO, authDAO);
        userService = new UserService(userDAO, authDAO);

        // Register and login to get valid auth token
        userService.register(new RegisterRequest("testuser", "password123", "test@example.com"));
        validAuthToken = userService.login(new LoginRequest("testuser", "password123")).authToken();
    }

    // Create Game Tests

    @Test
    public void testCreateGamePositive() throws Exception {
        // Arrange
        CreateGameRequest request = new CreateGameRequest("My very own chess game");

        // Call the function
        CreateGameResult result = gameService.createGame(request, validAuthToken);

        // Assert
        assertNotNull(result);
        assertTrue(result.gameID() > 0);

        // Verify game was added to database
        GameData game = gameDAO.getGame(result.gameID());
        assertNotNull(game);
        assertEquals("My very own chess game", game.gameName());
        assertNull(game.whiteUsername());
        assertNull(game.blackUsername());
        assertNotNull(game.game());
    }

    @Test
    public void testCreateGameNegativeUnauthorized() {
        // Arrange
        CreateGameRequest request = new CreateGameRequest("Chess Game");

        // Call the function & Assert
        try {
            gameService.createGame(request, "invalid-token");
            // We're expecting an error!
            fail("Expected ServiceException for invalid auth token");
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("unauthorized"));
        }
    }

    // Tests for List Games

    @Test
    public void testListGamesPositive() throws Exception {
        // Arrange - create some games
        gameService.createGame(new CreateGameRequest("Game 1"), validAuthToken);
        gameService.createGame(new CreateGameRequest("Game 2"), validAuthToken);
        gameService.createGame(new CreateGameRequest("Game 3"), validAuthToken);

        // Call the function
        ListGamesResult result = gameService.listGames(validAuthToken);

        // Assert
        assertNotNull(result);
        assertNotNull(result.games());
        assertEquals(3, result.games().size());

        // Verify game names
        List<GameData> games = result.games();
        assertTrue(games.stream().anyMatch(g -> "Game 1".equals(g.gameName())));
        assertTrue(games.stream().anyMatch(g -> "Game 2".equals(g.gameName())));
        assertTrue(games.stream().anyMatch(g -> "Game 3".equals(g.gameName())));
    }

    @Test
    public void testListGamesNegativeUnauthorized() {
        // Call the function & Assert
        try {
            gameService.listGames("invalid-token");
            fail("Expected ServiceException for invalid auth token");
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("unauthorized"));
        }
    }

    @Test
    public void testListGamesEmptyList() throws Exception {
        // Call the function
        ListGamesResult result = gameService.listGames(validAuthToken);

        // Assert - should return empty list, not null
        assertNotNull(result);
        assertNotNull(result.games());
        assertEquals(0, result.games().size());
    }

    // testing join game

    @Test
    public void testJoinGamePositiveBothPlayers() throws Exception {
        // Arrange - create two users
        userService.register(new RegisterRequest("hello", "pass1", "user1@example.com"));
        userService.register(new RegisterRequest("there", "pass2", "user2@example.com"));
        String user1Token = userService.login(new LoginRequest("hello", "pass1")).authToken();
        String user2Token = userService.login(new LoginRequest("there", "pass2")).authToken();

        // Create game and join as both players
        CreateGameResult createResult = gameService.createGame(new CreateGameRequest("Two Player Game"), user1Token);
        int gameID = createResult.gameID();

        // Call the function
        gameService.joinGame(new JoinGameRequest("WHITE", gameID), user1Token);
        gameService.joinGame(new JoinGameRequest("BLACK", gameID), user2Token);

        // Assert
        GameData game = gameDAO.getGame(gameID);
        assertEquals("hello", game.whiteUsername());
        assertEquals("there", game.blackUsername());
    }

    @Test
    public void testJoinGameNegativeUnauthorized() throws Exception {
        // Arrange
        CreateGameResult createResult = gameService.createGame(new CreateGameRequest("Test Game"), validAuthToken);
        JoinGameRequest joinRequest = new JoinGameRequest("WHITE", createResult.gameID());

        // Call the function & Assert
        try {
            gameService.joinGame(joinRequest, "invalid-token");
            fail("Expected ServiceException for invalid auth token");
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("unauthorized"));
        }
    }

    @Test
    public void testJoinGameNegativeColorAlreadyTaken() throws Exception {
        // Arrange
        CreateGameResult createResult = gameService.createGame(new CreateGameRequest("Test Game"), validAuthToken);
        int gameID = createResult.gameID();

        // First player joins as WHITE
        gameService.joinGame(new JoinGameRequest("WHITE", gameID), validAuthToken);

        // Call the function & Assert - second attempt to join as WHITE should fail
        try {
            userService.register(new RegisterRequest("there", "pass", "user2@example.com"));
            String user2Token = userService.login(new LoginRequest("there", "pass")).authToken();
            gameService.joinGame(new JoinGameRequest("WHITE", gameID), user2Token);
            fail("Expected ServiceException for already taken color");
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("already taken"));
        }
    }

}
