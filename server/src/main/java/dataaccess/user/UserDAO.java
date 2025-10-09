package dataaccess.user;

import dataaccess.DataAccessException;
import model.UserData;

public interface UserDAO {
    void clear() throws DataAccessException;
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
}
