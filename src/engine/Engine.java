package engine;

import chessboard.Chessboard;
import chessboard.LegalMoveGenerator;
import chessboard.Move;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Engine {
    // Time limit in milliseconds
    public static final long TIME_LIMIT = 5000;
    private static long _debugTime_GenerateAllLegalMoves = 0;
    private static long _debugTime_EvaluatePosition = 0;

    /**
     * Calculates the best move for the current board state using iterative deepening within
     * the time limit.
     *
     * @param board the current chess board state
     * @param whiteToMove true if white is to move, false otherwise
     * @return the best move found, or null if no move is available
     */
    public BestMove calculateBestMove(char[][] board, boolean whiteToMove) {
        BestMove bestMove = DepthFirstSearchStrategy.iterativeDeepeningSearch(board, whiteToMove);
        System.out.printf("Calculation parts: generate all legal moves: %dms, evaluate Position: %dms\n",
                _debugTime_GenerateAllLegalMoves, _debugTime_EvaluatePosition);
        _debugTime_GenerateAllLegalMoves = 0;
        _debugTime_EvaluatePosition = 0;
        return bestMove;
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
     * Applies on a given board evaluation the eval delta given a certain move.
     *
     * @param board the board state
     * @param evalInfo the evaluation of the board state before the move and the current sum of all piece values
     * @param move the move
     * @return the evaluation of the board state after the move and the new pieceValueSum
     */
    public static int[] evaluateMove(char[][] board, int[] evalInfo, Move move) {
        long startTime = System.currentTimeMillis();
        int capturedPieceValue = 0;
        int newPieceValueSum = evalInfo[1];
        if (move.isCapture) {
            char capturedPiece = board[move.toRow][move.toCol];
            capturedPieceValue = PieceValues.getPieceValue(capturedPiece) + PieceValues.getPieceTableValue(capturedPiece, move.toRow, move.toCol, evalInfo[1] );
            newPieceValueSum -= Math.abs(PieceValues.getPieceValue(capturedPiece));
        }
        char movedPiece = board[move.fromRow][move.fromCol];
        int movePieceTableValueBefore = PieceValues.getPieceTableValue(movedPiece, move.fromRow, move.fromCol, evalInfo[1]);
        int movePieceTableValueAfter = PieceValues.getPieceTableValue(movedPiece, move.toRow, move.toCol, newPieceValueSum);
        int evalDelta = -capturedPieceValue + movePieceTableValueAfter - movePieceTableValueBefore;
        _debugTime_EvaluatePosition += System.currentTimeMillis() - startTime;
        return new int[]{evalInfo[0] + evalDelta, newPieceValueSum};
    }

    /**
     * Evaluates a position based on piece value and piece position value.
     *
     * @param board the current board state
     * @return the evaluation and the sum of all piece values
     */
    public static int[] evaluatePosition(char[][] board) {
        int evaluation = 0;
        int pieceValueSum = 0;
        int pieceTotalValue;
        int[] whiteKingPos = new int[0];
        int[] blackKingPos = new int[0];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board[row][col];
                int pieceValue = PieceValues.getPieceValue(piece);
                int pieceTableValue = Character.toLowerCase(piece) != 'k' ? PieceValues.getPieceTableValue(piece, row, col, 0) : 0;
                if (piece == 'K') {
                    whiteKingPos = new int[]{row, col};
                } else if (piece == 'k') {
                    blackKingPos = new int[]{row, col};
                } else {
                    pieceValueSum += Math.abs(pieceValue);
                    pieceTotalValue = pieceValue + pieceTableValue;
                    evaluation += pieceTotalValue;
                    Chessboard._debug_pieceValues[row][col] = Math.abs(pieceTotalValue);
                }
            }
        }
        pieceTotalValue = PieceValues.KING + PieceValues.getPieceTableValue('K', whiteKingPos[0], whiteKingPos[1], pieceValueSum);
        evaluation += pieceTotalValue;
        Chessboard._debug_pieceValues[whiteKingPos[0]][whiteKingPos[1]] = Math.abs(pieceTotalValue);
        pieceTotalValue = -PieceValues.KING + PieceValues.getPieceTableValue('k', blackKingPos[0], blackKingPos[1], pieceValueSum);
        evaluation += pieceTotalValue;
        Chessboard._debug_pieceValues[blackKingPos[0]][blackKingPos[1]] = Math.abs(pieceTotalValue);
        return new int[]{evaluation, pieceValueSum};
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
        long startTime = System.currentTimeMillis();
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
        _debugTime_GenerateAllLegalMoves += System.currentTimeMillis() - startTime;
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
        int movePieceVal = PieceValues.getPieceValue(pieceToMove);

        if (move.isCheck) {
            score += 10 * movePieceVal;
        }

        if (move.isCapture) {
            char pieceToCapture = board[move.toRow][move.toCol];
            int capturePieceVal = PieceValues.getPieceValue(pieceToCapture);
            score += 10 * capturePieceVal - movePieceVal;
        }

        return score;
    }
}