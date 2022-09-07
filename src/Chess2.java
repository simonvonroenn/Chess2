import processing.core.PApplet;

public class Chess2 extends PApplet {

    /** The starting position. */
    public static final String FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    /** The size of the chess board in pixel. */
    public static final int BOARD_SIZE = 800;

    private Chessboard board = new Chessboard(this);

    /**
     * The settings method runs once at the beginning of execution.
     */
    public void settings(){
        size(BOARD_SIZE, BOARD_SIZE);
        board.loadPosition();
        board.loadImages();
    }

    /**
     * The draw method runs continously.
     */
    public void draw(){
        board.load();
    }

    /**
     * The main method integrates Processing in Java.
     */
    public static void main(String[] args){
        String[] processingArgs = {"MySketch"};
        Chess2 chess2 = new Chess2();
        PApplet.runSketch(processingArgs, chess2);
    }
}
