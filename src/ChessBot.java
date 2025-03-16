import java.util.ArrayList;
import java.util.List;

/**
 * The ChessBot class uses a depth-first search (DFS) approach to brute-force
 * search through capture moves within a given time limit (10 seconds) to determine
 * the best move based on material gain. Checkmate is considered infinitely valuable.
 */
public class ChessBot {
    // Time limit in milliseconds
    public static final long TIME_LIMIT = 2000;
    // Max depth for DFS
    public static final int MAX_DEPTH = 10;

    // Piece values in pawn equivalents
    private static final double PAWN_VALUE = 1.0;
    private static final double KNIGHT_VALUE = 3.0;
    private static final double BISHOP_VALUE = 3.0;
    private static final double ROOK_VALUE = 5.0;
    private static final double QUEEN_VALUE = 9.0;
    private static final double KING_VALUE = 1000.0;

    /**
     * Calculates the best move for the current board state using iterative deepening within
     * the time limit.
     *
     * @param board the current chess board state
     * @param whiteToMove true if white is to move, false otherwise
     * @return the best move found, or null if no move is available
     */
    public Move calculateBestMove(char[][] board, boolean whiteToMove) {
        return iterativeDeepeningSearch(board, whiteToMove);
    }

    // Helper class to store the best move and its evaluation value.
    private static class BestMove {
        Move move;
        double evaluation;

        BestMove(Move move, double evaluation) {
            this.move = move;
            this.evaluation = evaluation;
        }
    }

    /**
     * Performs iterative deepening search up to a maximum depth within the given time limit.
     *
     * @param board the current board state
     * @param whiteToMove true if white is to move
     * @return the best move found so far
     */
    private Move iterativeDeepeningSearch(char[][] board, boolean whiteToMove) {
        Move bestMove = null;
        long startTime = System.currentTimeMillis();
        for (int depth = 2; depth <= MAX_DEPTH; depth+=2) {
            BestMove bm = depthLimitedDFS(board, whiteToMove, depth, startTime);
            if (bm != null && bm.move != null) {
                bestMove = bm.move;
            }
            if (System.currentTimeMillis() - startTime >= TIME_LIMIT) {
                System.out.println("Reached depth: " + depth);
                break;
            }
        }
        return bestMove;
    }

    /**
     * Performs a depth-limited DFS search to evaluate moves.
     * Uses a negamax-like approach where the move value is the material gain plus
     * the negative value of the opponent's best response.
     *
     * @param board the current board state
     * @param whiteToMove true if it is white's turn
     * @param depth the current depth limit
     * @param startTime the start time of the search
     * @return a BestMove object containing the best move and its evaluation
     */
    private BestMove depthLimitedDFS(char[][] board, boolean whiteToMove, int depth, long startTime) {
        // Terminate search if time limit reached
        if (System.currentTimeMillis() - startTime >= TIME_LIMIT) {
            return new BestMove(null, evaluateBoard(board));
        }
        // Generate legal moves for current player
        List<Move> moves = generateAllLegalMoves(board, whiteToMove);
        if (moves.isEmpty()) {
            // Terminal state: if checkmate, return loss, else draw.
            if (LegalMoveGenerator.isCheckmate(board, whiteToMove)) {
                return new BestMove(null, -KING_VALUE);
            }
            return new BestMove(null, 0);
        }
        // At depth 0, return board evaluation.
        if (depth == 0) {
            return new BestMove(null, evaluateBoard(board));
        }

        BestMove bestMove = null;
        // Negamax: maximize the evaluation for current player.
        for (Move move : moves) {
            char[][] boardCopy = LegalMoveGenerator.copyBoard(board);
            applyMove(boardCopy, move);
            BestMove response = depthLimitedDFS(boardCopy, !whiteToMove, depth - 1, startTime);
            double moveValue = evaluateMove(board, boardCopy);
            double totalEval = moveValue + response.evaluation;
            if (bestMove == null || (whiteToMove && (totalEval > bestMove.evaluation)) || (!whiteToMove && (totalEval < bestMove.evaluation))) {
                bestMove = new BestMove(move, totalEval);
            }
        }
        return bestMove;
    }

    /**
     * Evaluates the board based on material balance in pawn equivalents.
     *
     * @param board the current board state
     * @return the evaluation value from white's perspective
     */
    private double evaluateBoard(char[][] board) {
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
    private double evaluateMove(char[][] boardBefore, char[][] boardAfter) {
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
    private void applyMove(char[][] board, Move move) {
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
    private List<Move> generateAllLegalMoves(char[][] board, boolean whiteToMove) {
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