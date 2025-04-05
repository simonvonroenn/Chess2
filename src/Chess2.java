import engine.Engine;
import chessboard.Chessboard;
import processing.core.PApplet;

public class Chess2 extends PApplet {

    /** The starting position. */
    public static final String FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    //public static final String FEN = "rnbqk2r/pp1pbppp/4pn2/6B1/8/2N2N2/PPP1QPPP/R3KB1R w KQkq - 2 8";

    /** Set to true if the user wants to play white; Set to false if the user wants to play black */
    public static final boolean playWhite = true;



    public static final int BOARD_SIZE = 800;
    public static final int TILE_SIZE = BOARD_SIZE / 8;
    public static boolean isGameOver = false;
    private final Chessboard board = new Chessboard(this, TILE_SIZE, FEN);
    private final Engine bot = new Engine();

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
        board.loadPosition();
        board.loadImages();
        // Make first move if the bot plays white
        if (!playWhite) {
            new Thread(() -> {
                board.movePieceForBot(bot);
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
                    isGameOver = board.movePiece(row, col);
                    if (!isGameOver && board.changePlayer() != playWhite) {
                        new Thread(() -> {
                            isGameOver = board.movePieceForBot(bot);
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
