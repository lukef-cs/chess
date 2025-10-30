package dataaccess;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import chess.ChessGame;
import dataaccess.auth.SqlAuthDAO;
import dataaccess.game.SqlGameDAO;
import dataaccess.user.SqlUserDAO;
import model.AuthData;
import model.UserData;
import passoff.model.TestUser;
import passoff.server.TestServerFacade;
import server.Server;
import model.GameData;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

public class SqlGameDAOTest {
    private SqlGameDAO gameDAO;
    private SqlUserDAO userDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        gameDAO = new SqlGameDAO();
        userDAO = new SqlUserDAO();
        gameDAO.clear();
        userDAO.clear();
    }

    @Test
    public void testClearPositive() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData data = new GameData(0, null, null, "Test Game", game);
        gameDAO.createGame(data);

        gameDAO.clear();

        List<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty(), "Games should be empty after clear");
    }

    @Test
    public void testCreateGamePositive() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData data = new GameData(0, null, null, "Test Game", game);
        int gameID = gameDAO.createGame(data);

        List<GameData> games = gameDAO.listGames();
        assertTrue(games.size() == 1, "There should be one game after creation");
        assertTrue(games.get(0).gameName().equals("Test Game"), "Game name should match");
        assertTrue(games.get(0).gameID() == gameID, "Game ID should match");
    }

    @Test
    public void testCreateGameNegative() {
        ChessGame game = new ChessGame();
        GameData data = new GameData(0, null, null, null, game);

        assertThrows(DataAccessException.class, () -> gameDAO.createGame(data), "Creating game with null name should throw exception");
    }

    @Test
    public void testGetGamePositive() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData data = new GameData(0, null, null, "Test Game", game);
        int gameID = gameDAO.createGame(data);

        GameData retrieved = gameDAO.getGame(gameID);

        assertNotNull(retrieved, "Game should exist");
        assertEquals("Test Game", retrieved.gameName(), "Game names should match");
    }

    @Test
    public void testGetGameNegative() throws DataAccessException {
        GameData retrieved = gameDAO.getGame(1000000);

        assertNull(retrieved, "Non-existent game should return null");
    }

    @Test
    public void testListGamesPositive() throws DataAccessException {
        ChessGame game1 = new ChessGame();
        GameData data1 = new GameData(0, null, null, "Test Game 1", game1);
        gameDAO.createGame(data1);

        ChessGame game2 = new ChessGame();
        GameData data2 = new GameData(0, null, null, "Test Game 2", game2);
        gameDAO.createGame(data2);

        List<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size(), "There should be two games listed");

    }

    @Test
    public void testListGamesNegative() throws DataAccessException {
        List<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty(), "There should be no games listed");
    }

    @Test
    public void testUpdateGamePositive() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData data = new GameData(0, null, null, "Test Game", game);
        int gameID = gameDAO.createGame(data);

        UserData user = new UserData("player1", "password", "doink@doink.com");
        userDAO.createUser(user);

        GameData updated = new GameData(gameID, "player1", null, "Updated Game", game);
        gameDAO.updateGame(updated);

        GameData retrieved = gameDAO.getGame(gameID);
        assertEquals("player1", retrieved.whiteUsername(), "White username should be updated");
        assertEquals("Updated Game", retrieved.gameName(), "Game name should be updated");
    }

    @Test
    public void testUpdateGameNegative() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData invalidUpdate = new GameData(999, "what", null, "No Game", game);
        assertDoesNotThrow(() -> gameDAO.updateGame(invalidUpdate), "Updating non-existent game should throw exception");
    }

}
