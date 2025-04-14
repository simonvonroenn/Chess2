package engine;

import chessboard.BoardEnv;
import chessboard.LegalMoveGenerator;
import chessboard.Move;
import engine.Engine.BestMove;

import java.util.Collections;
import java.util.List;

public class DepthFirstSearchStrategy {
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
            bestMove = depthLimitedDFS(board, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, startTime);
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
     * Performs a depth-limited DFS search to evaluate moves.
     * Uses a negamax-like approach where the move value is the material gain plus
     * the negative value of the opponent's best response.
     *
     * @param board the current board state
     * @param depth the current depth limit
     * @param startTime the start time of the search
     * @return a BestMove object containing the best move and its evaluation
     */
    private static BestMove depthLimitedDFS(BoardEnv board, int depth, int alpha, int beta, long startTime) {
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
        // Generate legal moves for current player
        List<Move> moves = Engine.generateAllLegalMoves(board);
        if (moves.isEmpty()) {
            _debug_positionsAnalyzed++;
            // Terminal state: if checkmate, return loss, else draw.
            if (LegalMoveGenerator.isCheckmate(board)) {
                return new BestMove(null, board.whiteToMove ? Integer.MIN_VALUE : Integer.MAX_VALUE, Collections.emptyList());
            }
            return new BestMove(null, 0, Collections.emptyList());
        }
        Engine.orderMoves(board, moves);
        BestMove bestMoveResponse = null;
        int originalEvaluation = board.evaluation;
        int originalPieceValueSum = board.pieceValueSum;
        for (Move move : moves) {
            int[] evalInfo = Engine.evaluateMove(board, move);
            board.evaluation = evalInfo[0];
            board.pieceValueSum = evalInfo[1];
            char pieceCaptured = LegalMoveGenerator.applyMove(board, move);
            board.whiteToMove = !board.whiteToMove;
            BestMove response;
            if (depth == 1 && (move.isCapture || move.isCheck)) {
                response = depthLimitedDFS(board, depth, alpha, beta, startTime);
            } else {
                response = depthLimitedDFS(board,depth - 1, alpha, beta, startTime);
            }
            board.whiteToMove = !board.whiteToMove;
            LegalMoveGenerator.undoMove(board, move, pieceCaptured);
            board.evaluation = originalEvaluation;
            board.pieceValueSum = originalPieceValueSum;
            if (board.whiteToMove) {
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
