import processing.core.PApplet;

public class Chess2 extends PApplet {

    public static final int BOARD_SIZE = 800;

    private Chessboard board = new Chessboard(this);

    public void settings(){
        size(BOARD_SIZE, BOARD_SIZE);
    }

    public void draw(){
        scale(1, -1);
        translate(0, -height);
        board.load();
    }

    public static void main(String[] args){
        String[] processingArgs = {"MySketch"};
        Chess2 chess2 = new Chess2();
        PApplet.runSketch(processingArgs, chess2);
    }
}
