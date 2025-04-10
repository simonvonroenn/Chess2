package engine;

import chessboard.Chessboard;
import chessboard.LegalMoveGenerator;
import chessboard.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Engine {
    // Time limit in milliseconds
    protected static final long TIME_LIMIT = 5000;
    public static long _debugTime_ApplyMove = 0;
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
        _debugTime_ApplyMove = 0;
        char[][] boardCopy = LegalMoveGenerator.copyBoard(board);
        BestMove bestMove = DepthFirstSearchStrategy.iterativeDeepeningSearch(boardCopy, whiteToMove);
        System.out.printf("Calculation parts: generate all legal moves: %dms, " +
                        "evaluate Position: %dms," +
                        "applyMove: %dms\n",
                _debugTime_GenerateAllLegalMoves, _debugTime_EvaluatePosition, _debugTime_ApplyMove);
        _debugTime_GenerateAllLegalMoves = 0;
        _debugTime_EvaluatePosition = 0;
        _debugTime_ApplyMove = 0;
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
            char capturedPiece;
            // En passant
            if (Character.toLowerCase(move.piece) == 'p' && move.fromCol != move.toCol && board[move.toRow][move.toCol] == '\0') {
                capturedPiece = (move.piece == 'P' ? board[move.toRow + 1][move.toCol] : board[move.toRow - 1][move.toCol]);
            } else {
                capturedPiece = board[move.toRow][move.toCol];
            }
            capturedPieceValue = PieceValues.getPieceValue(capturedPiece) + PieceValues.getPieceTableValue(capturedPiece, move.toRow, move.toCol, evalInfo[1] );
            newPieceValueSum -= Math.abs(PieceValues.getPieceValue(capturedPiece));
        }

        int movePieceTableValueBefore = PieceValues.getPieceTableValue(move.piece, move.fromRow, move.fromCol, evalInfo[1]);
        int movePieceTableValueAfter = PieceValues.getPieceTableValue(move.piece, move.toRow, move.toCol, newPieceValueSum);

        // Castling
        int castlingRookTableValueDelta = 0;
        if (Character.toLowerCase(move.piece) == 'k' && move.toCol - move.fromCol == -2) { // better rook position when O-O-O
            castlingRookTableValueDelta = Character.isUpperCase(move.piece) ? PieceValues.OOO_ROOK_DELTA : -PieceValues.OOO_ROOK_DELTA;
        }

        // Promotion
        int promotionPieceValue = 0;
        int promotionPieceTableValue = 0;
        if (move.promotionPiece != '\0') {
            promotionPieceValue = PieceValues.getPieceValue(move.promotionPiece) - PieceValues.getPieceValue(move.piece);
            promotionPieceTableValue = PieceValues.getPieceTableValue(move.promotionPiece, move.toRow, move.toCol, evalInfo[1]);
            movePieceTableValueAfter = 0;
            newPieceValueSum += Math.abs(promotionPieceValue);
        }

        int evalDelta = -capturedPieceValue + movePieceTableValueAfter - movePieceTableValueBefore
                        + castlingRookTableValueDelta // if castling
                        + promotionPieceValue + promotionPieceTableValue; // if promotion

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

    protected static void orderMoves(char[][] board, List<Move> moves, int pieceValueSum) {
        Map<Move, Integer> evaluationCache = new HashMap<>();
        for (Move move : moves) {
            evaluationCache.put(move, guessMoveScore(board, move, pieceValueSum));
        }
        // sort in descending order
        moves.sort((m1, m2) -> Integer.compare(evaluationCache.get(m2), evaluationCache.get(m1)));
        //System.out.println("MoveOrder: " + moves);
    }

    private static int guessMoveScore(char[][] board, Move move, int pieceValueSum) {
        char pieceToMove = board[move.fromRow][move.fromCol];
        int score = PieceValues.getPieceTableValue(pieceToMove, move.toRow, move.toCol, pieceValueSum)
                    - PieceValues.getPieceTableValue(pieceToMove, move.fromRow, move.fromCol, pieceValueSum);
        int movePieceAbsVal = Math.abs(PieceValues.getPieceValue(pieceToMove));

        if (move.isCheck) {
            score += 2 * movePieceAbsVal;
        }

        if (move.isCapture) {
            char pieceToCapture = board[move.toRow][move.toCol];
            int capturePieceAbsVal = Math.abs(PieceValues.getPieceValue(pieceToCapture));
            // TODO: Implement when attacked squares are stored
            // if square is not attacked, dont substract movePieceAbsVal
            score += 2 * capturePieceAbsVal - movePieceAbsVal;
        }
        //System.out.println(move + " scored " + score);
        return score;
    }
}