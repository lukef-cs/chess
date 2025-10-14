package service;

import chess.ChessGame;
import dataaccess.auth.AuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.results.CreateGameResult;
import service.results.ListGamesResult;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public CreateGameResult createGame(CreateGameRequest request, String authToken) throws ServiceException {
        try {
            // Validate auth token
            if (authDAO.getAuth(authToken) == null) {
                throw new ServiceException("Error: unauthorized");
            }

            // Validate input
            if (request.gameName() == null) {
                throw new ServiceException("Error: bad request");
            }

            // Create new game
            ChessGame game = new ChessGame();
            GameData newGame = new GameData(0, null, null, request.gameName(), game);
            int gameID = gameDAO.createGame(newGame);

            return new CreateGameResult(gameID);
        } catch (DataAccessException e) {
            throw new ServiceException("Error: " + e.getMessage());
        }
    }

    public ListGamesResult listGames(String authToken) throws ServiceException {
        try {
            // Validate auth token
            if (authDAO.getAuth(authToken) == null) {
                throw new ServiceException("Error: unauthorized");
            }

            return new ListGamesResult(gameDAO.listGames());
        } catch (DataAccessException e) {
            throw new ServiceException("Error: " + e.getMessage());
        }
    }

    public void joinGame(JoinGameRequest request, String authToken) throws ServiceException {
        try {
            // Validate auth token
            AuthData authData = authDAO.getAuth(authToken);
            if (authData == null) {
                throw new ServiceException("Error: unauthorized");
            }

            // Validate input
            if (request.playerColor() == null) {
                throw new ServiceException("Error: bad request");
            }

            // Get game
            GameData game = gameDAO.getGame(request.gameID());
            if (game == null) {
                throw new ServiceException("Error: bad request");
            }

            // Check if color is already taken
            String playerColor = request.playerColor().toUpperCase();
            if (playerColor.equals("WHITE")) {
                if (game.whiteUsername() != null) {
                    throw new ServiceException("Error: already taken");
                }
                GameData updatedGame = new GameData(game.gameID(), authData.username(),
                        game.blackUsername(), game.gameName(), game.game());
                gameDAO.updateGame(updatedGame);
            } else if (playerColor.equals("BLACK")) {
                if (game.blackUsername() != null) {
                    throw new ServiceException("Error: already taken");
                }
                GameData updatedGame = new GameData(game.gameID(), game.whiteUsername(),
                        authData.username(), game.gameName(), game.game());
                gameDAO.updateGame(updatedGame);
            } else {
                throw new ServiceException("Error: bad request");
            }
        } catch (DataAccessException e) {
            throw new ServiceException("Error: " + e.getMessage());
        }
    }
}
