package server;

import io.javalin.*;

import java.util.Map;

import com.google.gson.Gson;

import dataaccess.auth.AuthDAO;
import dataaccess.auth.MemoryAuthDAO;

import dataaccess.user.UserDAO;
import dataaccess.user.MemoryUserDAO;

import dataaccess.game.GameDAO;
import dataaccess.game.MemoryGameDAO;
import service.*;
import service.requests.*;
import service.results.*;

public class Server {

    private final Javalin javalin;
    private final Gson gson = new Gson();
    private final UserService userService;
    private final ClearService clearService;
    private final GameService gameService;

    public Server() {

        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        this.userService = new UserService(userDAO, authDAO);
        this.clearService = new ClearService(userDAO, gameDAO, authDAO);
        this.gameService = new GameService(gameDAO, authDAO);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.post("/user", ctx -> {
            try {
                RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
                RegisterResult result = userService.register(request);
                ctx.status(200).result(gson.toJson(result)).contentType("application/json");
            } catch (ServiceException e) {
                if (e.getMessage().contains("bad request")) {
                    ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request"))).contentType("application/json");
                } else if (e.getMessage().contains("already taken")) {
                    ctx.status(403).result(gson.toJson(Map.of("message", "Error: already taken"))).contentType("application/json");
                } else {
                    ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage()))).contentType("application/json");
                }
            }
        });
        // Register your endpoints and exception handlers here.

        javalin.post("/session", ctx -> {  // Use /session as per rubric
            try {
                LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
                LoginResult result = userService.login(request);
                ctx.status(200).result(gson.toJson(result)).contentType("application/json");
            } catch (ServiceException e) {
                System.out.println(e.getMessage());
                if(e.getMessage().contains("bad request")){

                    // WE'RE USING MAP.OF, idk if we should be using that


                    ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request"))).contentType("application/json");
                } else {
                    ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized"))).contentType("application/json");
                }
            }
        });

        javalin.delete("/session", ctx -> {
            try {
                String authToken = ctx.header("Authorization");
                if (authToken == null) {
                    ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized"))).contentType("application/json");
                    return;
                }
                LogoutRequest request = new LogoutRequest(authToken);  // Assuming LogoutRequest(String authToken)
                userService.logout(request);
                ctx.status(200);
            } catch (ServiceException e) {
                ctx.status(401).result(gson.toJson(Map.of("message", "Error: " + e.getMessage()))).contentType("application/json");
            }
        });

        javalin.post("/game", ctx -> {
            try {
                CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
                String authToken = ctx.header("Authorization");
                CreateGameResult result = gameService.createGame(request, authToken);
                ctx.status(200).result(gson.toJson(result)).contentType("application/json");
            } catch (ServiceException e) {
                if (e.getMessage().contains("unauthorized")) {
                    ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized"))).contentType("application/json");
                } else if (e.getMessage().contains("bad request")) {
                    ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request"))).contentType("application/json");
                } else {
                    ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage()))).contentType("application/json");
                }
            }
        });

        javalin.put("/game", ctx -> {
            try {
                JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
                String authToken = ctx.header("Authorization");
                gameService.joinGame(request, authToken);
                ctx.status(200);
            } catch (ServiceException e) {
                if (e.getMessage().contains("unauthorized")) {
                    ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized"))).contentType("application/json");
                } else if (e.getMessage().contains("bad request")) {
                    ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request"))).contentType("application/json");
                } else if (e.getMessage().contains("already taken")) {
                    ctx.status(403).result(gson.toJson(Map.of("message", "Error: already taken"))).contentType("application/json");
                } else {
                    ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage()))).contentType("application/json");
                }
            }
        });

        javalin.get("/game", ctx -> {
            try {
                String authToken = ctx.header("Authorization");
                ListGamesResult result = gameService.listGames(authToken);
                ctx.status(200).result(gson.toJson(result)).contentType("application/json");
            } catch (ServiceException e) {
                if (e.getMessage().contains("unauthorized")) {
                    ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized"))).contentType("application/json");
                } else {
                    ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage()))).contentType("application/json");
                }
            }
        });

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
