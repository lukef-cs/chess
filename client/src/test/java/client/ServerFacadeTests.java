package client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static int port;
    private ServerFacade facade;

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
    }

    @Test
    public void registerPositive() throws Exception {
        var authData = facade.register("luke", "luke", "luke");
        assertNotNull(authData.authToken());
        assertEquals("luke", authData.username());
    }

}
