package server;

import com.google.gson.Gson;
import dataaccess.auth.AuthDAO;
import dataaccess.auth.MemoryAuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.game.MemoryGameDAO;
import dataaccess.user.UserDAO;
import dataaccess.user.MemoryUserDAO;
import io.javalin.Javalin;
import io.javalin.http.Context;
import service.ClearService;
import service.GameService;
import service.ServiceException;
import service.UserService;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.CreateGameResult;
import service.results.ListGamesResult;
import service.results.LoginResult;
import service.results.RegisterResult;

import java.util.Map;

/**
 * HTTP server for the chess application.
 * Handles all API endpoints for user authentication, game management, and database operations.
 */
public class Server {
    private Javalin javalin;
    private Gson gson = new Gson();
    private UserService userService;
    private GameService gameService;
    private ClearService clearService;

    public Server() {
        // Initialize data access objects
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        // Initialize services
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        clearService = new ClearService(userDAO, gameDAO, authDAO);

        // Create Javalin server
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Set up endpoints
        setupRoutes();
    }

    private void setupRoutes() {
        // User endpoints
        javalin.post("/user", ctx -> handleRegister(ctx));
        javalin.post("/session", ctx -> handleLogin(ctx));
        javalin.delete("/session", ctx -> handleLogout(ctx));

        // Game endpoints
        javalin.post("/game", ctx -> handleCreateGame(ctx));
        javalin.get("/game", ctx -> handleListGames(ctx));
        javalin.put("/game", ctx -> handleJoinGame(ctx));

        // Database endpoints
        javalin.delete("/db", ctx -> handleClear(ctx));
    }

    private void handleRegister(Context ctx) {
        try {
            RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
            RegisterResult result = userService.register(request);
            ctx.status(200).result(gson.toJson(result)).contentType("application/json");
        } catch (ServiceException e) {
            String message = e.getMessage();
            if (message.contains("bad request")) {
                ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request"))).contentType("application/json");
            } else if (message.contains("already taken")) {
                ctx.status(403).result(gson.toJson(Map.of("message", "Error: already taken"))).contentType("application/json");
            } else {
                ctx.status(500).result(gson.toJson(Map.of("message", message))).contentType("application/json");
            }
        }
    }

    private void handleLogin(Context ctx) {
        try {
            LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
            LoginResult result = userService.login(request);
            ctx.status(200).result(gson.toJson(result)).contentType("application/json");
        } catch (ServiceException e) {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized"))).contentType("application/json");
        }
    }

    private void handleLogout(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            if (authToken == null) {
                ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized"))).contentType("application/json");
                return;
            }
            LogoutRequest request = new LogoutRequest(authToken);
            userService.logout(request);
            ctx.status(200);
        } catch (ServiceException e) {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized"))).contentType("application/json");
        }
    }

    private void handleCreateGame(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
            CreateGameResult result = gameService.createGame(request, authToken);
            ctx.status(200).result(gson.toJson(result)).contentType("application/json");
        } catch (ServiceException e) {
            String message = e.getMessage();
            if (message.contains("unauthorized")) {
                ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized"))).contentType("application/json");
            } else if (message.contains("bad request")) {
                ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request"))).contentType("application/json");
            } else {
                ctx.status(500).result(gson.toJson(Map.of("message", message))).contentType("application/json");
            }
        }
    }

    private void handleListGames(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            ListGamesResult result = gameService.listGames(authToken);
            ctx.status(200).result(gson.toJson(result)).contentType("application/json");
        } catch (ServiceException e) {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized"))).contentType("application/json");
        }
    }

    private void handleJoinGame(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
            gameService.joinGame(request, authToken);
            ctx.status(200);
        } catch (ServiceException e) {
            String message = e.getMessage();
            if (message.contains("unauthorized")) {
                ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized"))).contentType("application/json");
            } else if (message.contains("bad request")) {
                ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request"))).contentType("application/json");
            } else if (message.contains("already taken")) {
                ctx.status(403).result(gson.toJson(Map.of("message", "Error: already taken"))).contentType("application/json");
            } else {
                ctx.status(500).result(gson.toJson(Map.of("message", message))).contentType("application/json");
            }
        }
    }

    private void handleClear(Context ctx) {
        try {
            clearService.clear();
            ctx.status(200);
        } catch (ServiceException e) {
            ctx.status(500).result(gson.toJson(Map.of("message", e.getMessage()))).contentType("application/json");
        }
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
