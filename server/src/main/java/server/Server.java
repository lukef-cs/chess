package server;

import com.google.gson.Gson;

import dataaccess.DatabaseManager;
import dataaccess.auth.AuthDAO;
import dataaccess.auth.MemoryAuthDAO;
import dataaccess.auth.SqlAuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.game.MemoryGameDAO;
import dataaccess.game.SqlGameDAO;
import dataaccess.user.UserDAO;
import dataaccess.user.MemoryUserDAO;
import dataaccess.user.SqlUserDAO;
import io.javalin.Javalin;
import io.javalin.http.Context;
import service.ClearService;
import service.GameService;
import service.ServiceException;
import service.UserService;
import websocket.WebSocketHandler;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.LoginRequest;
import requests.LogoutRequest;
import requests.RegisterRequest;
import results.CreateGameResult;
import results.ListGamesResult;
import results.LoginResult;
import results.RegisterResult;
import model.AuthData;

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

    private WebSocketHandler webSocketHandler;

    private AuthDAO authDAO;

    public Server() {
        try {
            DatabaseManager.createDatabase();
            DatabaseManager.createTables();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }

        UserDAO userDAO = new SqlUserDAO();
        authDAO = new SqlAuthDAO();
        GameDAO gameDAO = new SqlGameDAO();

        // Initialize services
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        clearService = new ClearService(userDAO, gameDAO, authDAO);

        // Initialize WebSocket handler
        webSocketHandler = new WebSocketHandler(userDAO, authDAO, gameDAO);

        // Create Javalin server
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Set up endpoints
        setupRoutes();
    }

    private void checkAuth(String authToken, Context ctx) throws ServiceException {
        try{
            AuthData auth = authDAO.getAuth(authToken);
            if (auth == null) {
                ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized"))).contentType("application/json");
                return;
            }
        } catch(Exception e){
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized"))).contentType("application/json");
            return;
        }
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

        javalin.ws("/ws", ws -> {
            ws.onMessage(webSocketHandler::handleConnection);
        });
    }

    private void returnStatus(ServiceException e, Context ctx) {
        String message = e.getMessage();
        if (message.contains("unauthorized")) {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized"))).contentType("application/json");
        } else if (message.contains("invalid credentials")) {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: invalid credentials"))).contentType("application/json");
        } else if (message.contains("bad request")) {
            ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request"))).contentType("application/json");
        } else {
            ctx.status(500).result(gson.toJson(Map.of("message", message))).contentType("application/json");
        }
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
            } else if (message.contains("already exists")) {
                ctx.status(403).result(gson.toJson(Map.of("message", "Error: already exists"))).contentType("application/json");
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
            returnStatus(e, ctx);
        }
    }

    private void handleLogout(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            checkAuth(authToken, ctx);

            LogoutRequest request = new LogoutRequest(authToken);
            userService.logout(request);
            ctx.status(200);
        } catch (ServiceException e) {
            returnStatus(e, ctx);
        }
    }

    private void handleCreateGame(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            checkAuth(authToken, ctx);
            CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
            CreateGameResult result = gameService.createGame(request, authToken);
            ctx.status(200).result(gson.toJson(result)).contentType("application/json");
        } catch (ServiceException e) {
            returnStatus(e, ctx);
        }
    }

    private void handleListGames(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            checkAuth(authToken, ctx);
            ListGamesResult result = gameService.listGames(authToken);
            ctx.status(200).result(gson.toJson(result)).contentType("application/json");
        } catch (ServiceException e) {
            returnStatus(e, ctx);
        }
    }

    private void handleJoinGame(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            checkAuth(authToken, ctx);
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
