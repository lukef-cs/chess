package dataaccess.user;

import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.UserData;

public class SqlUserDAO implements UserDAO{


    @Override
    public void createUser(UserData user) throws DataAccessException {
        var hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        var sql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getPublicConnection();
            var statement = conn.prepareStatement(sql)){
            statement.setString(1, user.username());
            statement.setString(2, hashedPassword);
            statement.setString(3, user.email());
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert user", e);
        };
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var sql = "SELECT username, password_hash, email FROM users WHERE username = ?";
        try (var conn = DatabaseManager.getPublicConnection();
            var statement = conn.prepareStatement(sql)) {
            statement.setString(1, username);
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new UserData(
                    resultSet.getString("username"),
                    resultSet.getString("password_hash"),
                    resultSet.getString("email")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get user", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        var sql = "DELETE FROM users";
        try (var conn = DatabaseManager.getPublicConnection();
            var statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear users", e);
        }
    }
}

