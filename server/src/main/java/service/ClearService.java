package service;

import dataaccess.auth.AuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.user.UserDAO;
import dataaccess.DataAccessException;

public class ClearService {
    private final UserDAO userDAO;
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public ClearService(UserDAO userDAO, GameDAO gameDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void clear() throws ServiceException {
        try {
            userDAO.clear();
            gameDAO.clear();
            authDAO.clear();
        } catch (DataAccessException e) {
            throw new ServiceException("Error: " + e.getMessage());
        }
    }
}
