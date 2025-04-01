package engine;

import chessboard.LegalMoveGenerator;
import chessboard.Move;
import engine.Engine.BestMove;

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
        long startTime = System.currentTimeMillis();
        BestMove bestMove = null;
        int[] evalInfo = Engine.evaluatePosition(board);
        for (int depth = 4; depth <= MAX_DEPTH; depth++) {
            bestMove = depthLimitedDFS(board, evalInfo, whiteToMove, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, startTime);
            /*
            if (System.currentTimeMillis() - startTime >= Engine.TIME_LIMIT) {
                System.out.println("Reached depth: " + depth);
                break;
            }
             */
        }
        System.out.printf("Calculated for %d milliseconds.\n", System.currentTimeMillis() - startTime);
        System.out.printf("Bestmoves: %s\n", bestMove.moveSequence);
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
    private static BestMove depthLimitedDFS(char[][] board, int[] evalInfo, boolean whiteToMove, int depth, int alpha, int beta, long startTime) {
        // Terminate search if time limit reached
        //TODO: engine makes weird moves with time limit
        /*
        if (System.currentTimeMillis() - startTime >= Engine.TIME_LIMIT) {
            return new BestMove(null, evalInfo[0], Collections.emptyList());
        }
         */
        // At depth 0, return board evaluation.
        if (depth == 0) {
            return new BestMove(null, evalInfo[0], Collections.emptyList());
        }
        // Generate legal moves for current player
        List<Move> moves = Engine.generateAllLegalMoves(board, whiteToMove);
        if (moves.isEmpty()) {
            // Terminal state: if checkmate, return loss, else draw.
            if (LegalMoveGenerator.isCheckmate(board, whiteToMove)) {
                return new BestMove(null, whiteToMove ? Integer.MIN_VALUE : Integer.MAX_VALUE, Collections.emptyList());
            }
            return new BestMove(null, 0, Collections.emptyList());
        }
        //TODO: improve speed
        // Engine.orderMoves(board, moves);
        BestMove bestMoveResponse = null;
        for (Move move : moves) {
            int[] newEvalInfo = Engine.evaluateMove(board, evalInfo, move);
            char pieceCaptured = LegalMoveGenerator.applyMove(board, move, whiteToMove);
            BestMove response;
            if (depth == 1 && (move.isCapture || move.isCheck)) {
                response = depthLimitedDFS(board, newEvalInfo, !whiteToMove, depth, alpha, beta, startTime);
            } else {
                response = depthLimitedDFS(board, newEvalInfo, !whiteToMove, depth - 1, alpha, beta, startTime);
            }
            LegalMoveGenerator.undoMove(board, move, whiteToMove, pieceCaptured);
            if (whiteToMove) {
                if (bestMoveResponse == null ||  response.evaluation > bestMoveResponse.evaluation) {
                    bestMoveResponse = new BestMove(move, response.evaluation, response.moveSequence);
                }
                alpha = Math.max(alpha, bestMoveResponse.evaluation);
            } else {
                if (bestMoveResponse == null || response.evaluation < bestMoveResponse.evaluation) {
                    bestMoveResponse = new BestMove(move, response.evaluation, response.moveSequence);
                }
                beta = Math.min(beta, bestMoveResponse.evaluation);
            }
            if (alpha >= beta) {
                break;
            }
            if (depth == MAX_DEPTH) {
                System.out.printf("%s %d %s\n", move, response.evaluation, response.moveSequence);
            }
        }
        return bestMoveResponse;
    }
}
