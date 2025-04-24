package main;

import main.engine.Engine;
import main.chessboard.Chessboard;
import processing.core.PApplet;

public class Chess2 extends PApplet {

    /** The starting position. */
    public static final String FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    //public static final String FEN = "8/8/8/3k4/8/2n1K3/2b5/8 b - - 0 1";
    /** Set to true if the user wants to play white; Set to false if the user wants to play black */
    public static final boolean playWhite = true;



    public static final int BOARD_SIZE = 800;
    public static final int TILE_SIZE = BOARD_SIZE / 8;
    private final Chessboard chessboard = new Chessboard(this, TILE_SIZE, FEN);
    private final Engine engine = new Engine();
    public boolean isGameOver = false;
    private int xText = BOARD_SIZE + 20; // x-coordinate for displaying text
    private int yText; // y-coordinate for displaying text

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
        chessboard.loadOpenings();
        // Make first move if its the engines turn at the beginning
        if (chessboard.board.whiteToMove != playWhite) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // wait 1 second, so that the player can observe the first move
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
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
        xText = BOARD_SIZE + 20;
        yText = 50;
        background(200);
        chessboard.load();
        fill(0);
        textSize(24);
        textAlign(LEFT);
        String evaluationText = isGameOver || chessboard.board.totalHalfMoveCount == 0 ? "" :
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
    }

    private void newLine() {
        yText += 20;
    }

    private void newColumn() {
        xText += 150;
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
