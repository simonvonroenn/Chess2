import processing.core.PApplet;

public class Chess2 extends PApplet {

    /** The starting position. */
    public static final String FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    /** Set to true if the user wants to play white; Set to false if the user wants to play black */
    public static final boolean playWhite = true;



    public static final int BOARD_SIZE = 800;
    public static boolean isGameOver = false;
    private final Chessboard board = new Chessboard(this);
    private final ChessBot bot = new ChessBot();

    /**
     * Initializes the board.
     * Runs once at startup.
     */
    public void settings() {
        size(BOARD_SIZE, BOARD_SIZE);
    }

    /**
     * Configures frameRate and loads inital position and piece images.
     * Runs once at startup.
     */
    public void setup() {
        frameRate(60);
        board.loadPosition();
        board.loadImages();
    }

    /**
     * Draws the board continously.
     * Updates the visual.
     */
    public void draw(){
        board.load();
    }

    /**
     * On mouse click event selects a piece, removes piece selection or moves a piece if it is a legal move.
     */
    public void mousePressed() {
        int row = mouseY / Chessboard.TILE_SIZE;
        int col = mouseX / Chessboard.TILE_SIZE;

        if (!isGameOver) {
            if (mouseX < BOARD_SIZE && mouseY < BOARD_SIZE && mouseButton == LEFT) {
                if (board.isLegalMove(row, col)) {
                    board.movePiece(row, col);
                    if (board.changePlayer() != playWhite && !isGameOver) {
                        new Thread(() -> {
                            board.movePieceForBot(bot);
                            board.changePlayer();
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
