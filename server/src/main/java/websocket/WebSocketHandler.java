package websocket;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;

import chess.ChessGame;
import chess.InvalidMoveException;
import dataaccess.auth.AuthDAO;
import dataaccess.auth.SqlAuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.game.SqlGameDAO;
import dataaccess.user.SqlUserDAO;
import dataaccess.user.UserDAO;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;


public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();

    UserDAO userDAO;
    AuthDAO authDAO;
    GameDAO gameDAO;

    private void handleException(Session session, Exception e) {
        try {
            session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: " + e.getMessage())));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public WebSocketHandler(UserDAO userDAO, AuthDAO authDao, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDao;
        this.gameDAO = gameDAO;
    }

    public void handleConnection(WsMessageContext ctx) {
        try {
            String message = ctx.message();
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> connect(ctx.session, command);
                case MAKE_MOVE -> {
                    MakeMoveCommand moveCommand = new Gson().fromJson(message, MakeMoveCommand.class);
                    makeMove(ctx.session, moveCommand);
                }
                case LEAVE -> leave(ctx.session, command);
                case RESIGN -> resign(ctx.session, command);
            }
        } catch (Exception e) {
            try {
                ctx.session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: " + e.getMessage())));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void connect(Session session, UserGameCommand command) {
        try{
            AuthData authData = authDAO.getAuth(command.getAuthToken());
            if (authData == null) {
                session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: Invalid auth token")));
                return;
            }

            Integer gameId = command.getGameID();
            GameData gameData = gameDAO.getGame(gameId);
            if (gameData == null) {
                session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: Invalid game ID")));
                return;
            }

            String username = authData.username();

            connections.add(username, gameId, session);

            LoadGameMessage loadGameMessage = new LoadGameMessage(gameData);
            session.getRemote().sendString(new Gson().toJson(loadGameMessage));

            String message = "User " + username + " connected to game " + gameId;
            NotificationMessage notificaiton = new NotificationMessage(message);
            connections.broadcast(username, notificaiton, gameId);

        } catch (Exception e){
            try {
                session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: " + e.getMessage())));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void makeMove(Session session, MakeMoveCommand command) {
        try {
            AuthData authData = authDAO.getAuth(command.getAuthToken());
            if (authData == null) {
                session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: Invalid auth token")));
                return;
            }

            String username = authData.username();
            Integer gameId = command.getGameID();
            GameData gameData = gameDAO.getGame(gameId);
            if (gameData == null) {
                session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: Invalid game ID")));
                return;
            }

            ChessGame game = gameData.game();

            if (game.isGameOver()) {
                session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: Game is already over")));
                return;
            }

            if (game.getTeamTurn() == ChessGame.TeamColor.WHITE && !username.equals(gameData.whiteUsername())) {
                session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: Not your turn")));
                return;
            } else if (game.getTeamTurn() == ChessGame.TeamColor.BLACK && !username.equals(gameData.blackUsername())) {
                String message = "Error: Not your turn";
                session.getRemote().sendString(new Gson().toJson(new ErrorMessage(message)));
                return;
            }

            try {
                game.makeMove(command.getMove());
            } catch (InvalidMoveException e) {
                session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: Invalid move")));
                return;
            }

            ChessGame.TeamColor opponentsColor = game.getTeamTurn() == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

            String notificationMessage = null;
            if (game.isInCheckmate(game.getTeamTurn())) {
                game.setGameOver(true);
                notificationMessage = "Checkmate! " + username + " wins the game!";
            }  else if (game.isInStalemate(game.getTeamTurn())) {
                game.setGameOver(true);
                notificationMessage = "Stalemate! The game is a draw.";
            } else if (game.isInCheck(opponentsColor)) {
                notificationMessage = "Check! " + (game.getTeamTurn() == ChessGame.TeamColor.WHITE ? "White" : "Black") + "'s turn.";
            }

            gameDAO.updateGame(gameData);

            LoadGameMessage loadGameMessage = new LoadGameMessage(gameData);
            connections.broadcast("", loadGameMessage, gameId);

            String message = "User " + username + " made move " + command.getMove() + " in game " + gameId;
            NotificationMessage notification = new NotificationMessage(message);
            connections.broadcast(username, notification, gameId);

        } catch (Exception e){
            handleException(session, e);
        }
    }

    private void leave(Session session, UserGameCommand command) {
        try {
            AuthData authData = authDAO.getAuth(command.getAuthToken());
            if (authData == null) {
                session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: Invalid auth token")));
                return;
            }

            String username = authData.username();
            Integer gameId = command.getGameID();

            connections.remove(username);

            GameData gameData = gameDAO.getGame(gameId);
            if(gameData != null) {
                if(username.equals(gameData.whiteUsername())){
                    gameData = new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game());
                    gameDAO.updateGame(gameData);
                } else if (username.equals(gameData.blackUsername())) {
                    gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
                    gameDAO.updateGame(gameData);
                }
            }

            String message = "User " + username + " left game " + gameId;
            NotificationMessage notification = new NotificationMessage(message);
            connections.broadcast(username, notification, gameId);

        } catch (Exception e){
            handleException(session, e);
        }
    }

    private void resign(Session session, UserGameCommand command) {
        try {
            AuthData authData = authDAO.getAuth(command.getAuthToken());
            if (authData == null) {
                session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: Invalid auth token")));
                return;
            }

            String username = authData.username();
            Integer gameId = command.getGameID();
            GameData gameData = gameDAO.getGame(gameId);
            if (gameData == null) {
                session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: Invalid game ID")));
                return;
            }

            if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
                session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: You are not a player in this game")));
                return;
            }

            ChessGame game = gameData.game();

            if (game.isGameOver()){
                session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: Game is already over")));
                return;
            }

            game.setGameOver(true);

            gameDAO.updateGame(gameData);

            String message = "User " + username + " has resigned from game " + gameId;
            NotificationMessage notification = new NotificationMessage(message);
            connections.broadcast("", notification, gameId);

        } catch (Exception e){
            handleException(session, e);
        }
    }
}