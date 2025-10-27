package dataaccess.game;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.GameData;

public class SqlGameDAO implements GameDAO {

    private final Gson gson = new Gson();

    @Override
    public void clear() throws DataAccessException {
        var sql = "DELETE FROM games";
        try (var conn = DatabaseManager.getPublicConnection();
            var statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear games", e);
        }
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        var gameJson = gson.toJson(game.game());
        var sql = "INSERT INTO games (game_name, game_state) VALUES (?, ?)";
        try (var conn = DatabaseManager.getPublicConnection();
            var statement = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)){
            statement.setString(1, game.gameName());
            statement.setString(2, gameJson);
            statement.executeUpdate();

            var resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                throw new DataAccessException("Failed to retrieve generated game ID", null);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create game", e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var sql = "SELECT game_id, game_name, white_username, black_username, game_state FROM games WHERE game_id = ?";
        try (var conn = DatabaseManager.getPublicConnection();
            var statement = conn.prepareStatement(sql)) {
            statement.setInt(1, gameID);
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                var gameJson = resultSet.getString("game_state");
                ChessGame game = gson.fromJson(gameJson, ChessGame.class);
                return new GameData(
                    resultSet.getInt("game_id"),
                    resultSet.getString("white_username"),
                    resultSet.getString("black_username"),
                    resultSet.getString("game_name"),
                    game
                );
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get game", e);
        }

    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        var sql = "SELECT game_id, game_name, white_username, black_username, game_state FROM games";
        try (var conn = DatabaseManager.getPublicConnection();
            var statement = conn.prepareStatement(sql)) {
            var resultSet = statement.executeQuery();
            List<GameData> games = new ArrayList<>();
            while (resultSet.next()) {
                var gameJson = resultSet.getString("game_state");
                ChessGame game = gson.fromJson(gameJson, ChessGame.class);
                games.add(new GameData(
                    resultSet.getInt("game_id"),
                    resultSet.getString("white_username"),
                    resultSet.getString("black_username"),
                    resultSet.getString("game_name"),
                    game
                ));
            }
            return games;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list games", e);
        }

    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        var gameJson = gson.toJson(game.game());
        var sql = "UPDATE games SET white_username = ?, black_username = ?, game_name = ?, game_state = ? WHERE game_id = ?";
        try (var conn = DatabaseManager.getPublicConnection();
            var statement = conn.prepareStatement(sql)) {
            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, gameJson);
            statement.setInt(5, game.gameID());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update game", e);
        }
    }
}
