package main;

import main.chessboard.Move;
import main.engine.Engine;
import main.chessboard.Chessboard;
import main.chessboard.GameOutcome;
import processing.core.PApplet;

import java.util.List;

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
                chessboard.movePieceForEngine(engine);
                chessboard.computeAllLegalMoves();
            }).start();
        } else {
            new Thread(chessboard::computeAllLegalMoves).start();
        }
    }

    /**
     * Draws the board continuously.
     * Updates the visual.
     */
    // In Chess2.java

    @Override
    public void draw() {
        resetXText();
        yText = 50;
        background(200);
        chessboard.load();
        fill(0);
        textSize(24);
        textAlign(LEFT);

        if (!outcome.equals(GameOutcome.ONGOING)) {
            drawWrappedText(outcome.getMessage(), xText, yText, 290);
        } else {
            String evaluationText = chessboard.board.totalHalfMoveCount == 0 ? "" :
                    (chessboard.board.evaluation == null ? "book" :
                            String.valueOf((float) chessboard.board.evaluation / 100));
            text("Evaluation: " + evaluationText, xText, yText);
        }

        textSize(18);
        newLine();
        newLine();
        text("Openings left in database: " + Chessboard.openings.size(), xText, yText);
        newLine();
        newLine();
        text("Moves:", xText, yText);
        newLine();
        int yTextInitialValue = yText;
        List<Move> playedMoves = chessboard.board.playedMoves;
        for (int i = 0; i < playedMoves.size(); i += 2) {
            if (yText > BOARD_SIZE - 60) { // leave room for button
                yText = yTextInitialValue;
                newColumn();
            }
            if (i + 1 == playedMoves.size()) {
                text(playedMoves.get(i).toString(), xText, yText);
                break;
            }
            text(playedMoves.get(i).toString() + " " + playedMoves.get(i + 1).toString() + ",", xText, yText);
            newLine();
        }

        // PGN copy button
        drawPgnButton();
    }

    /**
     * Draws the "Copy PGN" button in the bottom-right of the sidebar.
     */
    private void drawPgnButton() {
        int btnX = BOARD_SIZE + 20;
        int btnY = BOARD_SIZE - 45;
        int btnW = 260;
        int btnH = 35;

        boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW
                && mouseY >= btnY && mouseY <= btnY + btnH;

        fill(hovered ? color(80, 80, 80) : color(50, 50, 50));
        rect(btnX, btnY, btnW, btnH, 6);

        fill(255);
        textSize(15);
        textAlign(CENTER, CENTER);
        text("Copy PGN to clipboard", btnX + btnW / 2f, btnY + btnH / 2f);
        textAlign(LEFT);
    }

    /**
     * Converts the played moves to PGN (Portable Game Notation) format and
     * copies the result to the system clipboard.
     *
     * <p>PGN format:
     * <pre>
     * [Event "?"]
     * [Site "?"]
     * [Date "2024.01.01"]
     * [White "?"]
     * [Black "Engine"]
     * [Result "*"]
     *
     * 1. e4 e5 2. Nf3 Nc6 *
     * </pre>
     *
     * @param moves   the list of played moves in order
     * @param outcome the game outcome used to determine the PGN result tag
     * @return the full PGN string
     */
    private String movesToPGN(List<Move> moves, GameOutcome outcome) {
        String result = switch (outcome) {
            case CHECKMATE_BLACK -> "1-0";
            case CHECKMATE_WHITE -> "0-1";
            case STALEMATE, FIFTY_MOVE_RULE, THREE_FOLD_REPETITION, INSUFFICIENT_MATERIAL -> "1/2-1/2";
            default -> "*";
        };

        String date = new java.text.SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date());

        StringBuilder pgn = new StringBuilder();
        pgn.append("[Date \"").append(date).append("\"]\n");
        pgn.append("[White \"").append(playWhite ? "Player" : "Engine").append("\"]\n");
        pgn.append("[Black \"").append(playWhite ? "Engine" : "Player").append("\"]\n");
        pgn.append("[Result \"").append(result).append("\"]\n\n");

        for (int i = 0; i < moves.size(); i++) {
            if (i % 2 == 0) {
                pgn.append(i / 2 + 1).append(". ");
            }
            pgn.append(moves.get(i).toString());
            if (i < moves.size() - 1) {
                pgn.append(" ");
            }
        }
        pgn.append(" ").append(result);

        return pgn.toString();
    }

    /**
     * Copies the given string to the system clipboard.
     *
     * @param content the string to copy
     */
    private void copyToClipboard(String content) {
        java.awt.datatransfer.StringSelection selection =
                new java.awt.datatransfer.StringSelection(content);
        java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(selection, selection);
    }

    @Override
    public void mousePressed() {
        int row = mouseY / TILE_SIZE;
        int col = mouseX / TILE_SIZE;

        // PGN button click
        int btnX = BOARD_SIZE + 20;
        int btnY = BOARD_SIZE - 45;
        int btnW = 260;
        int btnH = 35;
        if (mouseX >= btnX && mouseX <= btnX + btnW
                && mouseY >= btnY && mouseY <= btnY + btnH) {
            String pgn = movesToPGN(chessboard.board.playedMoves, outcome);
            copyToClipboard(pgn);
            System.out.println("PGN copied to clipboard:\n" + pgn);
            return;
        }

        if (outcome.equals(GameOutcome.ONGOING)) {
            if (mouseX < BOARD_SIZE && mouseY < BOARD_SIZE && mouseButton == LEFT) {
                if (chessboard.isLegalMove(row, col)) {
                    outcome = chessboard.movePieceForPlayer(row, col);
                    if (outcome.equals(GameOutcome.ONGOING)) {
                        new Thread(() -> {
                            outcome = chessboard.movePieceForEngine(engine);
                            chessboard.computeAllLegalMoves(); // Precompute legal moves for the player's next turn
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

    private void drawWrappedText(String str, int x, int y, int maxWidth) {
        String[] words = str.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (textWidth(candidate) > maxWidth) {
                text(line.toString(), x, y);
                y += 28;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) {
            text(line.toString(), x, y);
            yText = y;
        }
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
     * Main method.
     * Integrates Processing in Java.
     */
    public static void main(String[] args){
        String[] processingArgs = {"MySketch"};
        Chess2 chess2 = new Chess2();
        PApplet.runSketch(processingArgs, chess2);
    }
}
