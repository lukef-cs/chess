package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {

    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        // Include the -1 because a chessboard is 7x7, but we're going from 0-7 yk
        squares[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow()-1][position.getColumn()-1];
    }

    private static final ChessPiece.PieceType[] BACK_RANK = {
            ChessPiece.PieceType.ROOK,
            ChessPiece.PieceType.KNIGHT,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.QUEEN,
            ChessPiece.PieceType.KING,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.KNIGHT,
            ChessPiece.PieceType.ROOK
    };

    private void placeBackRank(int row, ChessGame.TeamColor color) {
        for (int col = 0; col < 8; col++) {
            squares[row][col] = new ChessPiece(color, BACK_RANK[col]);
        }
    }

    private void placePawns(int row, ChessGame.TeamColor color) {
            for (int col = 0; col < 8; col++) {
                squares[row][col] = new ChessPiece(color, ChessPiece.PieceType.PAWN);
            }
        }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        squares = new ChessPiece[8][8];

        // --- White at bottom (rows 6–7) ---
        placeBackRank(0, ChessGame.TeamColor.WHITE);
        placePawns(1, ChessGame.TeamColor.WHITE);

        // --- Black at top (rows 0–1) ---
        placeBackRank(7, ChessGame.TeamColor.BLACK);
        placePawns(6, ChessGame.TeamColor.BLACK);
    }
}
