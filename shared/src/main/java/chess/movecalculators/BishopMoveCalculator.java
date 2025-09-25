package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

public class BishopMoveCalculator extends MoveCalculator {

    private final int[][] BISHOP_DIRECTIONS = {
        {1,1},{-1,1},{-1,-1},{1,-1}
    };


    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        return calculateMovesFromDirections(board, position, BISHOP_DIRECTIONS);
    }
}