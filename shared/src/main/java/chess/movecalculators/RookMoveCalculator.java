package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RookMoveCalculator extends MoveCalculator {


    private static final int[][] ROOK_DIRECTIONS = {
            {-1,0},{1,0},{0,1},{0,-1}
    };

    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        return calculateMovesFromDirections(board, position, ROOK_DIRECTIONS);
    }
}