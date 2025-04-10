import engine.Engine;
import chessboard.Chessboard;
import processing.core.PApplet;

public class Chess2 extends PApplet {

    /** The starting position. */
    public static final String FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    //public static final String FEN = "r1bqk2r/p5pp/2p2n2/3pp3/1b6/2N1P3/PP2BPPP/R1BQK2R w KQkq - 2 11";
    /** Set to true if the user wants to play white; Set to false if the user wants to play black */
    public static final boolean playWhite = true;



    public static final int BOARD_SIZE = 800;
    public static final int TILE_SIZE = BOARD_SIZE / 8;
    public static boolean isGameOver = false;
    private final Chessboard board = new Chessboard(this, TILE_SIZE);
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
        board.loadPosition(FEN);
        board.loadImages();
        // Make first move if its the engines turn at the beginning
        if (Chessboard.whiteToMove != playWhite) {
            new Thread(() -> {
                board.movePieceForEngine(engine);
                board.changePlayer();
            }).start();
        }
    }

    /**
     * Draws the board continously.
     * Updates the visual.
     */
    public void draw(){
        background(200);
        board.load();
        fill(0);
        textSize(24);
        textAlign(LEFT);
        text("Evaluation: " + Chessboard.evaluation / 100, BOARD_SIZE + 50, 50);

    }

    /**
     * On mouse click event selects a piece, removes piece selection or moves a piece if it is a legal move.
     */
    public void mousePressed() {
        int row = mouseY / TILE_SIZE;
        int col = mouseX / TILE_SIZE;

        if (!isGameOver) {
            if (mouseX < BOARD_SIZE && mouseY < BOARD_SIZE && mouseButton == LEFT) {
                if (board.isLegalMove(row, col)) {
                    isGameOver = board.movePieceForPlayer(row, col);
                    if (!isGameOver && board.changePlayer() != playWhite) {
                        new Thread(() -> {
                            isGameOver = board.movePieceForEngine(engine);
                            if (!isGameOver) board.changePlayer();
                        }).start();
                    }
                } else {
                    board.selectPiece(row, col);
                }
            } else {
                board.resetSelection();
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
