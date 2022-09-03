import processing.core.PApplet;

public class Chess2 extends PApplet {

    /** The size of the chess board in pixel */
    public static final int BOARD_SIZE = 800;

    private Chessboard board = new Chessboard(this);

    /**
     * The settings method runs once at the beginning of execution.
     */
    public void settings(){
        size(BOARD_SIZE, BOARD_SIZE);
    }

    /**
     * The draw method runs continously.
     */
    public void draw(){
        scale(1, -1);
        translate(0, -height);
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
