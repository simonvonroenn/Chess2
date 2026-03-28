package main.chessboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LegalMoveGenerator {

    private LegalMoveGenerator() {}

    /**
     * Generates legal moves for a given piece in a position.
     *
     * @param board the position
     * @param row row of the piece
     * @param col col of the piece
     * @return a list of legal moves
     */
    public static List<Move> generateLegalMoves(BoardEnv board, int row, int col, boolean skipPostMoveCalculations) {
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
            MakeMoveResult result = Chessboard.makeMove(board, move, skipPostMoveCalculations);
            if (!isKingInCheck(board, !board.whiteToMove)) { // Check if move leaves own king in check
                if (isKingInCheck(board, board.whiteToMove)) { // Check if move checks the opponent's king
                    move.setCheck();
                    if (result.outcome.isCheckmate()) {
                        move.setCheckmate();
                    }
                }
                legalMoves.add(move);
            }
            Chessboard.unmakeMove(board, move, result.undoInfo);
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
                    promoteQueen.setPromotionPiece(board.whiteToMove ? 'Q' : 'q');
                    Move promoteKnight = new Move(board.whiteToMove ? 'P' : 'p', row, col, newRow, newCol, true);
                    promoteKnight.setPromotionPiece(board.whiteToMove ? 'N' : 'n');
                    Move promoteRook = new Move(board.whiteToMove ? 'P' : 'p', row, col, newRow, newCol, true);
                    promoteRook.setPromotionPiece(board.whiteToMove ? 'R' : 'r');
                    Move promoteBishop = new Move(board.whiteToMove ? 'P' : 'p', row, col, newRow, newCol, true);
                    promoteBishop.setPromotionPiece(board.whiteToMove ? 'B' : 'b');
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
        boolean kingInCheck = isSquareAttacked(board, row, col, !board.whiteToMove);
        if (kingInCheck) return;
        if (board.state[row][col] == 'K' && row == 7 && col == 4) {
            // White kingside castling
            if (board.whiteKingSideCastling &&
                    isEmpty(board, 7, 5) && isEmpty(board, 7, 6) &&
                    !isSquareAttacked(board, 7, 5, false) &&
                    !isSquareAttacked(board, 7, 6, false)) {
                moves.add(new Move('K', row, col, 7, 6, false));
            }
            // White queenside castling
            if (board.whiteQueenSideCastling &&
                    isEmpty(board, 7, 1) && isEmpty(board, 7, 2) && isEmpty(board, 7, 3) &&
                    !isSquareAttacked(board, 7, 3, false) &&
                    !isSquareAttacked(board, 7, 2, false)) {
                moves.add(new Move('K', row, col, 7, 2, false));
            }
        } else if (board.state[row][col] == 'k' && row == 0 && col == 4) {
            // Black kingside castling
            if (board.blackKingSideCastling &&
                    isEmpty(board, 0, 5) && isEmpty(board, 0, 6) &&
                    !isSquareAttacked(board, 0, 5, true) &&
                    !isSquareAttacked(board, 0, 6, true)) {
                moves.add(new Move('k', row, col, 0, 6, false));
            }
            // Black queenside castling
            if (board.blackQueenSideCastling &&
                    isEmpty(board, 0, 1) && isEmpty(board, 0, 2) && isEmpty(board, 0, 3) &&
                    !isSquareAttacked(board, 0, 3, true) &&
                    !isSquareAttacked(board, 0, 2, true)) {
                moves.add(new Move('k', row, col, 0, 2, false));
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
     * Checks if the king is in check.
     *
     * @param board the current chess position
     * @param whiteKing true, if it is the white king to be checked, false if it is the black king
     * @return true if the king is in check, otherwise false
     */
    public static boolean isKingInCheck(BoardEnv board, boolean whiteKing) {
        int kingRow = whiteKing ? board.whiteKingPos[0] : board.blackKingPos[0];
        int kingCol = whiteKing ? board.whiteKingPos[1] : board.blackKingPos[1];
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
        int pawnDir = byWhite ? 1 : -1;

        // Straight rays — check first square for rook/queen/king, then continue for rook/queen
        for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            int rr = targetRow + d[0], cc = targetCol + d[1];
            if (!isNotOutOfBoard(rr, cc)) continue;
            char piece = board.state[rr][cc];
            if (piece != '\0') {
                if (Character.isUpperCase(piece) == byWhite) {
                    char p = Character.toLowerCase(piece);
                    if (p == 'r' || p == 'q' || p == 'k') return true;
                }
                continue; // blocked, no need to continue ray
            }
            // First square empty — continue ray from second square for rook/queen only
            rr += d[0]; cc += d[1];
            while (isNotOutOfBoard(rr, cc)) {
                piece = board.state[rr][cc];
                if (piece != '\0') {
                    if (Character.isUpperCase(piece) == byWhite) {
                        char p = Character.toLowerCase(piece);
                        if (p == 'r' || p == 'q') return true;
                    }
                    break;
                }
                rr += d[0]; cc += d[1];
            }
        }

        // Diagonal rays — check first square for bishop/queen/king/pawn, then continue for bishop/queen
        for (int[] d : new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}}) {
            int rr = targetRow + d[0], cc = targetCol + d[1];
            if (!isNotOutOfBoard(rr, cc)) continue;
            char piece = board.state[rr][cc];
            if (piece != '\0') {
                if (Character.isUpperCase(piece) == byWhite) {
                    char p = Character.toLowerCase(piece);
                    if (p == 'b' || p == 'q' || p == 'k') return true;
                    if (p == 'p' && d[0] == pawnDir) return true;
                }
                continue; // blocked
            }
            // First square empty — continue ray from second square for bishop/queen only
            rr += d[0]; cc += d[1];
            while (isNotOutOfBoard(rr, cc)) {
                piece = board.state[rr][cc];
                if (piece != '\0') {
                    if (Character.isUpperCase(piece) == byWhite) {
                        char p = Character.toLowerCase(piece);
                        if (p == 'b' || p == 'q') return true;
                    }
                    break;
                }
                rr += d[0]; cc += d[1];
            }
        }

        // Knights — L-shapes only, no sliding
        for (int[] d : new int[][]{{-2, -1}, {-2, 1}, {2, -1}, {2, 1}, {-1, -2}, {1, -2}, {-1, 2}, {1, 2}}) {
            int rr = targetRow + d[0], cc = targetCol + d[1];
            if (!isNotOutOfBoard(rr, cc)) continue;
            char piece = board.state[rr][cc];
            if (piece != '\0' && Character.isUpperCase(piece) == byWhite && Character.toLowerCase(piece) == 'n') {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the current position is a checkmate or a stalemate.
     *
     * @param board the current chess position
     * @return the game outcome
     */
    public static GameOutcome determineCheckmateOrStalemate(BoardEnv board) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board.state[row][col];
                if (piece == '\0') continue;
                if (board.whiteToMove == Character.isUpperCase(piece)) {
                    List<Move> moves = generateLegalMoves(board, row, col, true);
                    if (!moves.isEmpty()) return GameOutcome.ONGOING; // The player has at least one legal move
                }
            }
        }
        if (isKingInCheck(board, board.whiteToMove)) {
            return board.whiteToMove ? GameOutcome.CHECKMATE_WHITE : GameOutcome.CHECKMATE_BLACK;
        } else {
            return GameOutcome.STALEMATE;
        }
    }
}
