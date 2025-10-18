package chess.movecalculators;
import chess.ChessGame.TeamColor;
import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class PawnMoveCalculator {

    private int [][] getPawnOffsets(TeamColor color, ChessPosition position) {
        if(color == TeamColor.BLACK){
            // We're moving down
            if(position.getRow() == 7){
                // First move
                return new int[][]{{-1,0},{-1,-1},{-1,1}};
            }
            return new int[][]{{-1,0},{-1,-1},{-1,1}};
        } else {
            // we're moving up
            if(position.getRow() == 2){
                return new int[][]{{1, 0}, {1, -1}, {1, 1}};
            }
            return new int[][]{{1, 0}, {1, -1}, {1, 1}};
        }
    }

    private Collection<ChessMove> addPromotionMove(ChessPosition position, ChessPosition newPosition, Collection<ChessMove> moves) {
        moves.add(new ChessMove(position, newPosition, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(position, newPosition, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(position, newPosition, ChessPiece.PieceType.BISHOP));
        moves.add(new ChessMove(position, newPosition, ChessPiece.PieceType.KNIGHT));
        return moves;
    }

    private void handlePawnMove(ChessBoard board, ChessPosition position, int newRow, int newCol,
                                int startCol, boolean shouldPromote, List<ChessMove> moves) {
        ChessPosition newPosition = new ChessPosition(newRow, newCol);
        TeamColor ourColor = board.getPiece(position).getTeamColor();

        if (board.getPiece(newPosition) != null && newCol == startCol) {
            // Cant go forwards into someone
            return;
        }

        if (board.getPiece(newPosition) != null) {
            handleCapture(board, position, newPosition, ourColor, shouldPromote, moves);
        } else if (startCol == newCol) {
            handleForwardMove(board, position, newRow, newCol, startCol, shouldPromote, moves, ourColor);
        }
    }

    private void handleCapture(ChessBoard board, ChessPosition position, ChessPosition newPosition,
                               TeamColor ourColor, boolean shouldPromote, List<ChessMove> moves) {
        TeamColor pieceColor = board.getPiece(newPosition).getTeamColor();
        if (ourColor != pieceColor) {
            // we can take it
            if (shouldPromote) {
                addPromotionMove(position, newPosition, moves);
            } else {
                moves.add(new ChessMove(position, newPosition, null));
            }
        }
    }

    private void handleForwardMove(ChessBoard board, ChessPosition position, int newRow, int newCol,
                                   int startCol, boolean shouldPromote, List<ChessMove> moves, TeamColor ourColor) {
        if (shouldPromote) {
            addPromotionMove(position, new ChessPosition(newRow, newCol), moves);
        } else {
            moves.add(new ChessMove(position, new ChessPosition(newRow, newCol), null));
        }

        // Check if first move
        if (ourColor == TeamColor.WHITE && position.getRow() == 2) {
            handleWhitePawnFirstMove(board, position, newRow, newCol, moves);
        } else if (ourColor == TeamColor.BLACK && position.getRow() == 7) {
            handleBlackPawnFirstMove(board, position, newRow, newCol, moves);
        }
    }

    private void handleWhitePawnFirstMove(ChessBoard board, ChessPosition position, int newRow, int newCol, List<ChessMove> moves) {
        if (board.getPiece(new ChessPosition(newRow, newCol)) != null ||
            board.getPiece(new ChessPosition(newRow + 1, newCol)) != null) {
            // There's someone in the way
            return;
        }
        // Moving up
        moves.add(new ChessMove(position, new ChessPosition(newRow + 1, newCol), null));
    }

    private void handleBlackPawnFirstMove(ChessBoard board, ChessPosition position, int newRow, int newCol, List<ChessMove> moves) {
        if (board.getPiece(new ChessPosition(newRow, newCol)) != null ||
            board.getPiece(new ChessPosition(newRow - 1, newCol)) != null) {
            // There's someone in the way
            return;
        }
        // Moving down
        moves.add(new ChessMove(position, new ChessPosition(newRow - 1, newCol), null));
    }

    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        int startRow = position.getRow();
        int startCol = position.getColumn();
        List<ChessMove> moves = new ArrayList<>();
        TeamColor ourColor = board.getPiece(position).getTeamColor();

        for (int[] offset : getPawnOffsets(ourColor, position)) {
            int newRow = startRow + offset[0];
            int newCol = startCol + offset[1];

            if (newRow > 0 && newRow <= 8 && newCol > 0 && newCol <= 8) {
                boolean shouldPromote = (newRow == 1 && ourColor == TeamColor.BLACK) ||
                                       (newRow == 8 && ourColor == TeamColor.WHITE);
                handlePawnMove(board, position, newRow, newCol, startCol, shouldPromote, moves);
            }
        }
        return moves;
    }
}