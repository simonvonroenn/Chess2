import processing.core.PApplet;

import java.util.Arrays;

public class Chessboard {

    private final PApplet sketch;

    private static final int TILE_SIZE = Chess2.BOARD_SIZE / 8;

    private final char[][] board = new char[8][8];

    private String position;

    /**
     * The Chessboard constructor integrates Processing in Java.
     */
    public Chessboard(PApplet sketch, String position) {
        this.sketch = sketch;
        this.position = position;
    }

    /**
     * The loadPosition method loads the starting position at the beginning.
     */
    public void loadPosition() {
        String[] rows = Chess2.FEN.split(" ")[0].split("/");
        for (int i = 0; i < 8; i++) {
            int j = 0;
            while (j < rows[i].length()) {
                if (Character.isDigit(rows[i].charAt(j))) {
                    j += Character.getNumericValue(rows[i].charAt(j));
                } else {
                    board[i][j] = rows[i].charAt(j);
                    j++;
                }
            }
        }
    }

    /**
     * The load method (re)loads the chess board.
     */
    public void load() {
        sketch.noStroke();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (((j%2)+i+1)%2 == 0) {
                    sketch.fill(240, 217, 181);
                } else {
                    sketch.fill(181, 136, 99);
                }
                sketch.rect(i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }
}
