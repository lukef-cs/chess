package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KingMoveCalculator extends MoveCalculator{

    private final int[][] KING_OFFSETS = {
            {1,1},{-1,1},{1,-1},{-1,-1},{1,0},{-1,0},{0,1},{0,-1}
    };

    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        return calculateMovesFromOffset(board, position, KING_OFFSETS);
    }
}