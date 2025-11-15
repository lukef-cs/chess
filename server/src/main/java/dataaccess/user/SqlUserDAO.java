package dataaccess.user;

import java.sql.SQLException;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.UserData;

public class SqlUserDAO implements UserDAO{


    @Override
    public void createUser(UserData user) throws DataAccessException {
        var sql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getPublicConnection();
            var statement = conn.prepareStatement(sql)){
            statement.setString(1, user.username());
            statement.setString(2, user.password());  // Already hashed in service layer
            statement.setString(3, user.email());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create user", e);
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

