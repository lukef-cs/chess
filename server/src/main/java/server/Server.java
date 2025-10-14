package server;

import io.javalin.*;
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

    public Server() {

        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        this.userService = new UserService(userDAO, authDAO);
        this.clearService = new ClearService(userDAO, gameDAO, authDAO);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.post("/user", ctx -> {
            try {
                RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
                RegisterResult result = userService.register(request);
                ctx.status(200).result(gson.toJson(result)).contentType("application/json");
            } catch (ServiceException e) {
                if (e.getMessage().contains("bad request")) {
                    ctx.status(400).json(new ServiceException("Error: bad request"));
                } else if (e.getMessage().contains("already taken")) {
                    ctx.status(403).json(new ServiceException("Error: already taken"));
                } else {
                    ctx.status(500).json(new ServiceException("Error: " + e.getMessage()));
                }
            }
        });
        // Register your endpoints and exception handlers here.

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
