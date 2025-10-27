package dataaccess.auth;

import java.sql.SQLException;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.AuthData;
import model.UserData;

public class SqlAuthDAO implements AuthDAO {

    @Override
    public void clear() throws DataAccessException {
        var sql = "DELETE FROM auth";
        try (var conn = DatabaseManager.getPublicConnection();
            var statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear auth", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var sql = "SELECT auth_token, username FROM auth WHERE auth_token = ?";
        try (var conn = DatabaseManager.getPublicConnection();
            var statement = conn.prepareStatement(sql)) {
            statement.setString(1, authToken);
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new AuthData(
                    resultSet.getString("auth_token"),
                    resultSet.getString("username")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get user", e);
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        var sql = "INSERT INTO auth (auth_token, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getPublicConnection();
            var statement = conn.prepareStatement(sql)){
            statement.setString(1, auth.authToken());
            statement.setString(2, auth.username());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create auth", e);
        };
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var sql = "DELETE FROM auth WHERE auth_token = ?";
        try (var conn = DatabaseManager.getPublicConnection();
            var statement = conn.prepareStatement(sql)) {
            statement.setString(1, authToken);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete auth", e);
        }
    }

}
