package main.engine;

import main.chessboard.BoardEnv;
import main.chessboard.Chessboard;
import main.chessboard.LegalMoveGenerator;
import main.chessboard.Move;

import java.util.ArrayList;
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
     * @return the best move found, or null if no move is available
     */
    public BestMove calculateBestMove(BoardEnv board) {
        _debugTime_ApplyMove = 0;
        BestMove bestMove;

        // Find all openings that follow the current position
        List<List<String>> remaining = new ArrayList<>();
        for (List<String> opening : Chessboard.openings) {
            if (opening.size() > board.playedMoves.size()) {
                boolean match = true;
                for (int i = 0; i < board.playedMoves.size(); i++) {
                    if (!opening.get(i).equals(board.playedMoves.get(i).toString())) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    remaining.add(opening);
                }
            }
        }
        Chessboard.openings = remaining;

        // If present, choose random opening continuation
        if (!Chessboard.openings.isEmpty()) {
            // Wähle zufällig eine der möglichen Openings, die noch passen.
            List<String> selected = Chessboard.openings.get((int) (Math.random() * Chessboard.openings.size()));
            // Der nächste Zug entspricht dem Zug an der Position playedMoves.size() in der Opening-Zeile.
            String nextMove = selected.get(board.playedMoves.size());
            Move move = createMoveFromSAN(board, nextMove);
            return new BestMove(move);
        }

        // Else, calculate best move by iterative DFS
        BoardEnv boardCopy = board.deepCopy();
        int[] evalInfo = Engine.evaluatePosition(boardCopy);
        boardCopy.evaluation = evalInfo[0];
        boardCopy.pieceValueSum = evalInfo[1];
        bestMove = DepthFirstSearchStrategy.iterativeDeepeningSearch(boardCopy);

        // Debugging
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
        public Integer evaluation;
        public List<String> moveSequence = new ArrayList<>();

        BestMove(Move move) {
            this.move = move;
        }

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
     * @param move the move
     */
    public static int[] evaluateMove(BoardEnv board, Move move) {
        long startTime = System.currentTimeMillis();
        int capturedPieceValue = 0;
        int newPieceValueSum = board.pieceValueSum;

        if (move.isCapture) {
            char capturedPiece;
            // En passant
            if (Character.toLowerCase(move.piece) == 'p' && move.fromCol != move.toCol && board.state[move.toRow][move.toCol] == '\0') {
                capturedPiece = (move.piece == 'P' ? board.state[move.toRow + 1][move.toCol] : board.state[move.toRow - 1][move.toCol]);
            } else {
                capturedPiece = board.state[move.toRow][move.toCol];
            }
            capturedPieceValue = PieceValues.getPieceValue(capturedPiece) + PieceValues.getPieceTableValue(capturedPiece, move.toRow, move.toCol, board.pieceValueSum);
            newPieceValueSum -= Math.abs(PieceValues.getPieceValue(capturedPiece));
        }

        int movePieceTableValueBefore = PieceValues.getPieceTableValue(move.piece, move.fromRow, move.fromCol, board.pieceValueSum);
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
            promotionPieceTableValue = PieceValues.getPieceTableValue(move.promotionPiece, move.toRow, move.toCol, board.pieceValueSum);
            movePieceTableValueAfter = 0;
            newPieceValueSum += Math.abs(promotionPieceValue);
        }

        int evalDelta = -capturedPieceValue + movePieceTableValueAfter - movePieceTableValueBefore
                        + castlingRookTableValueDelta // if castling
                        + promotionPieceValue + promotionPieceTableValue; // if promotion

        _debugTime_EvaluatePosition += System.currentTimeMillis() - startTime;

        return new int[]{board.evaluation + evalDelta, newPieceValueSum};
    }

    /**
     * Evaluates a position based on piece value and piece position value.
     *
     * @param board the current board state
     * @return the evaluation and the sum of all piece values
     */
    public static int[] evaluatePosition(BoardEnv board) {
        int evaluation = 0;
        int pieceValueSum = 0;
        int pieceTotalValue;
        int[] whiteKingPos = new int[0];
        int[] blackKingPos = new int[0];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board.state[row][col];
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
                    BoardEnv._debug_pieceValues[row][col] = Math.abs(pieceTotalValue);
                }
            }
        }
        pieceTotalValue = PieceValues.KING + PieceValues.getPieceTableValue('K', whiteKingPos[0], whiteKingPos[1], pieceValueSum);
        evaluation += pieceTotalValue;
        BoardEnv._debug_pieceValues[whiteKingPos[0]][whiteKingPos[1]] = Math.abs(pieceTotalValue);
        pieceTotalValue = -PieceValues.KING + PieceValues.getPieceTableValue('k', blackKingPos[0], blackKingPos[1], pieceValueSum);
        evaluation += pieceTotalValue;
        BoardEnv._debug_pieceValues[blackKingPos[0]][blackKingPos[1]] = Math.abs(pieceTotalValue);
        return new int[]{evaluation, pieceValueSum};
    }

    /**
     * Generates all legal moves for the current player from the given board state.
     *
     * @param board the current board state
     * @return a list of legal moves
     */
    protected static List<Move> generateAllLegalMoves(BoardEnv board) {
        long startTime = System.currentTimeMillis();
        List<Move> allMoves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board.state[row][col];
                if (piece == '\0') continue;
                if ((board.whiteToMove && Character.isUpperCase(piece)) || (!board.whiteToMove && Character.isLowerCase(piece))) {
                    allMoves.addAll(LegalMoveGenerator.generateLegalMoves(board, row, col));
                }
            }
        }
        _debugTime_GenerateAllLegalMoves += System.currentTimeMillis() - startTime;
        return allMoves;
    }

    protected static void orderMoves(BoardEnv board, List<Move> moves) {
        Map<Move, Integer> evaluationCache = new HashMap<>();
        for (Move move : moves) {
            evaluationCache.put(move, guessMoveScore(board, move));
        }
        // sort in descending order
        moves.sort((m1, m2) -> Integer.compare(evaluationCache.get(m2), evaluationCache.get(m1)));
        //System.out.println("MoveOrder: " + moves);
    }

    private static int guessMoveScore(BoardEnv board, Move move) {
        char pieceToMove = board.state[move.fromRow][move.fromCol];
        int score = PieceValues.getPieceTableValue(pieceToMove, move.toRow, move.toCol, board.pieceValueSum)
                    - PieceValues.getPieceTableValue(pieceToMove, move.fromRow, move.fromCol, board.pieceValueSum);
        int movePieceAbsVal = Math.abs(PieceValues.getPieceValue(pieceToMove));

        if (move.isCheck) {
            score += 2 * movePieceAbsVal;
        }

        if (move.isCapture) {
            char pieceToCapture = board.state[move.toRow][move.toCol];
            int capturePieceAbsVal = Math.abs(PieceValues.getPieceValue(pieceToCapture));
            // TODO: Implement when attacked squares are stored
            // if square is not attacked, dont substract movePieceAbsVal
            score += 2 * capturePieceAbsVal - movePieceAbsVal;
        }
        //System.out.println(move + " scored " + score);
        return score;
    }

    private Move createMoveFromSAN(BoardEnv board, String san) {
        System.out.println("SAN: " + san);
        boolean isWhite = board.whiteToMove;

        // Castling move
        if (san.equals("O-O")) {
            return new Move(isWhite ? 'K' : 'k', isWhite ? 7 : 0, 4, isWhite ? 7 : 0, 6, false);
        } else if (san.equals("O-O-O")) {
            return new Move(isWhite ? 'K' : 'k', isWhite ? 7 : 0, 4, isWhite ? 7 : 0, 2, false);
        }

        int toCol, toRow;
        char piece = 'P'; // default: pawn
        int fromCol = -1;
        boolean isCapture = san.charAt(1) == 'x';

        // Determine piece if piece is not a pawn
        if (Character.isUpperCase(san.charAt(0)) && san.charAt(0) != 'O') {
            piece = san.charAt(0);
        }

        // For pawns: determine origin column if move is capture
        if (piece == 'P' && san.length() == 4 && san.charAt(1) == 'x') {
            fromCol = san.charAt(0) - 'a';
        }

        // Extract target row and column
        String targetSquare = san.substring(san.length() - 2);
        toCol = targetSquare.charAt(0) - 'a';
        toRow = 8 - Character.getNumericValue(targetSquare.charAt(1));


        // Search board for piece
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char fig = board.state[row][col];
                if (fig == '\0') continue;
                if (isWhite != Character.isUpperCase(fig)) continue;
                if (Character.toUpperCase(fig) != piece) continue;

                if (piece == 'P' && (fromCol != -1 && col != fromCol || fromCol == -1 && col != toCol)) continue;
                if (!moveIsForPiece(piece, row, col, toRow, toCol)) continue;

                return new Move(isWhite ? fig : Character.toLowerCase(fig), row, col, toRow, toCol, isCapture);
            }
        }

        return null;
    }

    private boolean moveIsForPiece(char piece, int fromRow, int fromCol, int toRow, int toCol) {
        return switch (piece) {
            case 'R' -> fromRow == toRow || fromCol == toCol;
            case 'B' -> Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol);
            case 'N' -> Math.abs(fromRow - toRow) * Math.abs(fromCol - toCol) == 2;
            case 'P', 'Q', 'K' -> true; // Pawns are already determined via their column and queen and king are unique
            default -> false;
        };
    }
}