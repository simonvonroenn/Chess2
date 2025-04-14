package chessboard;

import engine.Engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LegalMoveGenerator {

    /**
     * Generates legal moves for a given piece in a position.
     *
     * @param board the position
     * @param row row of the piece
     * @param col col of the piece
     * @return a list of legal moves
     */
    public static List<Move> generateLegalMoves(BoardEnv board, int row, int col) {
        List<Move> pseudoMoves = new ArrayList<>();
        char piece = board.state[row][col];

        // Asserts correct player
        if ((board.whiteToMove && Character.isLowerCase(piece)) || (!board.whiteToMove && Character.isUpperCase(piece))) {
            return Collections.emptyList();
        }

        switch (Character.toLowerCase(piece)) {
            case 'p' -> generatePawnMoves(board, row, col, pseudoMoves);
            case 'r' -> generateRookMoves(board, row, col, pseudoMoves);
            case 'n' -> generateKnightMoves(board, row, col, pseudoMoves);
            case 'b' -> generateBishopMoves(board, row, col, pseudoMoves);
            case 'q' -> generateQueenMoves(board, row, col, pseudoMoves);
            case 'k' -> generateKingMoves(board, row, col, pseudoMoves);
        }
        // Filter out moves that leave the king in check
        List<Move> legalMoves = new ArrayList<>();
        for (Move move : pseudoMoves) {
            char pieceCaptured = applyMove(board, move);
            if (!isKingInCheck(board, board.whiteToMove)) { // Check if move leaves own king in check
                if (isKingInCheck(board, !board.whiteToMove)) { // Check if move checks the opponent's king
                    move.setCheck();
                }
                legalMoves.add(move);
            }
            undoMove(board, move, pieceCaptured);
        }
        return legalMoves;
    }

    private static void generatePawnMoves(BoardEnv board, int row, int col, List<Move> moves) {
        int direction = board.whiteToMove ? -1 : 1;
        int startRow = board.whiteToMove ? 6 : 1;
        int newRow = row + direction;

        // Move
        if (isNotOutOfBoard(newRow, col) && isEmpty(board, newRow, col)) {
            if (newRow == 0 || newRow == 7) {
                Move promoteQueen = new Move(board.whiteToMove ? 'P' : 'p', row, col, newRow, col, false);
                promoteQueen.setPromotionPiece(board.whiteToMove ? 'Q' : 'q');
                Move promoteKnight = new Move(board.whiteToMove ? 'P' : 'p', row, col, newRow, col, false);
                promoteKnight.setPromotionPiece(board.whiteToMove ? 'N' : 'n');
                Move promoteRook = new Move(board.whiteToMove ? 'P' : 'p', row, col, newRow, col, false);
                promoteRook.setPromotionPiece(board.whiteToMove ? 'R' : 'r');
                Move promoteBishop = new Move(board.whiteToMove ? 'P' : 'p', row, col, newRow, col, false);
                promoteBishop.setPromotionPiece(board.whiteToMove ? 'B' : 'b');
                moves.addAll(List.of(promoteQueen, promoteKnight, promoteRook, promoteBishop));
            } else {
                moves.add(new Move(board.whiteToMove ? 'P' : 'p', row, col, newRow, col, false));
            }
            // Move two fields on first move
            if (row == startRow && isEmpty(board, newRow + direction, col)) {
                moves.add(new Move(board.whiteToMove ? 'P' : 'p', row, col, newRow + direction, col, false));
            }
        }

        // Capture a piece
        for (int side = -1; side <= 1; side += 2) {
            int newCol = col + side;
            if (isNotOutOfBoard(newRow, newCol) && isNotOwnPiece(board, row, col, newRow, newCol)) {
                if (newRow == 0 || newRow == 7) {
                    Move promoteQueen = new Move(board.whiteToMove ? 'P' : 'p', row, col, newRow, newCol, true);
                    promoteQueen.setPromotionPiece('Q');
                    Move promoteKnight = new Move(board.whiteToMove ? 'P' : 'p', row, col, newRow, newCol, true);
                    promoteKnight.setPromotionPiece('N');
                    Move promoteRook = new Move(board.whiteToMove ? 'P' : 'p', row, col, newRow, newCol, true);
                    promoteRook.setPromotionPiece('R');
                    Move promoteBishop = new Move(board.whiteToMove ? 'P' : 'p', row, col, newRow, newCol, true);
                    promoteBishop.setPromotionPiece('B');
                    moves.addAll(List.of(promoteQueen, promoteKnight, promoteRook, promoteBishop));
                } else {
                    moves.add(new Move(board.whiteToMove ? 'P' : 'p', row, col, newRow, newCol, true));
                }
            }
        }

        // En Passant
        if (board.enPassantTarget != null) {
            int epRow = board.enPassantTarget[0];
            int epCol = board.enPassantTarget[1];
            if (newRow == epRow && Math.abs(col - epCol) == 1) {
                // Check that there is an enemy pawn in the correct position
                int capturedPawnRow = board.whiteToMove ? newRow + 1 : newRow - 1;
                if (isNotOutOfBoard(capturedPawnRow, epCol) && Character.toLowerCase(board.state[capturedPawnRow][epCol]) == 'p') {
                    moves.add(new Move(board.whiteToMove ? 'P' : 'p', row, col, newRow, epCol, true));
                }
            }
        }
    }

    private static void generateRookMoves(BoardEnv board, int row, int col, List<Move> moves) {
        generateSlidingMoves(board, row, col, moves, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}, board.whiteToMove ? 'R' : 'r');
    }

    private static void generateBishopMoves(BoardEnv board, int row, int col, List<Move> moves) {
        generateSlidingMoves(board, row, col, moves, new int[][]{{1, 1}, {-1, -1}, {1, -1}, {-1, 1}}, board.whiteToMove ? 'B' : 'b');
    }

    private static void generateQueenMoves(BoardEnv board, int row, int col, List<Move> moves) {
        generateSlidingMoves(board, row, col, moves, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}}, board.whiteToMove ? 'Q' : 'q');
    }

    private static void generateKnightMoves(BoardEnv board, int row, int col, List<Move> moves) {
        int[][] knightMoves = {{-2, -1}, {-2, 1}, {2, -1}, {2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}};
        for (int[] move : knightMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (isValid(board, row, col, newRow, newCol)) {
                boolean capture = !isEmpty(board, newRow, newCol);
                moves.add(new Move(board.whiteToMove ? 'N' : 'n', row, col, newRow, newCol, capture));
            }
        }
    }

    private static void generateKingMoves(BoardEnv board, int row, int col, List<Move> moves) {
        int[][] kingMoves = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        for (int[] move : kingMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (isValid(board, row, col, newRow, newCol)) {
                boolean capture = !isEmpty(board, newRow, newCol);
                moves.add(new Move(board.whiteToMove ? 'K' : 'k', row, col, newRow, newCol, capture));
            }
        }
        // Castling moves
        // Only add castling moves if king is in its original position and not in check
        if (board.state[row][col] == 'K' && row == 7 && col == 4) {
            // White kingside castling
            if (board.whiteKingSideCastling &&
                    isEmpty(board, 7, 5) && isEmpty(board, 7, 6) &&
                    !isSquareAttacked(board, 7, 4, false) &&
                    !isSquareAttacked(board, 7, 5, false) &&
                    !isSquareAttacked(board, 7, 6, false)) {
                moves.add(new Move(board.whiteToMove ? 'K' : 'k', row, col, 7, 6, false));
            }
            // White queenside castling
            if (board.whiteQueenSideCastling &&
                    isEmpty(board, 7, 1) && isEmpty(board, 7, 2) && isEmpty(board, 7, 3) &&
                    !isSquareAttacked(board, 7, 4, false) &&
                    !isSquareAttacked(board, 7, 3, false) &&
                    !isSquareAttacked(board, 7, 2, false)) {
                moves.add(new Move(board.whiteToMove ? 'K' : 'k', row, col, 7, 2, false));
            }
        } else if (board.state[row][col] == 'k' && row == 0 && col == 4) {
            // Black kingside castling
            if (board.blackKingSideCastling &&
                    isEmpty(board, 0, 5) && isEmpty(board, 0, 6) &&
                    !isSquareAttacked(board, 0, 4, true) &&
                    !isSquareAttacked(board, 0, 5, true) &&
                    !isSquareAttacked(board, 0, 6, true)) {
                moves.add(new Move(board.whiteToMove ? 'K' : 'k', row, col, 0, 6, false));
            }
            // Black queenside castling
            if (board.blackQueenSideCastling &&
                    isEmpty(board, 0, 1) && isEmpty(board, 0, 2) && isEmpty(board, 0, 3) &&
                    !isSquareAttacked(board, 0, 4, true) &&
                    !isSquareAttacked(board, 0, 3, true) &&
                    !isSquareAttacked(board, 0, 2, true)) {
                moves.add(new Move(board.whiteToMove ? 'K' : 'k', row, col, 0, 2, false));
            }
        }
    }

    private static void generateSlidingMoves(BoardEnv board, int row, int col, List<Move> moves, int[][] directions, char piece) {
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            while (isNotOutOfBoard(newRow, newCol) && isEmpty(board, newRow, newCol)) {
                moves.add(new Move(piece, row, col, newRow, newCol, false));
                newRow += dir[0];
                newCol += dir[1];
            }
            if (isValid(board, row, col, newRow, newCol)) {
                moves.add(new Move(piece, row, col, newRow, newCol, true));
            }
        }
    }

    private static boolean isValid(BoardEnv board, int row, int col, int newRow, int newCol) {
        return isNotOutOfBoard(newRow, newCol) && (isEmpty(board, newRow, newCol) || isNotOwnPiece(board, row, col, newRow, newCol));
    }

    private static boolean isNotOutOfBoard(int newRow, int newCol) {
        return newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8;
    }

    private static boolean isEmpty(BoardEnv board, int newRow, int newCol) {
        return board.state[newRow][newCol] == '\0';
    }

    public static boolean isNotOwnPiece(BoardEnv board, int row, int col, int newRow, int newCol) {
        return Character.isLetter(board.state[newRow][newCol]) && Character.isUpperCase(board.state[row][col]) != Character.isUpperCase(board.state[newRow][newCol]);
    }

    /**
     * Applies a move to a copy of the board and returns this copy.
     *
     * @param board the current position
     * @param move the move to apply
     * @return the position after move application
     */
    public static char applyMove(BoardEnv board, Move move) {
        long startTime = System.currentTimeMillis();
        //BoardEnv boardCopy = copyBoard(board);
        char pieceCaptured = board.state[move.toRow][move.toCol];
        if (Character.toLowerCase(move.piece) == 'k' && Math.abs(move.toCol - move.fromCol) == 2) {
            // Simulate castling
            board.state[move.toRow][move.toCol] = move.piece;
            board.state[move.fromRow][move.fromCol] = '\0';
            if (move.toCol > move.fromCol) { // kingside
                board.state[move.toRow][move.toCol - 1] = board.state[move.toRow][7];
                board.state[move.toRow][7] = '\0';
                if (board.whiteToMove) board.whiteKingSideCastling = true; else board.blackKingSideCastling = true;
            } else { // queenside
                board.state[move.toRow][move.toCol + 1] = board.state[move.toRow][0];
                board.state[move.toRow][0] = '\0';
                if (board.whiteToMove) board.whiteQueenSideCastling = true; else board.blackQueenSideCastling = true;
            }
        } else if (Character.toLowerCase(move.piece) == 'p'
                && move.fromCol != move.toCol
                && board.enPassantTarget != null
                && move.toRow == board.enPassantTarget[0]
                && move.toCol == board.enPassantTarget[1]) {
            // Simulate en passant capture
            board.state[move.toRow][move.toCol] = move.piece;
            board.state[move.fromRow][move.fromCol] = '\0';
            int capturedPawnRow = board.whiteToMove ? move.toRow + 1 : move.toRow - 1;
            pieceCaptured = board.state[capturedPawnRow][move.toCol];
            board.state[capturedPawnRow][move.toCol] = '\0';
            board.previousEnPassantTarget = board.enPassantTarget;
        } else {
            if (Character.toLowerCase(move.piece) == 'p' && Math.abs(move.toRow - move.fromRow) == 2
                    && (move.toCol < 7 && board.state[move.toRow][move.toCol+1] == (Character.isUpperCase(move.piece) ? 'p' : 'P')
                    || move.toCol > 0 && board.state[move.toRow][move.toCol-1] == (Character.isUpperCase(move.piece) ? 'p' : 'P'))) {
                // Set en passant target
                int epRow = (move.fromRow + move.toRow) / 2;
                board.enPassantTarget = new int[]{epRow, move.fromCol};
            }
            // Normal move
            board.state[move.toRow][move.toCol] = move.piece;
            board.state[move.fromRow][move.fromCol] = '\0';
        }
        if ((move.piece == 'P' && move.toRow == 0) || (move.piece == 'p' && move.toRow == 7)) {
            board.state[move.toRow][move.toCol] = move.promotionPiece;
        }
        Engine._debugTime_ApplyMove += System.currentTimeMillis() - startTime;
        return pieceCaptured;
    }

    public static void undoMove(BoardEnv board, Move move,  char pieceCaptured) {
        long startTime = System.currentTimeMillis();
        if (Character.toLowerCase(move.piece) == 'k' && Math.abs(move.toCol - move.fromCol) == 2) {
            // Undo castling
            board.state[move.fromRow][move.fromCol] = move.piece;
            board.state[move.toRow][move.toCol] = '\0';
            if (move.toCol > move.fromCol) { // kingside
                board.state[move.toRow][7] = board.state[move.toRow][move.toCol - 1];
                board.state[move.toRow][move.toCol - 1] = '\0';
                if (board.whiteToMove) board.whiteKingSideCastling = false; else board.blackKingSideCastling = false;
            } else { // queenside
                board.state[move.toRow][0] = board.state[move.toRow][move.toCol + 1];
                board.state[move.toRow][move.toCol + 1] = '\0';
                if (board.whiteToMove) board.whiteQueenSideCastling = false; else board.blackQueenSideCastling = false;
            }
        } else if (Character.toLowerCase(move.piece) == 'p'
                && move.fromCol != move.toCol
                && board.previousEnPassantTarget != null
                && move.toRow == board.previousEnPassantTarget[0]
                && move.toCol == board.previousEnPassantTarget[1]) {
            // Undo en passant capture
            board.state[move.fromRow][move.fromCol] = move.piece;
            board.state[move.toRow][move.toCol] = '\0';
            int capturedPawnRow = board.whiteToMove ? move.toRow + 1 : move.toRow - 1;
            board.state[capturedPawnRow][move.toCol] = board.whiteToMove ? 'p' : 'P';
            board.enPassantTarget = board.previousEnPassantTarget;
            board.previousEnPassantTarget = null;
        } else {
            if (Character.toLowerCase(move.piece) == 'p' && Math.abs(move.toRow - move.fromRow) == 2
                    && (move.toCol < 7 && board.state[move.toRow][move.toCol+1] == (Character.isUpperCase(move.piece) ? 'p' : 'P')
                    || move.toCol > 0 && board.state[move.toRow][move.toCol-1] == (Character.isUpperCase(move.piece) ? 'p' : 'P'))) {
                // Reset en passant target
                board.enPassantTarget = null;
            }
            // Normal move
            board.state[move.fromRow][move.fromCol] = move.piece;
            board.state[move.toRow][move.toCol] = pieceCaptured;
        }
        Engine._debugTime_ApplyMove += System.currentTimeMillis() - startTime;
    }

    /**
     * Creates a deep copy of the board.
     *
     * @param board the current chess position
     * @return the deep copy of the board
     */
    @Deprecated
    public static char[][] copyBoard(BoardEnv board) {
        char[][] copy = new char[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(board.state[i], 0, copy[i], 0, 8);
        }
        return copy;
    }

    /**
     * Checks if the king is in check.
     *
     * @param board the current chess position
     * @param whiteKing true, if it is the white king to be checked, false if it is the black king
     * @return true if the king is in check, otherwise false
     */
    public static boolean isKingInCheck(BoardEnv board, boolean whiteKing) {
        int kingRow = -1, kingCol = -1;
        char kingChar = whiteKing ? 'K' : 'k';
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board.state[i][j] == kingChar) {
                    kingRow = i;
                    kingCol = j;
                    break;
                }
            }
            if (kingRow != -1) break;
        }
        if (kingRow == -1) return false;
        return isSquareAttacked(board, kingRow, kingCol, !whiteKing);
    }

    /**
     * Checks if a square is attacked by any piece of the specified color
     *
     * @param board the current chess position
     * @param targetRow the target row
     * @param targetCol the target col
     * @param byWhite the color
     * @return true, if the square is attacked, otherwise false
     */
    private static boolean isSquareAttacked(BoardEnv board, int targetRow, int targetCol, boolean byWhite) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                char piece = board.state[r][c];
                if (piece == '\0') continue;
                if (Character.isUpperCase(piece) != byWhite) continue;
                switch (Character.toLowerCase(piece)) {
                    case 'p' -> {
                        int direction = byWhite ? -1 : 1;
                        if (targetRow == r + direction && (targetCol == c - 1 || targetCol == c + 1)) {
                            return true;
                        }
                    }
                    case 'n' -> {
                        int[][] knightMoves = {{-2, -1}, {-2, 1}, {2, -1}, {2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}};
                        for (int[] move : knightMoves) {
                            if (r + move[0] == targetRow && c + move[1] == targetCol) return true;
                        }
                    }
                    case 'b' -> {
                        int[][] directionsDiag = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
                        for (int[] d : directionsDiag) {
                            int rr = r + d[0], cc = c + d[1];
                            while (isNotOutOfBoard(rr, cc)) {
                                if (rr == targetRow && cc == targetCol) return true;
                                if (board.state[rr][cc] != '\0') break;
                                rr += d[0]; cc += d[1];
                            }
                        }
                    }
                    case 'r' -> {
                        int[][] directionsStraight = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                        for (int[] d : directionsStraight) {
                            int rr = r + d[0], cc = c + d[1];
                            while (isNotOutOfBoard(rr, cc)) {
                                if (rr == targetRow && cc == targetCol) return true;
                                if (board.state[rr][cc] != '\0') break;
                                rr += d[0]; cc += d[1];
                            }
                        }
                    }
                    case 'q' -> {
                        int[][] directionsStraight = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                        int[][] directionsDiag = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
                        for (int[] d : directionsStraight) {
                            int rr = r + d[0], cc = c + d[1];
                            while (isNotOutOfBoard(rr, cc)) {
                                if (rr == targetRow && cc == targetCol) return true;
                                if (board.state[rr][cc] != '\0') break;
                                rr += d[0]; cc += d[1];
                            }
                        }
                        for (int[] d : directionsDiag) {
                            int rr = r + d[0], cc = c + d[1];
                            while (isNotOutOfBoard(rr, cc)) {
                                if (rr == targetRow && cc == targetCol) return true;
                                if (board.state[rr][cc] != '\0') break;
                                rr += d[0]; cc += d[1];
                            }
                        }
                    }
                    case 'k' -> {
                        int[][] kingMoves = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
                        for (int[] move : kingMoves) {
                            if (r + move[0] == targetRow && c + move[1] == targetCol) return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if the current position is a checkmate.
     *
     * @param board the current chess position
     * @return true if the position is a checkmate, otherwise false
     */
    public static boolean isCheckmate(BoardEnv board) {
        // Checkmate occurs if no legal moves exist and the king is in check
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board.state[row][col];
                if (piece == '\0') continue;
                if ((board.whiteToMove && Character.isUpperCase(piece)) || (!board.whiteToMove && Character.isLowerCase(piece))) {
                    List<Move> moves = generateLegalMoves(board, row, col);
                    if (!moves.isEmpty()) return false; // The player has at least one legal move
                }
            }
        }
        return isKingInCheck(board, board.whiteToMove); // Checkmate only if the king IS in check
    }

    /**
     * Checks if the current player is in stalemate.
     *
     * @param board the current chess position
     * @return true if the position is a stalemate, otherwise false
     */
    public static boolean isStalemate(BoardEnv board) {
        // Stalemate occurs if the current player has no legal moves and is not in check
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board.state[row][col];
                if (piece == '\0') continue;
                if ((board.whiteToMove && Character.isUpperCase(piece)) || (!board.whiteToMove && Character.isLowerCase(piece))) {
                    List<Move> legalMoves = generateLegalMoves(board, row, col);
                    if (!legalMoves.isEmpty()) return false; // The player has at least one legal move
                }
            }
        }
        return !isKingInCheck(board, board.whiteToMove); // Stalemate only if the king IS NOT in check
    }
}
