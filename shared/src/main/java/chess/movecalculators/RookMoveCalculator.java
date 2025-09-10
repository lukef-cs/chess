package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.List;

public class RookMoveCalculator {


    private final int[][] ROOK_DIRECTIONS = {
            {-1,0},{1,0},{0,1},{0,-1}
    };

    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        // Placeholder for King move calculation logic
        return List.of();
    }
}