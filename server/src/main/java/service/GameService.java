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
import java.util.List;

public class GameService {
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public CreateGameResult createGame(CreateGameRequest request, String authToken) throws ServiceException {
        try {
            // Check if user is authenticated
            if (authDAO.getAuth(authToken) == null) {
                throw new ServiceException("Error: unauthorized");
            }

            // Validate request
            if (request.gameName() == null) {
                throw new ServiceException("Error: bad request");
            }

            // Create new game
            ChessGame game = new ChessGame();
            GameData newGame = new GameData(0, null, null, request.gameName(), game);
            int gameID = gameDAO.createGame(newGame);

            return new CreateGameResult(gameID);
        } catch (DataAccessException exception) {
            throw new ServiceException("Error: " + exception.getMessage());
        }
    }

    public ListGamesResult listGames(String authToken) throws ServiceException {
        try {
            // Check if user is authenticated
            if (authDAO.getAuth(authToken) == null) {
                throw new ServiceException("Error: unauthorized");
            }

            List<GameData> games = gameDAO.listGames();
            return new ListGamesResult(games);
        } catch (DataAccessException exception) {
            throw new ServiceException("Error: " + exception.getMessage());
        }
    }

    public void joinGame(JoinGameRequest request, String authToken) throws ServiceException {
        try {
            // Check if user is authenticated
            AuthData authData = authDAO.getAuth(authToken);
            if (authData == null) {
                throw new ServiceException("Error: unauthorized");
            }

            // Validate request
            if (request.playerColor() == null) {
                throw new ServiceException("Error: bad request");
            }

            // Get game
            GameData game = gameDAO.getGame(request.gameID());
            if (game == null) {
                throw new ServiceException("Error: bad request");
            }

            // Update game with player
            String playerColor = request.playerColor().toUpperCase();
            if ("WHITE".equals(playerColor)) {
                if (game.whiteUsername() != null) {
                    throw new ServiceException("Error: already taken");
                }
                GameData updatedGame = new GameData(game.gameID(), authData.username(),
                        game.blackUsername(), game.gameName(), game.game());
                gameDAO.updateGame(updatedGame);
            } else if ("BLACK".equals(playerColor)) {
                if (game.blackUsername() != null) {
                    throw new ServiceException("Error: already taken");
                }
                GameData updatedGame = new GameData(game.gameID(), game.whiteUsername(),
                        authData.username(), game.gameName(), game.game());
                gameDAO.updateGame(updatedGame);
            } else {
                throw new ServiceException("Error: bad request");
            }
        } catch (DataAccessException exception) {
            throw new ServiceException("Error: " + exception.getMessage());
        }
    }
}
