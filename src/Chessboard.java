import processing.core.PApplet;
import processing.core.PImage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Chessboard {

    private final PApplet sketch;

    /** The size of each tile in pixel. */
    public static final int TILE_SIZE = Chess2.BOARD_SIZE / 8;

    private final char[][] board = new char[8][8];

    private Map<Character, PImage> images;
    private int selectedRow, selectedCol = -1;
    private boolean whiteToMove = true;
    private List<int[]> legalMoves;

    /**
     * Integrates Processing in Java.
     */
    public Chessboard(PApplet sketch) {
        this.sketch = sketch;
    }

    /**
     *Loads the starting position at the beginning.
     */
    public void loadPosition() {
        String[] rows = Chess2.FEN.split(" ")[0].split("/");
        for (int i = 0; i < 8; i++) {
            int offset = 0;
            for (int j = 0; j < rows[i].length(); j++) {
                if (Character.isDigit(rows[i].charAt(j))) {
                    offset += Character.getNumericValue(rows[i].charAt(j)) - 1;
                } else {
                    board[i][j + offset] = rows[i].charAt(j);
                }
            }
        }
    }

    /**
     * Loads the images of the pieces.
     */
    public void loadImages() {
        String baseURL = "/src/Resources/Images/";
        images = Map.ofEntries(
                Map.entry('b', sketch.loadImage(baseURL + "black_bishop.png")),
                Map.entry('k', sketch.loadImage(baseURL + "black_king.png")),
                Map.entry('n', sketch.loadImage(baseURL + "black_knight.png")),
                Map.entry('p', sketch.loadImage(baseURL + "black_pawn.png")),
                Map.entry('q', sketch.loadImage(baseURL + "black_queen.png")),
                Map.entry('r', sketch.loadImage(baseURL + "black_rook.png")),

                Map.entry('B', sketch.loadImage(baseURL + "white_bishop.png")),
                Map.entry('K', sketch.loadImage(baseURL + "white_king.png")),
                Map.entry('N', sketch.loadImage(baseURL + "white_knight.png")),
                Map.entry('P', sketch.loadImage(baseURL + "white_pawn.png")),
                Map.entry('Q', sketch.loadImage(baseURL + "white_queen.png")),
                Map.entry('R', sketch.loadImage(baseURL + "white_rook.png"))
        );
    }

    /**
     * (Re)Loads the chess board.
     */
    public void load() {
        int lum; // luminosity - highlights legal moves
        sketch.noStroke();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                lum = isLegalMove(row, col) ? 50 : 0;
                if (row == selectedRow && col == selectedCol) {
                    sketch.fill(129, 183, 131); // clicked piece
                } else if (((col%2)+row+1)%2 == 0) {
                    sketch.fill(181 + lum, 136 + lum, 99 + lum); // dark squares
                } else {
                    sketch.fill(240 + lum, 217 + lum, 181 + lum); // light squares
                }
                sketch.rect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                if (Character.isLetter(board[row][col])) {
                    sketch.image(images.get(board[row][col]), col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    /**
     * Selects a piece and calculates all legal moves or deselects a piece.
     *
     * @param row the row, where the piece is
     * @param col the column, where the piece is
     */
    public void selectPiece(int row, int col) {
        char piece = board[row][col];
        if (Character.isLetter(piece) && (row != selectedRow || col != selectedCol)) {
            selectedRow = row;
            selectedCol = col;
            legalMoves = LegalMoveGenerator.generateLegalMoves(board, selectedRow, selectedCol, whiteToMove);
            for (int[] arr : legalMoves) {
                System.out.println(toChessCoordinate(arr));
            }
        } else {
            resetSelection();
        }
    }

    /**
     * Checks if the selected move is a legal move.
     *
     * @param row the row of the selected move
     * @param col the col of the selected move
     * @return true if legal, else false
     */
    public boolean isLegalMove(int row, int col) {
        if (selectedRow == -1 || selectedCol == -1) return false;

        for (int[] move : legalMoves) {
            if (move[0] == row && move[1] == col) return true;
        }
        return false;
    }

    /**
     * Moves a piece.
     *
     * @param row the row to which the piece is moved
     * @param col the col to which the piece is moved
     */
    public void movePiece(int row, int col) {
        board[row][col] = board[selectedRow][selectedCol];
        board[selectedRow][selectedCol] = '\0';

        // Auto-promote to queen
        if ((board[row][col] == 'P' && row == 0) || (board[row][col] == 'p' && row == 7)) {
            board[row][col] = whiteToMove ? 'Q' : 'q';
        }

        printBoard();

        resetSelection();
    }

    /**
     * Resets the selection.
     */
    public void resetSelection() {
        selectedRow = -1;
        selectedCol = -1;
    }

    /**
     * Changes the player.
     */
    public void changePlayer() {
        whiteToMove = !whiteToMove;
    }

    /**
     * Prints the chess board in the console.
     */
    private void printBoard() {
        for (char[] rows : board) {
            for (char piece : rows) {
                System.out.print(piece + " ");
            }
            System.out.println();
        }
    }

    /**
     * Converts a position from the chess board into chess coordinate notation.
     *
     * @param position the position from the chess board
     * @return the chess coordinate notation
     */
    private String toChessCoordinate(int[] position) {
        int row = 8 - position[0];
        char column = (char) ('a' + position[1]);

        return "" + column + row;
    }
}
