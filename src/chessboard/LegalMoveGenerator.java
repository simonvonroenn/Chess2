package chessboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LegalMoveGenerator {
    // Castling rights management
    public static boolean whiteKingSideCastling = true;
    public static boolean whiteQueenSideCastling = true;
    public static boolean blackKingSideCastling = true;
    public static boolean blackQueenSideCastling = true;

    // En passant target square: null if none, else [row, col]
    public static int[] enPassantTarget = null;

    /**
     * Generates legal moves for a given piece in a position.
     *
     * @param board the position
     * @param row row of the piece
     * @param col col of the piece
     * @param whiteToMove indicates if white or black is to move
     * @return a list of legal moves
     */
    public static List<Move> generateLegalMoves(char[][] board, int row, int col, boolean whiteToMove) {
        List<Move> pseudoMoves = new ArrayList<>();
        char piece = board[row][col];

        // Asserts correct player
        if ((whiteToMove && Character.isLowerCase(piece)) || (!whiteToMove && Character.isUpperCase(piece))) {
            return Collections.emptyList();
        }

        switch (Character.toLowerCase(piece)) {
            case 'p' -> generatePawnMoves(board, row, col, pseudoMoves, whiteToMove);
            case 'r' -> generateRookMoves(board, row, col, pseudoMoves);
            case 'n' -> generateKnightMoves(board, row, col, pseudoMoves);
            case 'b' -> generateBishopMoves(board, row, col, pseudoMoves);
            case 'q' -> generateQueenMoves(board, row, col, pseudoMoves);
            case 'k' -> generateKingMoves(board, row, col, pseudoMoves);
        }
        // Filter out moves that leave the king in check
        List<Move> legalMoves = new ArrayList<>();
        for (Move move : pseudoMoves) {
            char[][] boardCopy = applyMove(board, move, whiteToMove);
            if (!isKingInCheck(boardCopy, whiteToMove)) { // Check if move leaves own king in check
                if (isKingInCheck(boardCopy, !whiteToMove)) { // Check if move checks the opponent's king
                    move.setCheck();
                }
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    private static void generatePawnMoves(char[][] board, int row, int col, List<Move> moves, boolean whiteToMove) {
        int direction = whiteToMove ? -1 : 1;
        int startRow = whiteToMove ? 6 : 1;
        int newRow = row + direction;

        // Move
        if (isNotOutOfBoard(newRow, col) && isEmpty(board, newRow, col)) {
            moves.add(new Move(row, col, newRow, col, false));
            // Move two fields on first move
            if (row == startRow && isEmpty(board, newRow + direction, col)) {
                moves.add(new Move(row, col, newRow + direction, col, false));
            }
        }

        // Capture a piece
        for (int side = -1; side <= 1; side += 2) {
            int newCol = col + side;
            if (isNotOutOfBoard(newRow, newCol) && isNotOwnPiece(board, row, col, newRow, newCol)) {
                moves.add(new Move(row, col, newRow, newCol, true));
            }
        }

        // En Passant
        if (enPassantTarget != null) {
            int epRow = enPassantTarget[0];
            int epCol = enPassantTarget[1];
            if (newRow == epRow && Math.abs(col - epCol) == 1) {
                // Check that there is an enemy pawn in the correct position
                int capturedPawnRow = whiteToMove ? newRow + 1 : newRow - 1;
                if (isNotOutOfBoard(capturedPawnRow, epCol) && Character.toLowerCase(board[capturedPawnRow][epCol]) == 'p') {
                    moves.add(new Move(row, col, newRow, epCol, true));
                }
            }
        }
    }

    private static void generateRookMoves(char[][] board, int row, int col, List<Move> moves) {
        generateSlidingMoves(board, row, col, moves, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
    }

    private static void generateBishopMoves(char[][] board, int row, int col, List<Move> moves) {
        generateSlidingMoves(board, row, col, moves, new int[][]{{1, 1}, {-1, -1}, {1, -1}, {-1, 1}});
    }

    private static void generateQueenMoves(char[][] board, int row, int col, List<Move> moves) {
        generateSlidingMoves(board, row, col, moves, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}});
    }

    private static void generateKnightMoves(char[][] board, int row, int col, List<Move> moves) {
        int[][] knightMoves = {{-2, -1}, {-2, 1}, {2, -1}, {2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}};
        for (int[] move : knightMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (isValid(board, row, col, newRow, newCol)) {
                boolean capture = !isEmpty(board, newRow, newCol);
                moves.add(new Move(row, col, newRow, newCol, capture));
            }
        }
    }

    private static void generateKingMoves(char[][] board, int row, int col, List<Move> moves) {
        int[][] kingMoves = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        for (int[] move : kingMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (isValid(board, row, col, newRow, newCol)) {
                boolean capture = !isEmpty(board, newRow, newCol);
                moves.add(new Move(row, col, newRow, newCol, capture));
            }
        }
        // Castling moves
        // Only add castling moves if king is in its original position and not in check
        if (board[row][col] == 'K' && row == 7 && col == 4) {
            // White kingside castling
            if (whiteKingSideCastling &&
                    isEmpty(board, 7, 5) && isEmpty(board, 7, 6) &&
                    !isSquareAttacked(board, 7, 4, false) &&
                    !isSquareAttacked(board, 7, 5, false) &&
                    !isSquareAttacked(board, 7, 6, false)) {
                moves.add(new Move(row, col, 7, 6, false));
            }
            // White queenside castling
            if (whiteQueenSideCastling &&
                    isEmpty(board, 7, 1) && isEmpty(board, 7, 2) && isEmpty(board, 7, 3) &&
                    !isSquareAttacked(board, 7, 4, false) &&
                    !isSquareAttacked(board, 7, 3, false) &&
                    !isSquareAttacked(board, 7, 2, false)) {
                moves.add(new Move(row, col, 7, 2, false));
            }
        } else if (board[row][col] == 'k' && row == 0 && col == 4) {
            // Black kingside castling
            if (blackKingSideCastling &&
                    isEmpty(board, 0, 5) && isEmpty(board, 0, 6) &&
                    !isSquareAttacked(board, 0, 4, true) &&
                    !isSquareAttacked(board, 0, 5, true) &&
                    !isSquareAttacked(board, 0, 6, true)) {
                moves.add(new Move(row, col, 0, 6, false));
            }
            // Black queenside castling
            if (blackQueenSideCastling &&
                    isEmpty(board, 0, 1) && isEmpty(board, 0, 2) && isEmpty(board, 0, 3) &&
                    !isSquareAttacked(board, 0, 4, true) &&
                    !isSquareAttacked(board, 0, 3, true) &&
                    !isSquareAttacked(board, 0, 2, true)) {
                moves.add(new Move(row, col, 0, 2, false));
            }
        }
    }

    private static void generateSlidingMoves(char[][] board, int row, int col, List<Move> moves, int[][] directions) {
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            while (isNotOutOfBoard(newRow, newCol) && isEmpty(board, newRow, newCol)) {
                moves.add(new Move(row, col, newRow, newCol, false));
                newRow += dir[0];
                newCol += dir[1];
            }
            if (isValid(board, row, col, newRow, newCol)) {
                moves.add(new Move(row, col, newRow, newCol, true));
            }
        }
    }

    private static boolean isValid(char[][] board, int row, int col, int newRow, int newCol) {
        return isNotOutOfBoard(newRow, newCol) && (isEmpty(board, newRow, newCol) || isNotOwnPiece(board, row, col, newRow, newCol));
    }

    private static boolean isNotOutOfBoard(int newRow, int newCol) {
        return newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8;
    }

    private static boolean isEmpty(char[][] board, int newRow, int newCol) {
        return board[newRow][newCol] == '\0';
    }

    public static boolean isNotOwnPiece(char[][] board, int row, int col, int newRow, int newCol) {
        return Character.isLetter(board[newRow][newCol]) && Character.isUpperCase(board[row][col]) != Character.isUpperCase(board[newRow][newCol]);
    }

    /**
     * Update castling rights and en passant target based on the move performed.
     *
     * @param board the current chess position
     * @param fromRow move from row
     * @param fromCol move from col
     * @param toRow move to row
     * @param toCol move to col
     * @param capturedPiece the char of the captured piece or '\0' if no piece was captured
     */
    public static void updateRightsAndEnPassant(char[][] board, int fromRow, int fromCol, int toRow, int toCol, char capturedPiece) {
        char movingPiece = board[toRow][toCol];
        // For kings: remove castling rights if moved
        if (movingPiece == 'K') {
            whiteKingSideCastling = false;
            whiteQueenSideCastling = false;
        } else if (movingPiece == 'k') {
            blackKingSideCastling = false;
            blackQueenSideCastling = false;
        }
        // For rooks: if rook moved, remove corresponding rights
        if (movingPiece == 'R') {
            if (fromRow == 7 && fromCol == 0) {
                whiteQueenSideCastling = false;
            }
            if (fromRow == 7 && fromCol == 7) {
                whiteKingSideCastling = false;
            }
        } else if (movingPiece == 'r') {
            if (fromRow == 0 && fromCol == 0) {
                blackQueenSideCastling = false;
            }
            if (fromRow == 0 && fromCol == 7) {
                blackKingSideCastling = false;
            }
        }
        // If a rook is captured from its original square, update castling rights
        if (capturedPiece == 'R') {
            if (toRow == 7 && toCol == 0) {
                whiteQueenSideCastling = false;
            }
            if (toRow == 7 && toCol == 7) {
                whiteKingSideCastling = false;
            }
        } else if (capturedPiece == 'r') {
            if (toRow == 0 && toCol == 0) {
                blackQueenSideCastling = false;
            }
            if (toRow == 0 && toCol == 7) {
                blackKingSideCastling = false;
            }
        }
        // En passant: if pawn moved two squares forward, set en passant target, else clear.
        if (Character.toLowerCase(movingPiece) == 'p' && Math.abs(toRow - fromRow) == 2) {
            int epRow = (fromRow + toRow) / 2;
            enPassantTarget = new int[]{epRow, fromCol};
        } else {
            enPassantTarget = null;
        }
    }

    /**
     * Applies a move to a copy of the board and returns this copy.
     *
     * @param board the current position
     * @param move the move to apply
     * @param whiteToMove if white to move
     * @return the position after move application
     */
    public static char[][] applyMove(char[][] board, Move move, boolean whiteToMove) {
        char[][] boardCopy = copyBoard(board);
        char movingPiece = boardCopy[move.fromRow][move.fromCol];
        if (Character.toLowerCase(movingPiece) == 'k' && Math.abs(move.toCol - move.fromCol) == 2) {
            // Simulate castling
            boardCopy[move.toRow][move.toCol] = movingPiece;
            boardCopy[move.fromRow][move.fromCol] = '\0';
            if (move.toCol > move.fromCol) { // kingside
                boardCopy[move.toRow][move.toCol - 1] = boardCopy[move.toRow][7];
                boardCopy[move.toRow][7] = '\0';
            } else { // queenside
                boardCopy[move.toRow][move.toCol + 1] = boardCopy[move.toRow][0];
                boardCopy[move.toRow][0] = '\0';
            }
        } else if (Character.toLowerCase(movingPiece) == 'p' && move.fromCol != move.toCol && boardCopy[move.toRow][move.toCol] == '\0') {
            // Simulate en passant capture
            boardCopy[move.toRow][move.toCol] = movingPiece;
            boardCopy[move.fromRow][move.fromCol] = '\0';
            int capturedPawnRow = whiteToMove ? move.toRow + 1 : move.toRow - 1;
            boardCopy[capturedPawnRow][move.toCol] = '\0';
        } else {
            // Normal move
            boardCopy[move.toRow][move.toCol] = movingPiece;
            boardCopy[move.fromRow][move.fromCol] = '\0';
        }
        return boardCopy;
    }

    /**
     * Creates a deep copy of the board.
     *
     * @param board the current chess position
     * @return the deep copy of the board
     */
    public static char[][] copyBoard(char[][] board) {
        char[][] copy = new char[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, 8);
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
    public static boolean isKingInCheck(char[][] board, boolean whiteKing) {
        int kingRow = -1, kingCol = -1;
        char kingChar = whiteKing ? 'K' : 'k';
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == kingChar) {
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
    private static boolean isSquareAttacked(char[][] board, int targetRow, int targetCol, boolean byWhite) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                char piece = board[r][c];
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
                                if (board[rr][cc] != '\0') break;
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
                                if (board[rr][cc] != '\0') break;
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
                                if (board[rr][cc] != '\0') break;
                                rr += d[0]; cc += d[1];
                            }
                        }
                        for (int[] d : directionsDiag) {
                            int rr = r + d[0], cc = c + d[1];
                            while (isNotOutOfBoard(rr, cc)) {
                                if (rr == targetRow && cc == targetCol) return true;
                                if (board[rr][cc] != '\0') break;
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
     * @param whiteToMove indicates if white is to move
     * @return true if the position is a checkmate, otherwise false
     */
    public static boolean isCheckmate(char[][] board, boolean whiteToMove) {
        // Checkmate occurs if no legal moves exist and the king is in check
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board[row][col];
                if (piece == '\0') continue;
                if ((whiteToMove && Character.isUpperCase(piece)) || (!whiteToMove && Character.isLowerCase(piece))) {
                    List<Move> moves = generateLegalMoves(board, row, col, whiteToMove);
                    if (!moves.isEmpty()) return false; // The player has at least one legal move
                }
            }
        }
        return isKingInCheck(board, whiteToMove); // Checkmate only if the king IS in check
    }

    /**
     * Checks if the current player is in stalemate.
     *
     * @param board the current chess position
     * @param whiteToMove indicates if white is to move
     * @return true if the position is a stalemate, otherwise false
     */
    public static boolean isStalemate(char[][] board, boolean whiteToMove) {
        // Stalemate occurs if the current player has no legal moves and is not in check
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board[row][col];
                if (piece == '\0') continue;
                if ((whiteToMove && Character.isUpperCase(piece)) || (!whiteToMove && Character.isLowerCase(piece))) {
                    List<Move> legalMoves = generateLegalMoves(board, row, col, whiteToMove);
                    if (!legalMoves.isEmpty()) return false; // The player has at least one legal move
                }
            }
        }
        return !isKingInCheck(board, whiteToMove); // Stalemate only if the king IS NOT in check
    }
}
