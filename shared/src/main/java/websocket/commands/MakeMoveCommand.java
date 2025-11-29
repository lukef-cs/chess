package websocket.commands;
import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {
    private ChessMove move;

    public MakeMoveCommand(ChessMove move, String authToken, Integer gameId) {
        super(CommandType.MAKE_MOVE, authToken, gameId);
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }
}
