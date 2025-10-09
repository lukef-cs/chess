package dataaccess.auth;

import dataaccess.DataAccessException;
import model.AuthData;
import java.util.ArrayList;
import java.util.List;

public class MemoryAuthDAO implements AuthDAO {
    private final List<AuthData> auths = new ArrayList<>();

    @Override
    public void clear() throws DataAccessException {
        auths.clear();
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        auths.add(auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        for (AuthData auth : auths) {
            if (auth.authToken().equals(authToken)) {
                return auth;
            }
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        auths.removeIf(auth -> auth.authToken().equals(authToken));
    }
}
