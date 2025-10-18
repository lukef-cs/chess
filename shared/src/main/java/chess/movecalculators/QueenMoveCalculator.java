package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueenMoveCalculator extends MoveCalculator{

    private static final int[][] QUEEN_DIRECTIONS = { // Bishop + Rook
        {-1,0},{1,0},{0,-1},{0,1},{-1,-1},{-1,1},{1,-1},{1,1}
    };


    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        return calculateMovesFromDirections(board, position, QUEEN_DIRECTIONS);
    }
}