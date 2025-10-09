package dataaccess.user;

import dataaccess.DataAccessException;
import model.UserData;
import java.util.ArrayList;
import java.util.List;

public class MemoryUserDAO implements UserDAO {
    private final List<UserData> users = new ArrayList<>();

    @Override
    public void clear() throws DataAccessException {
        users.clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        users.add(user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        for (UserData user : users) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        return null;
    }
}
