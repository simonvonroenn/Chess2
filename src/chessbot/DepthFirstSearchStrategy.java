package chessbot;

import chessboard.LegalMoveGenerator;
import chessboard.Move;
import chessbot.Engine.BestMove;

import java.util.Collections;
import java.util.List;

public class DepthFirstSearchStrategy {
    // Max depth for DFS
    public static final int MAX_DEPTH = 4;

    /**
     * Performs iterative deepening search up to a maximum depth within the given time limit.
     *
     * @param board the current board state
     * @param whiteToMove true if white is to move
     * @return the best move found so far
     */
    public static BestMove iterativeDeepeningSearch(char[][] board, boolean whiteToMove) {
        BestMove bestMove = null;
        long startTime = System.currentTimeMillis();
        for (int depth = 4; depth <= MAX_DEPTH; depth+=2) {
            bestMove = depthLimitedDFS(board, whiteToMove, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, startTime);
            if (System.currentTimeMillis() - startTime >= Engine.TIME_LIMIT) {
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
    private static BestMove depthLimitedDFS(char[][] board, boolean whiteToMove, int depth, int alpha, int beta, long startTime) {
        // Terminate search if time limit reached
        /*
        if (System.currentTimeMillis() - startTime >= Engine.TIME_LIMIT) {
            return new BestMove(null, Engine.evaluateBoard(board));
        }
        */
        // Generate legal moves for current player
        List<Move> moves = Engine.generateAllLegalMoves(board, whiteToMove);
        if (moves.isEmpty()) {
            // Terminal state: if checkmate, return loss, else draw.
            if (LegalMoveGenerator.isCheckmate(board, whiteToMove)) {
                return new BestMove(null, whiteToMove ? Integer.MIN_VALUE : Integer.MAX_VALUE, Collections.emptyList());
            }
            return new BestMove(null, 0, Collections.emptyList());
        }
        // At depth 0, return board evaluation.
        if (depth == 0) {
            return new BestMove(null, Engine.evaluatePosition(board), Collections.emptyList());
        }

        BestMove bestMove = null;
        // Negamax: maximize the evaluation for current player.
        for (Move move : moves) {
            char[][] boardCopy = LegalMoveGenerator.applyMove(board, move, whiteToMove);
            BestMove response;
            if (move.isCapture || move.isCheck) {
                response = depthLimitedDFS(boardCopy, !whiteToMove, depth, alpha, beta, startTime);
            } else {
                response = depthLimitedDFS(boardCopy, !whiteToMove, depth - 1, alpha, beta, startTime);
            }
            int eval = response.evaluation;
            //double moveValue = Engine.evaluateMove(board, boardCopy);
            //double totalEval = moveValue + response.evaluation;
            if (whiteToMove) {
                if (bestMove == null ||  eval > bestMove.evaluation) {
                    bestMove = new BestMove(move, eval, response.moveSequence);
                }
                alpha = Math.max(alpha, bestMove.evaluation);
            } else {
                if (bestMove == null || eval < bestMove.evaluation) {
                    bestMove = new BestMove(move, eval, response.moveSequence);
                }
                beta = Math.min(beta, bestMove.evaluation);
            }
            if (alpha >= beta) {
                break;
            }
            if (depth == MAX_DEPTH) {
                System.out.printf("%s %d %s\n", move, eval, response.moveSequence);
            }
        }
        return bestMove;
    }
}
