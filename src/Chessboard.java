import processing.core.PApplet;
import processing.core.PImage;

import java.util.Map;

public class Chessboard {

    private final PApplet sketch;

    /** The size of each tile in pixel. */
    public static final int TILE_SIZE = Chess2.BOARD_SIZE / 8;

    private final char[][] board = new char[8][8];

    private Map<Character, PImage> images;
    private int pieceClicked = -1;

    /**
     * The Chessboard constructor integrates Processing in Java.
     */
    public Chessboard(PApplet sketch) {
        this.sketch = sketch;
    }

    /**
     * The loadPosition method loads the starting position at the beginning.
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
     * The loadImages() method loads the images of the pieces.
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
     * The load method (re)loads the chess board.
     */
    public void load() {
        sketch.noStroke();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i * 8 + j == pieceClicked) {
                    sketch.fill(129, 183, 131); // clicked piece
                } else if (((j%2)+i+1)%2 == 0) {
                    sketch.fill(181, 136, 99); // dark squares
                } else {
                    sketch.fill(240, 217, 181); // light squares
                }
                sketch.rect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                if (Character.isLetter(board[i][j])) {
                    sketch.image(images.get(board[i][j]), j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    /**
     * The calcLegalMoves method calculates for an individual piece, which legal moves it has.
     *
     * @param row the row, where the piece is
     * @param col the column, where the piece is
     */
    public void calcLegalMoves(int row, int col) {
        char piece = board[row][col];
        if (Character.isLetter(piece) && row * 8 + col != pieceClicked) {
            pieceClicked = row * 8 + col;
        } else {
            resetPieceClicked();
        }
    }

    public void resetPieceClicked() {
        this.pieceClicked = -1;
    }
}
