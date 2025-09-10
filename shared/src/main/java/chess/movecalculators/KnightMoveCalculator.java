package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class KnightMoveCalculator {

    private final int[][] KNIGHT_OFFSETS = {
            {-2,1},{-2,-1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}
    };

    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        int startRow = position.getRow();
        int startCol = position.getColumn();
        List<ChessMove> moves = new ArrayList<>();

        for (int[] offset : KNIGHT_OFFSETS) {
            int newRow = startRow + offset[0];
            int newCol = startCol + offset[1];

            // Check if the new position is within bounds
            if (newRow > 0 && newRow <= 8 && newCol > 0 && newCol <= 8) {
            ChessPosition newPosition = new ChessPosition(newRow, newCol);

            // Check if there's a piece at the destination
            if (board.getPiece(newPosition) == null) {
                // Empty square, can move there
                moves.add(new ChessMove(position, newPosition, null));
            } else if (board.getPiece(newPosition).getTeamColor() != board.getPiece(position).getTeamColor()) {
                // Opponent's piece, can capture
                moves.add(new ChessMove(position, newPosition, null));
            }
            // If it's our own piece, we can't move there (no move added)
            }
        }
        return moves;
    }
}