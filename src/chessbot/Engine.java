package chessbot;

import chessboard.LegalMoveGenerator;
import chessboard.Move;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Engine {
    // Time limit in milliseconds
    public static final long TIME_LIMIT = 5000;

    // Piece values in pawn equivalents
    public static final int PAWN_VALUE = 100;
    public static final int KNIGHT_VALUE = 300;
    public static final int BISHOP_VALUE = 320;
    public static final int ROOK_VALUE = 500;
    public static final int QUEEN_VALUE = 900;

    /**
     * Calculates the best move for the current board state using iterative deepening within
     * the time limit.
     *
     * @param board the current chess board state
     * @param whiteToMove true if white is to move, false otherwise
     * @return the best move found, or null if no move is available
     */
    public BestMove calculateBestMove(char[][] board, boolean whiteToMove) {
        return DepthFirstSearchStrategy.iterativeDeepeningSearch(board, whiteToMove);
    }

    // Helper class to store the best move and its evaluation value.
    public static class BestMove {
        public Move move;
        public int evaluation;
        public List<String> moveSequence = new ArrayList<>();

        BestMove(Move move, int evaluation, List<String> previousMoveSequence) {
            this.move = move;
            if (move != null) {
                moveSequence.add(move.toString());
            }
            moveSequence.addAll(previousMoveSequence);
            this.evaluation = evaluation;
        }
    }

    /**
     * Evaluates a position based on material balance in centipawn equivalents.
     *
     * @param board the current board state
     * @return the evaluation value from white's perspective
     */
    protected static int evaluatePosition(char[][] board) {
        int evaluation = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board[row][col];
                int value = getPieceValue(piece);
                if (Character.isUpperCase(piece)) {
                    evaluation += value;
                } else {
                    evaluation -= value;
                }
            }
        }
        return evaluation;
    }

    private static int getPieceValue(char piece) {
        return switch (Character.toLowerCase(piece)) {
            case 'p' -> PAWN_VALUE;
            case 'n' -> KNIGHT_VALUE;
            case 'b' -> BISHOP_VALUE;
            case 'r' -> ROOK_VALUE;
            case 'q' -> QUEEN_VALUE;
            default -> 0;
        };
    }

    /**
     * Evaluates the move by comparing the board state before and after the move.
     * Only material gain (captures) is considered.
     *
     * @param boardBefore the board state before the move
     * @param boardAfter the board state after the move
     * @return the move's value in pawn equivalents
     */
    @Deprecated
    protected static double evaluateMove(char[][] boardBefore, char[][] boardAfter) {
        double valueBefore = evaluatePosition(boardBefore);
        double valueAfter = evaluatePosition(boardAfter);
        return valueAfter - valueBefore;
    }

    /**
     * Applies a move to the given board state.
     * Note: Special moves like castling, en passant, or promotion are not handled.
     *
     * @param board the board state to apply the move to
     * @param move the move to apply
     */
    @Deprecated
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
                    allMoves.addAll(LegalMoveGenerator.generateLegalMoves(board, row, col, whiteToMove));
                }
            }
        }
        return allMoves;
    }

    protected static void orderMoves(char[][] board, List<Move> moves) {
        Map<Move, Integer> evaluationCache = new HashMap<>();
        for (Move move : moves) {
            evaluationCache.put(move, guessMoveScore(board, move));
        }
        // sort in descending order
        moves.sort((m1, m2) -> Integer.compare(evaluationCache.get(m2), evaluationCache.get(m1)));
    }

    private static int guessMoveScore(char[][] board, Move move) {
        int score = 0;
        char pieceToMove = board[move.fromRow][move.fromCol];
        int movePieceVal = getPieceValue(pieceToMove);

        if (move.isCheck) {
            score += 10 * movePieceVal;
        }

        if (move.isCapture) {
            char pieceToCapture = board[move.toRow][move.toCol];
            int capturePieceVal = getPieceValue(pieceToCapture);
            score += 10 * capturePieceVal - movePieceVal;
        }

        return score;
    }
}