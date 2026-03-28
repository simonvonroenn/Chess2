package main.engine;

import main.chessboard.*;
import main.engine.Engine.BestMove;

import java.util.Collections;
import java.util.List;

public class DepthFirstSearchStrategy {
    private DepthFirstSearchStrategy() {}

    // Max depth for DFS
    public static final int MAX_DEPTH = 4;
    public static int _debug_positionsAnalyzed;

    /**
     * Performs iterative deepening search up to a maximum depth within the given time limit.
     *
     * @param board the current board state
     * @return the best move found so far
     */
    public static BestMove iterativeDeepeningSearch(BoardEnv board) {
        _debug_positionsAnalyzed = 0;
        long startTime = System.currentTimeMillis();
        BestMove bestMove = null;
        for (int depth = 4; depth <= MAX_DEPTH; depth++) {
            bestMove = alphaBetaSearch(board, depth, startTime);
            /*
            if (System.currentTimeMillis() - startTime >= Engine.TIME_LIMIT) {
                System.out.println("Reached depth: " + depth);
                break;
            }
             */
        }
        System.out.printf("Calculated for %d milliseconds.\n", System.currentTimeMillis() - startTime);
        System.out.printf("Bestmoves: %s\n", bestMove.moveSequence);
        System.out.printf("%d positions analyzed\n", _debug_positionsAnalyzed);
        return bestMove;
    }

    /**
     * Entry point for alpha-beta search with quiescence extension.
     * Delegates to the full implementation with the default quiescence depth.
     *
     * @param board     the current board state
     * @param depth     the remaining search depth
     * @param startTime the start time of the search in milliseconds
     * @return the best move found at this node
     */
    private static BestMove alphaBetaSearch(BoardEnv board, int depth, long startTime) {
        return alphaBetaSearch(board, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, startTime, 4);
    }

    /**
     * Performs a depth-limited alpha-beta search with quiescence extension.
     *
     * <p>Uses a negamax-like approach where white maximizes and black minimizes the evaluation.
     * At depth 0, the static board evaluation is returned. At depth 1, captures and checks
     * are extended by up to {@code qDepth} additional plies to avoid the horizon effect.
     *
     * <p>Alpha-beta pruning cuts off branches that cannot affect the final result:
     * <ul>
     *   <li>Alpha cutoff (fail-high): a move is so good for white that black would have
     *       avoided this line earlier — remaining moves are skipped.</li>
     *   <li>Beta cutoff (fail-low): a move is so good for black that white would have
     *       avoided this line earlier — remaining moves are skipped.</li>
     * </ul>
     *
     * <p>Terminal nodes (checkmate or stalemate) are detected when no legal moves exist
     * and scored accordingly: checkmate is {@link Integer#MIN_VALUE} or {@link Integer#MAX_VALUE},
     * stalemate is 0.
     *
     * @param board     the current board state, modified in-place and restored via unmakeMove
     * @param depth     the remaining search depth; 0 triggers static evaluation
     * @param alpha     the best score the maximizing player (white) can guarantee so far
     * @param beta      the best score the minimizing player (black) can guarantee so far
     * @param startTime the start time of the search in milliseconds
     * @param qDepth    the remaining quiescence depth; limits capture/check extensions
     *                  to prevent infinite recursion in tactical sequences
     * @return the best move found at this node, or a terminal evaluation if no moves exist
     */
    private static BestMove alphaBetaSearch(BoardEnv board, int depth, int alpha, int beta, long startTime, int qDepth) {
        // Terminate search if time limit reached
        //TODO: engine makes weird moves with time limit
        /*
        if (System.currentTimeMillis() - startTime >= Engine.TIME_LIMIT) {
            return new BestMove(null, evalInfo[0], Collections.emptyList());
        }
        */

        // At depth 0, return board evaluation.
        if (depth == 0) {
            _debug_positionsAnalyzed++;
            return new BestMove(null, board.evaluation, Collections.emptyList());
        }

        List<Move> moves = Engine.generateAllLegalMoves(board);
        Engine.orderMoves(board, moves);

        // Terminal node — no legal moves means checkmate or stalemate
        if (moves.isEmpty()) {
            _debug_positionsAnalyzed++;
            if (LegalMoveGenerator.isKingInCheck(board, board.whiteToMove)) {
                return new BestMove(null, board.whiteToMove ? Integer.MIN_VALUE : Integer.MAX_VALUE, Collections.emptyList());
            } else {
                return new BestMove(null, 0, Collections.emptyList()); // Stalemate
            }
        }

        BestMove bestMoveResponse = null;
        int originalEvaluation = board.evaluation;
        int originalPieceValueSum = board.pieceValueSum;

        for (Move move : moves) {
            int[] evalInfo = Engine.evaluateMove(board, move);
            board.evaluation = evalInfo[0];
            board.pieceValueSum = evalInfo[1];

            MakeMoveResult result = Chessboard.makeMove(board, move, false);
            BestMove response;

            if (!result.outcome.equals(GameOutcome.ONGOING)) {
                _debug_positionsAnalyzed++;
                if (result.outcome.equals(GameOutcome.CHECKMATE_WHITE)) {
                    response = new BestMove(null, Integer.MIN_VALUE, Collections.emptyList());
                } else if (result.outcome.equals(GameOutcome.CHECKMATE_BLACK)) {
                    response = new BestMove(null, Integer.MAX_VALUE, Collections.emptyList());
                } else {
                    response = new BestMove(null, 0, Collections.emptyList()); // Draw
                }
            } else if (depth == 1 && (move.isCapture || move.isCheck) && qDepth > 0) {
                // Quiescence extension — avoid horizon effect on tactical sequences
                response = alphaBetaSearch(board, depth, alpha, beta, startTime, qDepth - 1);
            } else {
                response = alphaBetaSearch(board, depth - 1, alpha, beta, startTime, qDepth);
            }

            Chessboard.unmakeMove(board, move, result.undoInfo);
            board.evaluation = originalEvaluation;
            board.pieceValueSum = originalPieceValueSum;

            if (board.whiteToMove) {
                if (bestMoveResponse == null || response.evaluation > bestMoveResponse.evaluation) {
                    bestMoveResponse = new BestMove(move, response.evaluation, response.moveSequence);
                }
                alpha = Math.max(alpha, bestMoveResponse.evaluation);
            } else {
                if (bestMoveResponse == null || response.evaluation < bestMoveResponse.evaluation) {
                    bestMoveResponse = new BestMove(move, response.evaluation, response.moveSequence);
                }
                beta = Math.min(beta, bestMoveResponse.evaluation);
            }

            if (alpha >= beta) break; // Alpha-beta cutoff

            if (depth == MAX_DEPTH) {
                System.out.printf("%s %d %s\n", move, response.evaluation, response.moveSequence);
            }
        }

        return bestMoveResponse;
    }
}
