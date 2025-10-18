package service;

import dataaccess.auth.AuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.user.UserDAO;
import dataaccess.DataAccessException;

/**
 * Service class for database management operations.
 * Handles clearing all data from the database (useful for testing).
 */
public class ClearService {
    private final UserDAO userDAO;
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public ClearService(UserDAO userDAO, GameDAO gameDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    /**
     * Clear all data from the database.
     * Removes all users, games, and authentication tokens.
     *
     * @throws ServiceException if an error occurs during the clear operation
     */
    public void clear() throws ServiceException {
        try {
            userDAO.clear();
            gameDAO.clear();
            authDAO.clear();
        } catch (DataAccessException exception) {
            throw new ServiceException("Error: " + exception.getMessage());
        }
    }
}
