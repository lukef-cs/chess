package chess;

import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    private final int[][] KNIGHT_OFFSETS = {
            {-2,1},{-2,-1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}
    };
    private final int[][] KING_OFFSETS = {
            {1,1},{-1,1},{1,-1},{1,0},{-1,0},{0,1},{0,-1}
    };
    private final int[][] ROOK_DIRECTIONS = {
            {-1,0},{1,0},{0,1},{0,-1}
    };
    private final int[][] BISHOP_DIRECTIONS = {
            {1,1},{-1,1},{-1,-1},{1,-1}
    };
    private final int[][] QUEEN_DIRECTIONS = { // Bishop + Rook
            {-1,0},{1,0},{0,-1},{0,1},{-1,-1},{-1,1},{1,-1},{1,1}
    };

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        MoveCalculator calculator = new MoveCalculator();
        return calculator.calculateMoves(board, myPosition);
    }
}
