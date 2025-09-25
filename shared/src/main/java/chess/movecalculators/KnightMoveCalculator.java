package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class KnightMoveCalculator extends MoveCalculator{

    private static final int[][] KNIGHT_OFFSETS = {
            {-2,1},{-2,-1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}
    };

    public static Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        return calculateMovesFromOffset(board, position, KNIGHT_OFFSETS);
    }
}