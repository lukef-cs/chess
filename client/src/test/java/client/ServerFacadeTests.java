package client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import model.AuthData;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static int port;
    private ServerFacade facade;
    private AuthData authData;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void setup() throws Exception {
        facade = new ServerFacade(port);
        facade.clear();
        authData = facade.register("luke", "lukepass", "luke@luke.com");
    }

    @Test
    public void registerPositive() throws Exception {
        var authData = facade.register("registerTest", "password", "test@luke.com");
        assertNotNull(authData.authToken());
        assertEquals("registerTest", authData.username());
    }

    @Test
    public void registerNegative() throws Exception {

        assertThrows(Exception.class, () -> facade.register("luke", "lukepass", "luke@luke.com"));
    }

    @Test
    public void loginPositive() throws Exception {
        var authData = facade.login("luke", "lukepass");
        assertNotNull(authData.authToken());
        assertEquals("luke", authData.username());
    }

    @Test
    public void loginNegative() throws Exception {
        assertThrows(Exception.class, () -> facade.login("none", "wrongpass"));
    }

    @Test
    public void logoutPositive() throws Exception {

        var authData = facade.login("luke", "lukepass");
        assertDoesNotThrow(() -> facade.logout(authData.authToken()));
    }

    @Test
    public void logoutNegative() throws Exception {
        assertThrows(Exception.class, () -> facade.logout("invalid-token"));
    }

    @Test
    public void createGamePositive() throws Exception {

        var authData = facade.login("luke", "lukepass");
        var result = facade.createGame("test game", authData.authToken());

        assertNotNull(result.gameID());
        assertTrue(result.gameID() > 0);
    }

    @Test
    public void createGameNegative() throws Exception {

        assertThrows(Exception.class, () -> facade.createGame("Test", "invalid"));
    }

    @Test
    public void listGamesPositive() throws Exception {

        var authData = facade.login("luke", "lukepass");
        facade.createGame("test game", authData.authToken());

        var result = facade.listGames(authData.authToken());
        assertNotNull(result.games());
        assertEquals(1, result.games().size());
        assertEquals("test game", result.games().get(0).gameName());
    }

    @Test
    public void listGamesNegative() throws Exception {

        assertThrows(Exception.class, () -> facade.listGames("invalid"));
    }

    @Test
    public void joinGamePositive() throws Exception {

        var authData = facade.login("luke", "lukepass");
        var game = facade.createGame("test game", authData.authToken());

        assertDoesNotThrow(() -> facade.joinGame(game.gameID(), "WHITE", authData.authToken()));
    }

    @Test
    public void joinGameNegative() throws Exception {
        var authData = facade.login("luke", "lukepass");
        assertThrows(Exception.class, () -> facade.joinGame(9999, "WHITE", authData.authToken()));
    }

    @Test
    public void clearPositive() throws Exception {
        facade.register("clearTestUser", "password", "email@test.com");
        assertDoesNotThrow(() -> facade.clear());
        assertThrows(Exception.class, () -> facade.login("clearTestUser", "password"));
    }
}