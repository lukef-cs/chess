package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.List;
import java.util.Collection;

public class BishopMoveCalculator {

    private final int[][] BISHOP_DIRECTIONS = {
        {1,1},{-1,1},{-1,-1},{1,-1}
    };


    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        int startRow = position.getRow();
        int startCol = position.getColumn();
        List<ChessMove> moves = new java.util.ArrayList<>();
        for (int[] direction : BISHOP_DIRECTIONS) {
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