package dataaccess.auth;

import dataaccess.DataAccessException;
import model.AuthData;

public interface AuthDAO {
    void clear() throws DataAccessException;
    void createAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
}
