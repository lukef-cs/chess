package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.teamTurn = TeamColor.WHITE;
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "board=" + board +
                ", teamTurn=" + teamTurn +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && teamTurn == chessGame.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn);
    }

    private boolean isInCheck(ChessBoard board, TeamColor team) {
        ChessPosition kingPosition = findKing(board, team);
        if (kingPosition == null) {
            return false;
        }

        TeamColor opponentColor = team == TeamColor.BLACK ? TeamColor.WHITE : TeamColor.BLACK;
        return canOpponentAttackPosition(board, opponentColor, kingPosition);
    }

    private ChessPosition findKing(ChessBoard board, TeamColor team) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == team &&
                    piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return position;
                }
            }
        }
        return null;
    }

    private boolean canOpponentAttackPosition(ChessBoard board, TeamColor opponentColor, ChessPosition targetPosition) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece == null || piece.getTeamColor() != opponentColor) {
                    continue;
                }
                Collection<ChessMove> moves = piece.pieceMoves(board, position);
                for (ChessMove move : moves) {
                    if (move.getEndPosition().equals(targetPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if(piece == null){
            return null;
        }
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        List<ChessMove> valid = new ArrayList<>();

        for(ChessMove move : possibleMoves) {
            ChessBoard tempBoard = new ChessBoard(board);
            ChessPiece movingPiece = tempBoard.getPiece(move.getStartPosition());

            tempBoard.addPiece(move.getEndPosition(), movingPiece);
            tempBoard.addPiece(move.getStartPosition(), null);

            if(move.getPromotionPiece() != null){
                ChessPiece promoted = new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece());
                tempBoard.addPiece(move.getEndPosition(), promoted);
            }
            if(!isInCheck(tempBoard,movingPiece.getTeamColor())){
                valid.add(move);
            }
        }
        return valid;
    };

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> moves = validMoves(move.getStartPosition());
        if (moves == null || !moves.contains(move)){
            throw new InvalidMoveException("Invalid move");
        }
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if(getTeamTurn() != piece.getTeamColor()){
            throw new InvalidMoveException("Not your turn");
        }
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);

        if(move.getPromotionPiece() != null){
            ChessPiece promoted = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            board.addPiece(move.getEndPosition(), promoted);
        }

        teamTurn = teamTurn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(board, teamColor);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && validMovesForTeam(teamColor).isEmpty();
    }

    private Collection<ChessMove> validMovesForTeam(TeamColor team) {
        List<ChessMove> allValid = new ArrayList<>();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == team) {
                    Collection<ChessMove> moves = validMoves(position);
                    if (moves != null) {
                        allValid.addAll(moves);
                    }
                }
            }
        }
        return allValid;
    }



    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && validMovesForTeam(teamColor).isEmpty();
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
