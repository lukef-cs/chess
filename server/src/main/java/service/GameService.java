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

/**
 * Service class for game-related operations (create, list, join).
 * Handles game management business logic and player assignment.
 */
public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    /**
     * Create a new chess game.
     *
     * @param request contains the game name
     * @param authToken authentication token of the requesting user
     * @return CreateGameResult containing the newly created game ID
     * @throws ServiceException if user is unauthorized or validation fails
     */
    public CreateGameResult createGame(CreateGameRequest request, String authToken) throws ServiceException {
        try {
            validateAuthToken(authToken);
            validateCreateGameRequest(request);

            ChessGame game = new ChessGame();
            GameData newGame = new GameData(0, null, null, request.gameName(), game);
            int gameID = gameDAO.createGame(newGame);

            return new CreateGameResult(gameID);
        } catch (DataAccessException exception) {
            throw new ServiceException("Error: " + exception.getMessage());
        }
    }

    /**
     * List all available games.
     *
     * @param authToken authentication token of the requesting user
     * @return ListGamesResult containing a list of all games
     * @throws ServiceException if user is unauthorized
     */
    public ListGamesResult listGames(String authToken) throws ServiceException {
        try {
            validateAuthToken(authToken);
            List<GameData> games = gameDAO.listGames();
            return new ListGamesResult(games);
        } catch (DataAccessException exception) {
            throw new ServiceException("Error: " + exception.getMessage());
        }
    }

    /**
     * Join an existing game as a player.
     *
     * @param request contains player color and game ID
     * @param authToken authentication token of the requesting user
     * @throws ServiceException if user is unauthorized, game not found, or color is taken
     */
    public void joinGame(JoinGameRequest request, String authToken) throws ServiceException {
        try {
            AuthData authData = validateAuthTokenAndGet(authToken);
            validateJoinGameRequest(request);

            GameData game = gameDAO.getGame(request.gameID());
            if (game == null) {
                throw new ServiceException("Error: bad request");
            }

            updateGameWithPlayer(game, request.playerColor(), authData.username());
        } catch (DataAccessException exception) {
            throw new ServiceException("Error: " + exception.getMessage());
        }
    }

    private void validateAuthToken(String authToken) throws ServiceException, DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new ServiceException("Error: unauthorized");
        }
    }

    private AuthData validateAuthTokenAndGet(String authToken) throws ServiceException, DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new ServiceException("Error: unauthorized");
        }
        return authData;
    }

    private void validateCreateGameRequest(CreateGameRequest request) throws ServiceException {
        if (request.gameName() == null) {
            throw new ServiceException("Error: bad request");
        }
    }

    private void validateJoinGameRequest(JoinGameRequest request) throws ServiceException {
        if (request.playerColor() == null) {
            throw new ServiceException("Error: bad request");
        }
    }

    private void updateGameWithPlayer(GameData game, String playerColor, String username) 
            throws ServiceException, DataAccessException {
        String normalizedColor = playerColor.toUpperCase();

        if ("WHITE".equals(normalizedColor)) {
            if (game.whiteUsername() != null) {
                throw new ServiceException("Error: already taken");
            }
            GameData updatedGame = new GameData(game.gameID(), username, game.blackUsername(),
                    game.gameName(), game.game());
            gameDAO.updateGame(updatedGame);
        } else if ("BLACK".equals(normalizedColor)) {
            if (game.blackUsername() != null) {
                throw new ServiceException("Error: already taken");
            }
            GameData updatedGame = new GameData(game.gameID(), game.whiteUsername(), username,
                    game.gameName(), game.game());
            gameDAO.updateGame(updatedGame);
        } else {
            throw new ServiceException("Error: bad request");
        }
    }
}
