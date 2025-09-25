package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Collection;
import java.util.List;


public class MoveCalculatorHandler {
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);

        switch (piece.getPieceType()) {
            case PAWN:
                PawnMoveCalculator pawnCalculator = new PawnMoveCalculator();
                return pawnCalculator.calculateMoves(board, myPosition);
            case ROOK:
                RookMoveCalculator rookCalculator = new RookMoveCalculator();
                return rookCalculator.calculateMoves(board, myPosition);
            case KNIGHT:
                // this one's static
                return KnightMoveCalculator.calculateMoves(board, myPosition);
            case BISHOP:
                BishopMoveCalculator bishopCalculator = new BishopMoveCalculator();
                return bishopCalculator.calculateMoves(board, myPosition);
            case QUEEN:
                QueenMoveCalculator queenCalculator = new QueenMoveCalculator();
                return queenCalculator.calculateMoves(board, myPosition);
            case KING:
                KingMoveCalculator kingCalculator = new KingMoveCalculator();
                return kingCalculator.calculateMoves(board, myPosition);
            default:
                return List.of();
        }
    }
}
