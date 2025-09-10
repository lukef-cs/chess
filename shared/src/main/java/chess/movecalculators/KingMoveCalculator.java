package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KingMoveCalculator {

    private final int[][] KING_OFFSETS = {
            {1,1},{-1,1},{1,-1},{-1,-1},{1,0},{-1,0},{0,1},{0,-1}
    };

    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        int startRow = position.getRow();
        int startCol = position.getColumn();
        List<ChessMove> moves = new ArrayList<>();

        for (int[] offset : KING_OFFSETS) {
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