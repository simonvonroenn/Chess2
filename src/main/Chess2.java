package main;

import main.engine.Engine;
import main.chessboard.Chessboard;
import main.chessboard.GameOutcome;
import processing.core.PApplet;

public class Chess2 extends PApplet {

    /** The starting position. */
    public static final String FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    //public static final String FEN = "1r3rk1/ppp1nppp/2nq4/4Nb2/2BPpP2/1R2P1P1/P1P4P/2BQR1K1 b - - 0 1";
    /** Set to true if the user wants to play white; Set to false if the user wants to play black */
    public static final boolean playWhite = true;



    public static final int BOARD_SIZE = 800;
    public static final int TILE_SIZE = BOARD_SIZE / 8;
    private final Chessboard chessboard = new Chessboard(this, TILE_SIZE, FEN);
    private final Engine engine = new Engine();
    private volatile GameOutcome outcome = GameOutcome.ONGOING;
    private int xText; // x-coordinate for displaying text
    private int yText; // y-coordinate for displaying text

    /**
     * Initializes the board.
     * Runs once at startup.
     */
    @Override
    public void settings() {
        size(BOARD_SIZE + 300, BOARD_SIZE);
    }

    /**
     * Configures frameRate and loads initial position and piece images.
     * Runs once at startup.
     */
    @Override
    public void setup() {
        frameRate(60);
        chessboard.loadImages();
        chessboard.loadSounds();
        chessboard.loadOpenings();
        // Make first move if it's the engines turn at the beginning
        if (chessboard.board.whiteToMove != playWhite) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // wait 1 second, so that the player can observe the first move
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                chessboard.movePieceForEngine(engine);
            }).start();
        }
    }

    /**
     * Draws the board continuously.
     * Updates the visual.
     */
    @Override
    public void draw(){
        resetXText();
        yText = 50;
        background(200);
        chessboard.load();
        fill(0);
        textSize(24);
        textAlign(LEFT);
        String evaluationText = !outcome.equals(GameOutcome.ONGOING) || chessboard.board.totalHalfMoveCount == 0 ? "" :
                (chessboard.board.evaluation == null ? "book" :
                        String.valueOf((float) chessboard.board.evaluation / 100));
        text("Evaluation: " + evaluationText, xText, yText);
        textSize(18);
        newLine();
        newLine();
        text("Openings left in database: " + Chessboard.openings.size(), xText, yText);
        newLine();
        newLine();
        text("Moves:", xText, yText);
        newLine();
        int yTextInitialValue = yText;
        for (int i = 0; i < chessboard.board.playedMoves.size(); i+=2) {
            if (yText > BOARD_SIZE) {
                yText = yTextInitialValue;
                newColumn();
            }
            if (i+1 == chessboard.board.playedMoves.size()) {
                text(chessboard.board.playedMoves.get(i).toString(), xText, yText);
                break;
            }
            text(chessboard.board.playedMoves.get(i).toString() + " " + chessboard.board.playedMoves.get(i+1).toString() + ",", xText, yText);
            newLine();
        }
        resetXText();
        text(outcome.getMessage(), xText, yText);
    }

    private void newLine() {
        yText += 20;
    }

    private void newColumn() {
        xText += 150;
    }

    private void resetXText() {
        xText = BOARD_SIZE + 20;
    }

    /**
     * On mouse click event selects a piece, removes piece selection or moves a piece if it is a legal move.
     */
    @Override
    public void mousePressed() {
        int row = mouseY / TILE_SIZE;
        int col = mouseX / TILE_SIZE;

        if (outcome.equals(GameOutcome.ONGOING)) {
            if (mouseX < BOARD_SIZE && mouseY < BOARD_SIZE && mouseButton == LEFT) {
                if (chessboard.isLegalMove(row, col)) {
                    outcome = chessboard.movePieceForPlayer(row, col);
                    if (outcome.equals(GameOutcome.ONGOING)) {
                        new Thread(() -> outcome = chessboard.movePieceForEngine(engine)).start();
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
