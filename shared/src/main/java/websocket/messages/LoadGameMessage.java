package websocket.messages;

import model.GameData;
import websocket.messages.ServerMessage.ServerMessageType;

public class LoadGameMessage extends ServerMessage{
    private GameData game;

    public LoadGameMessage(GameData game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }

    public GameData getGame() {
        return game;
    }
}
