import processing.core.PApplet;

public class Chessboard {

    private PApplet sketch;

    private static final int TILE_SIZE = Chess2.BOARD_SIZE / 8;

    /**
     * The Chessboard constructor integrates Processing in Java.
     */
    public Chessboard(PApplet sketch) {
        this.sketch = sketch;
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
