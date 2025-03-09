import java.util.ArrayList;
import java.util.List;

public class LegalMoveGenerator {
    /**
     * Generates legal moves for a given piece in a position.
     *
     * @param board the position
     * @param row row of the piece
     * @param col col of the piece
     * @param whiteToMove indicates if white or black is to move
     * @return a list of legal moves
     */
    public static List<int[]> generateLegalMoves(char[][] board, int row, int col, boolean whiteToMove) {
        List<int[]> moves = new ArrayList<>();
        char piece = board[row][row];

        // Asserts correct player
        if ((whiteToMove && Character.isLowerCase(piece)) || (!whiteToMove && Character.isUpperCase(piece))) {
            return moves;
        }

        switch (Character.toLowerCase(piece)) {
            case 'p' -> generatePawnMoves(board, row, col, moves, whiteToMove);
            case 'r' -> generateRookMoves(board, row, col, moves);
            case 'n' -> generateKnightMoves(board, row, col, moves);
            case 'b' -> generateBishopMoves(board, row, col, moves);
            case 'q' -> generateQueenMoves(board, row, col, moves);
            case 'k' -> generateKingMoves(board, row, col, moves);
        }
        return moves;
    }

    private static void generatePawnMoves(char[][] board, int row, int col, List<int[]> moves, boolean whiteToMove) {
        int direction = whiteToMove ? -1 : 1;
        int startRow = whiteToMove ? 6 : 1;
        int newRow = row + direction;

        // Move
        if (isValid(newRow, col) && board[newRow][col] == '\0') {
            moves.add(new int[]{newRow, col});
            // Move two fields on first move
            if (row == startRow && board[newRow + direction][col] == '\0') {
                moves.add(new int[]{newRow + direction, col});
            }
        }

        // Capture a piece
        for (int side = -1; side <= 1; side += 2) {
            int newCol = col + side;
            if (isValid(newRow, newCol) && Character.isLetter(board[newRow][newCol])
                    && Character.isUpperCase(board[newRow][newCol]) != whiteToMove) {
                moves.add(new int[]{newRow, newCol});
            }
        }

        // En Passant
    }

    private static void generateRookMoves(char[][] board, int row, int col, List<int[]> moves) {
        generateSlidingMoves(board, row, col, moves, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
    }

    private static void generateBishopMoves(char[][] board, int row, int col, List<int[]> moves) {
        generateSlidingMoves(board, row, col, moves, new int[][]{{1, 1}, {-1, -1}, {1, -1}, {-1, 1}});
    }

    private static void generateQueenMoves(char[][] board, int row, int col, List<int[]> moves) {
        generateSlidingMoves(board, row, col, moves, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}});
    }

    private static void generateKnightMoves(char[][] board, int row, int col, List<int[]> moves) {
        int[][] knightMoves = {{-2, -1}, {-2, 1}, {2, -1}, {2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}};
        for (int[] move : knightMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (isValid(newRow, newCol)) {
                moves.add(new int[]{newRow, newCol});
            }
        }
    }

    private static void generateKingMoves(char[][] board, int row, int col, List<int[]> moves) {
        int[][] kingMoves = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        for (int[] move : kingMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (isValid(newRow, newCol)) {
                moves.add(new int[]{newRow, newCol});
            }
        }
    }

    private static void generateSlidingMoves(char[][] board, int row, int col, List<int[]> moves, int[][] directions) {
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            while (isValid(newRow, newCol) && board[newRow][newCol] == '\0') {
                moves.add(new int[]{newRow, newCol});
                newRow += dir[0];
                newCol += dir[1];
            }
            if (isValid(newRow, newCol) && Character.isLetter(board[newRow][newCol]) && Character.isUpperCase(board[row][col]) != Character.isUpperCase(board[newRow][newCol])) {
                moves.add(new int[]{newRow, newCol});
            }
        }
    }

    private static boolean isValid(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
