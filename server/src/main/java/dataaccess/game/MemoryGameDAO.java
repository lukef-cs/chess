package dataaccess.game;

import dataaccess.DataAccessException;
import model.GameData;
import java.util.*;

public class MemoryGameDAO implements GameDAO {
    private final List<GameData> games = new ArrayList<>();
    private int nextID = 1;

    @Override
    public void clear() throws DataAccessException {
        games.clear();
        nextID = 1;
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        int gameID = nextID++;
        GameData newGame = new GameData(gameID, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        games.add(newGame);
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        for (GameData game : games) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        return null;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(games);
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        for (int i = 0; i < games.size(); i++) {
            if (games.get(i).gameID() == game.gameID()) {
                games.set(i, game);
                return;
            }
        }
    }
}
