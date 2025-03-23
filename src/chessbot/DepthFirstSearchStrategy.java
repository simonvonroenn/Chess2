package chessbot;

import chessboard.LegalMoveGenerator;
import chessboard.Move;
import chessbot.ChessBot.BestMove;

import java.util.List;

public class DepthFirstSearchStrategy {
    // Max depth for DFS
    public static final int MAX_DEPTH = 2;

    /**
     * Performs iterative deepening search up to a maximum depth within the given time limit.
     *
     * @param board the current board state
     * @param whiteToMove true if white is to move
     * @return the best move found so far
     */
    public static Move iterativeDeepeningSearch(char[][] board, boolean whiteToMove) {
        Move bestMove = null;
        long startTime = System.currentTimeMillis();
        for (int depth = 2; depth <= MAX_DEPTH; depth+=2) {
            BestMove bm = depthLimitedDFS(board, whiteToMove, depth, startTime);
            if (bm != null && bm.move != null) {
                bestMove = bm.move;
            }
            if (System.currentTimeMillis() - startTime >= ChessBot.TIME_LIMIT) {
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
    private static BestMove depthLimitedDFS(char[][] board, boolean whiteToMove, int depth, long startTime) {
        // Terminate search if time limit reached
        if (System.currentTimeMillis() - startTime >= ChessBot.TIME_LIMIT) {
            return new BestMove(null, ChessBot.evaluateBoard(board));
        }
        // Generate legal moves for current player
        List<Move> moves = ChessBot.generateAllLegalMoves(board, whiteToMove);
        if (moves.isEmpty()) {
            // Terminal state: if checkmate, return loss, else draw.
            if (LegalMoveGenerator.isCheckmate(board, whiteToMove)) {
                return new BestMove(null, -ChessBot.KING_VALUE);
            }
            return new BestMove(null, 0);
        }
        // At depth 0, return board evaluation.
        if (depth == 0) {
            return new BestMove(null, ChessBot.evaluateBoard(board));
        }

        BestMove bestMove = null;
        // Negamax: maximize the evaluation for current player.
        for (Move move : moves) {
            char[][] boardCopy = LegalMoveGenerator.copyBoard(board);
            ChessBot.applyMove(boardCopy, move);
            BestMove response = depthLimitedDFS(boardCopy, !whiteToMove, depth - 1, startTime);
            double moveValue = ChessBot.evaluateMove(board, boardCopy);
            double totalEval = moveValue + response.evaluation;
            if (bestMove == null || (whiteToMove && (totalEval > bestMove.evaluation)) || (!whiteToMove && (totalEval < bestMove.evaluation))) {
                bestMove = new BestMove(move, totalEval);
            }
        }
        return bestMove;
    }
}
