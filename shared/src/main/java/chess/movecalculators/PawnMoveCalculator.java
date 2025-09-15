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

    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        int startRow = position.getRow();
        int startCol = position.getColumn();
        List<ChessMove> moves = new ArrayList<>();
        TeamColor ourColor = board.getPiece(position).getTeamColor();

        for(int[] offset : getPawnOffsets(ourColor, position)){
            int newRow = startRow + offset[0];
            int newCol = startCol + offset[1];

            if(newRow > 0 && newRow <= 8 && newCol > 0 && newCol <=8) {
                boolean shouldPromote = (newRow == 1 && ourColor == TeamColor.BLACK) || (newRow == 8 && ourColor == TeamColor.WHITE);

                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece theirPiece = board.getPiece(newPosition);
                if(board.getPiece(newPosition) != null && newCol == startCol){
                    // Cant go forwards into someone
                    continue;
                }
                if(board.getPiece(newPosition) != null){
                    TeamColor pieceColor = board.getPiece(newPosition).getTeamColor();
                    if (ourColor != pieceColor){
                        // we can take it
                        if (shouldPromote) {

                            addPromotionMove(position, newPosition, moves);
                        } else {
                            moves.add(new ChessMove(position, newPosition, null));
                        }
                    }
                } else if (startCol == newCol) {
                    if (shouldPromote) {
                        addPromotionMove(position, newPosition, moves);
                    } else {
                        moves.add(new ChessMove(position, newPosition, null));
                    }

                    // Check if first move
                    if(ourColor == TeamColor.WHITE && position.getRow() == 2){
                        if(board.getPiece(newPosition) != null || board.getPiece(new ChessPosition(newRow +1, newCol)) != null){
                            // There's someone in the way
                            continue;
                        }
                        // Moving up
                        moves.add(new ChessMove(position, new ChessPosition(newRow+1, newCol), null));
                    } else if (ourColor == TeamColor.BLACK && position.getRow() == 7){
                        if(board.getPiece(newPosition) != null || board.getPiece(new ChessPosition(newRow -1, newCol)) != null){
                            // There's someone in the way
                            continue;
                        }
                        // Moving down
                        moves.add(new ChessMove(position, new ChessPosition(newRow-1, newCol), null));
                    }
                }
            }
        }
        return moves;
    }
}