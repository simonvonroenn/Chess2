package chessbot;

import chessboard.LegalMoveGenerator;
import chessboard.Move;

import java.util.ArrayList;
import java.util.List;

/**
 * The ChessBot.ChessBot class uses a depth-first search (DFS) approach to brute-force
 * search through capture moves within a given time limit (10 seconds) to determine
 * the best move based on material gain. Checkmate is considered infinitely valuable.
 */
public class ChessBot {
    // Time limit in milliseconds
    public static final long TIME_LIMIT = 2000;

    // Piece values in pawn equivalents
    public static final double PAWN_VALUE = 1.0;
    public static final double KNIGHT_VALUE = 3.0;
    public static final double BISHOP_VALUE = 3.0;
    public static final double ROOK_VALUE = 5.0;
    public static final double QUEEN_VALUE = 9.0;
    public static final double KING_VALUE = 1000.0;

    /**
     * Calculates the best move for the current board state using iterative deepening within
     * the time limit.
     *
     * @param board the current chess board state
     * @param whiteToMove true if white is to move, false otherwise
     * @return the best move found, or null if no move is available
     */
    public Move calculateBestMove(char[][] board, boolean whiteToMove) {
        return DepthFirstSearchStrategy.iterativeDeepeningSearch(board, whiteToMove);
    }

    // Helper class to store the best move and its evaluation value.
    public static class BestMove {
        Move move;
        double evaluation;

        BestMove(Move move, double evaluation) {
            this.move = move;
            this.evaluation = evaluation;
        }
    }

    /**
     * Evaluates the board based on material balance in pawn equivalents.
     *
     * @param board the current board state
     * @return the evaluation value from white's perspective
     */
    protected static double evaluateBoard(char[][] board) {
        double evaluation = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board[row][col];
                double value = switch (Character.toLowerCase(piece)) {
                    case 'p' -> PAWN_VALUE;
                    case 'n' -> KNIGHT_VALUE;
                    case 'b' -> BISHOP_VALUE;
                    case 'r' -> ROOK_VALUE;
                    case 'q' -> QUEEN_VALUE;
                    case 'k' -> KING_VALUE;
                    default -> 0;
                };
                if (Character.isUpperCase(piece)) {
                    evaluation += value;
                } else {
                    evaluation -= value;
                }
            }
        }
        return evaluation;
    }

    /**
     * Evaluates the move by comparing the board state before and after the move.
     * Only material gain (captures) is considered.
     *
     * @param boardBefore the board state before the move
     * @param boardAfter the board state after the move
     * @return the move's value in pawn equivalents
     */
    protected static double evaluateMove(char[][] boardBefore, char[][] boardAfter) {
        double valueBefore = evaluateBoard(boardBefore);
        double valueAfter = evaluateBoard(boardAfter);
        return valueAfter - valueBefore;
    }

    /**
     * Applies a move to the given board state.
     * Note: Special moves like castling, en passant, or promotion are not handled.
     *
     * @param board the board state to apply the move to
     * @param move the move to apply
     */
    protected static void applyMove(char[][] board, Move move) {
        char movingPiece = board[move.fromRow][move.fromCol];
        board[move.toRow][move.toCol] = movingPiece;
        board[move.fromRow][move.fromCol] = '\0';
    }

    /**
     * Generates all legal moves for the current player from the given board state.
     *
     * @param board the current board state
     * @param whiteToMove true if white is to move
     * @return a list of legal moves
     */
    protected static List<Move> generateAllLegalMoves(char[][] board, boolean whiteToMove) {
        List<Move> allMoves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board[row][col];
                if (piece == '\0') continue;
                if ((whiteToMove && Character.isUpperCase(piece)) || (!whiteToMove && Character.isLowerCase(piece))) {
                    List<int[]> moves = LegalMoveGenerator.generateLegalMoves(board, row, col, whiteToMove);
                    for (int[] m : moves) {
                        allMoves.add(new Move(row, col, m[0], m[1]));
                    }
                }
            }
        }
        return allMoves;
    }
}