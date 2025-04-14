import engine.Engine;
import chessboard.Chessboard;
import processing.core.PApplet;

public class Chess2 extends PApplet {

    /** The starting position. */
    public static final String FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    //public static final String FEN = "r1bq1rk1/ppp2p1p/2n1p2p/3pP3/1b1P4/2NB1N1P/PPP2PP1/R2Q1RK1 w - - 0 1";
    /** Set to true if the user wants to play white; Set to false if the user wants to play black */
    public static final boolean playWhite = true;



    public static final int BOARD_SIZE = 800;
    public static final int TILE_SIZE = BOARD_SIZE / 8;
    public static boolean isGameOver = false;
    private final Chessboard chessboard = new Chessboard(this, TILE_SIZE, FEN);
    private final Engine engine = new Engine();

    /**
     * Initializes the board.
     * Runs once at startup.
     */
    public void settings() {
        size(BOARD_SIZE + 300, BOARD_SIZE);
    }

    /**
     * Configures frameRate and loads inital position and piece images.
     * Runs once at startup.
     */
    public void setup() {
        frameRate(60);
        chessboard.loadImages();
        // Make first move if its the engines turn at the beginning
        if (chessboard.board.whiteToMove != playWhite) {
            new Thread(() -> {
                chessboard.movePieceForEngine(engine);
                chessboard.changePlayer();
            }).start();
        }
    }

    /**
     * Draws the board continously.
     * Updates the visual.
     */
    public void draw(){
        background(200);
        chessboard.load();
        fill(0);
        textSize(24);
        textAlign(LEFT);
        text("Evaluation: " + (float) chessboard.board.evaluation / 100, BOARD_SIZE + 50, 50);

    }

    /**
     * On mouse click event selects a piece, removes piece selection or moves a piece if it is a legal move.
     */
    public void mousePressed() {
        int row = mouseY / TILE_SIZE;
        int col = mouseX / TILE_SIZE;

        if (!isGameOver) {
            if (mouseX < BOARD_SIZE && mouseY < BOARD_SIZE && mouseButton == LEFT) {
                if (chessboard.isLegalMove(row, col)) {
                    isGameOver = chessboard.movePieceForPlayer(row, col);
                    if (!isGameOver && chessboard.changePlayer() != playWhite) {
                        new Thread(() -> {
                            isGameOver = chessboard.movePieceForEngine(engine);
                            if (!isGameOver) chessboard.changePlayer();
                        }).start();
                    }
                } else {
                    chessboard.selectPiece(row, col);
                }
            } else {
                chessboard.resetSelection();
            }
        }
    }

    /**
     * Main method.
     * Integrates Processing in Java.
     */
    public static void main(String[] args){
        String[] processingArgs = {"MySketch"};
        Chess2 chess2 = new Chess2();
        PApplet.runSketch(processingArgs, chess2);
    }
}
