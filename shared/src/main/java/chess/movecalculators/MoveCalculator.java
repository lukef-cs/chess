package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MoveCalculator {
    public static Collection<ChessMove> calculateMovesFromOffset(ChessBoard board, ChessPosition position, int[][] PIECE_OFFSETS) {
        int startRow = position.getRow();
        int startCol = position.getColumn();
        List<ChessMove> moves = new ArrayList<>();

        for (int[] offset : PIECE_OFFSETS) {
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

    public static Collection<ChessMove> calculateMovesFromDirections(ChessBoard board, ChessPosition position, int[][] PIECE_DIRECTIONS) {
        int startRow = position.getRow();
        int startCol = position.getColumn();
        List<ChessMove> moves = new ArrayList<>();
        for (int[] direction : PIECE_DIRECTIONS) {
            int dRow = direction[0];
            int dCol = direction[1];
            int currentRow = startRow + dRow;
            int currentCol = startCol + dCol;

            while (currentRow > 0 && currentRow <= 8 && currentCol > 0 && currentCol <= 8) {
                ChessPosition newPosition = new ChessPosition(currentRow, currentCol);
                // detect if we've hit a piece
                if (board.getPiece(newPosition) != null) {
                    // If it's an opponent's piece, we can capture it
                    if (board.getPiece(newPosition).getTeamColor() != board.getPiece(position).getTeamColor()) {
                        moves.add(new ChessMove(position, newPosition, null));
                    }
                    break; // Stop moving in this direction
                }

                moves.add(new ChessMove(position, newPosition, null));
                currentRow += dRow;
                currentCol += dCol;
            }
        }
        return moves;
    }
}
