import processing.core.PApplet;
import processing.core.PImage;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Chessboard {

    private final PApplet sketch;

    /** The size of each tile in pixel. */
    public static final int TILE_SIZE = Chess2.BOARD_SIZE / 8;

    private final char[][] board = new char[8][8];

    private Map<Character, PImage> images;
    private int selectedRow, selectedCol = -1;
    private boolean whiteToMove = true;
    private List<int[]> legalMoves;

    private int halfMoveClock = 0; // Counts half moves since last pawn move or capture
    private Map<String, Integer> positionCount = new java.util.HashMap<>();

    /**
     * Integrates Processing in Java.
     */
    public Chessboard(PApplet sketch) {
        this.sketch = sketch;
    }

    /**
     *Loads the starting position at the beginning.
     */
    public void loadPosition() {
        String[] rows = Chess2.FEN.split(" ")[0].split("/");
        for (int i = 0; i < 8; i++) {
            int offset = 0;
            for (int j = 0; j < rows[i].length(); j++) {
                if (Character.isDigit(rows[i].charAt(j))) {
                    offset += Character.getNumericValue(rows[i].charAt(j)) - 1;
                } else {
                    board[i][j + offset] = rows[i].charAt(j);
                }
            }
        }
    }

    /**
     * Loads the images of the pieces.
     */
    public void loadImages() {
        String baseURL = "/src/Resources/Images/";
        images = Map.ofEntries(
                Map.entry('b', sketch.loadImage(baseURL + "black_bishop.png")),
                Map.entry('k', sketch.loadImage(baseURL + "black_king.png")),
                Map.entry('n', sketch.loadImage(baseURL + "black_knight.png")),
                Map.entry('p', sketch.loadImage(baseURL + "black_pawn.png")),
                Map.entry('q', sketch.loadImage(baseURL + "black_queen.png")),
                Map.entry('r', sketch.loadImage(baseURL + "black_rook.png")),

                Map.entry('B', sketch.loadImage(baseURL + "white_bishop.png")),
                Map.entry('K', sketch.loadImage(baseURL + "white_king.png")),
                Map.entry('N', sketch.loadImage(baseURL + "white_knight.png")),
                Map.entry('P', sketch.loadImage(baseURL + "white_pawn.png")),
                Map.entry('Q', sketch.loadImage(baseURL + "white_queen.png")),
                Map.entry('R', sketch.loadImage(baseURL + "white_rook.png"))
        );
    }

    /**
     * (Re)Loads the chess board.
     */
    public void load() {
        int lum; // luminosity - highlights legal moves
        sketch.noStroke();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                lum = isLegalMove(row, col) ? 50 : 0;
                if (row == selectedRow && col == selectedCol && (Character.isUpperCase(board[selectedRow][selectedCol]) == whiteToMove)) {
                    sketch.fill(129, 183, 131); // clicked piece
                } else if (((col%2)+row+1)%2 == 0) {
                    sketch.fill(181 + lum, 136 + lum, 99 + lum); // dark squares
                } else {
                    sketch.fill(240 + lum, 217 + lum, 181 + lum); // light squares
                }
                sketch.rect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                if (Character.isLetter(board[row][col])) {
                    sketch.image(images.get(board[row][col]), col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    /**
     * Selects a piece and calculates all legal moves or deselects a piece.
     *
     * @param row the row, where the piece is
     * @param col the column, where the piece is
     */
    public void selectPiece(int row, int col) {
        char piece = board[row][col];
        if (Character.isLetter(piece) && (row != selectedRow || col != selectedCol)) {
            selectedRow = row;
            selectedCol = col;
            legalMoves = LegalMoveGenerator.generateLegalMoves(board, selectedRow, selectedCol, whiteToMove);
            for (int[] arr : legalMoves) {
                System.out.println(toChessCoordinate(arr));
            }
        } else {
            resetSelection();
        }
    }

    /**
     * Checks if the selected move is a legal move.
     *
     * @param row the row of the selected move
     * @param col the col of the selected move
     * @return true if legal, else false
     */
    public boolean isLegalMove(int row, int col) {
        if (selectedRow == -1 || selectedCol == -1) return false;

        for (int[] move : legalMoves) {
            if (move[0] == row && move[1] == col) return true;
        }
        return false;
    }

    /**
     * Moves a piece.
     *
     * @param row the row to which the piece is moved
     * @param col the col to which the piece is moved
     */
    public void movePiece(int row, int col) {
        char movingPiece = board[selectedRow][selectedCol];
        char capturedPiece = '\0';
        // Check for castling move
        if (Character.toLowerCase(movingPiece) == 'k' && Math.abs(col - selectedCol) == 2) {
            board[row][col] = movingPiece;
            board[selectedRow][selectedCol] = '\0';
            if (col == 6) { // kingside castling
                capturedPiece = board[row][7]; // not really captured, but for rights update
                board[row][5] = (movingPiece == 'K' ? 'R' : 'r');
                board[row][7] = '\0';
            } else if (col == 2) { // queenside castling
                capturedPiece = board[row][0];
                board[row][3] = (movingPiece == 'K' ? 'R' : 'r');
                board[row][0] = '\0';
            }
        } else if (Character.toLowerCase(movingPiece) == 'p' && selectedCol != col && board[row][col] == '\0') {
            // En passant capture
            board[row][col] = movingPiece;
            board[selectedRow][selectedCol] = '\0';
            capturedPiece = (movingPiece == 'P' ? board[row + 1][col] : board[row - 1][col]);
            if (movingPiece == 'P') {
                board[row + 1][col] = '\0';
            } else {
                board[row - 1][col] = '\0';
            }
        } else {
            capturedPiece = board[row][col];
            board[row][col] = movingPiece;
            board[selectedRow][selectedCol] = '\0';
        }

        // Promotion with dialog
        if ((movingPiece == 'P' && row == 0) || (movingPiece == 'p' && row == 7)) {
            String[] options = {"Queen", "Rook", "Bishop", "Knight"};
            int choice = JOptionPane.showOptionDialog(null,
                    "Choose a piece for promotion:",
                    "Piece Promotion",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]);
            char promotedPiece = 'Q';
            if (choice == 1) promotedPiece = 'R';
            else if (choice == 2) promotedPiece = 'B';
            else if (choice == 3) promotedPiece = 'N';
            board[row][col] = whiteToMove ? promotedPiece : Character.toLowerCase(promotedPiece);
        }

        postMoveCalculations(row, col, movingPiece, capturedPiece);

        printBoard();

        resetSelection();
    }

    public void movePieceForBot(ChessBot bot) {
        Move move = bot.calculateBestMove(board, whiteToMove);
        char movingPiece = board[move.fromRow][move.fromCol];
        char capturedPiece = board[move.toRow][move.toCol];
        board[move.toRow][move.toCol] = board[move.fromRow][move.fromCol];
        board[move.fromRow][move.fromCol] = '\0';

        postMoveCalculations(move.toRow, move.toCol, movingPiece, capturedPiece);

        printBoard();
    }

    /**
     *  Performs all post-move calculations
     * @param row the row to which the piece is moved
     * @param col the col to which the piece is moved
     * @param movedPiece the char of the movedPiece
     * @param capturedPiece the char of the captured piece or '\0' if no piece was captured
     */
    private void postMoveCalculations(int row, int col, char movedPiece, char capturedPiece) {
        // Update castling rights and en passant target
        LegalMoveGenerator.updateRightsAndEnPassant(board, selectedRow, selectedCol, row, col, capturedPiece);

        // Update half-move clock: reset if a pawn move or capture occurred, otherwise increment.
        if (Character.toLowerCase(movedPiece) == 'p' || capturedPiece != '\0') {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }

        // Update position repetition count
        String state = getBoardStateSignature();
        positionCount.put(state, positionCount.getOrDefault(state, 0) + 1);

        // Check for draw conditions
        if (halfMoveClock >= 100) {
            Chess2.isGameOver = true;
            System.out.println("Draw by 50-move rule!");
        } else if (positionCount.get(state) >= 3) {
            Chess2.isGameOver = true;
            System.out.println("Draw by threefold repetition!");
        } else if (insufficientMaterial()) {
            Chess2.isGameOver = true;
            System.out.println("Draw by insufficient material!");
        }
        // Check for checkmate or stalemate if none of the above conditions apply
        else if (LegalMoveGenerator.isCheckmate(board, !whiteToMove)) {
            Chess2.isGameOver = true;
            System.out.println((whiteToMove ? "Black" : "White") + " wins by checkmate!");
        } else if (LegalMoveGenerator.isStalemate(board, !whiteToMove)) {
            Chess2.isGameOver = true;
            System.out.println("Draw by stalemate!");
        }
    }

    /**
     * Generates a signature string representing the current board state,
     * including piece positions, turn, castling rights, and en passant target.
     */
    private String getBoardStateSignature() {
        StringBuilder sb = new StringBuilder();
        // Append board configuration
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                sb.append(board[i][j] == '\0' ? '.' : board[i][j]);
            }
        }
        // Append current turn
        sb.append(whiteToMove ? "w" : "b");
        // Append castling rights from LegalMoveGenerator
        if (LegalMoveGenerator.whiteKingSideCastling) sb.append("K");
        if (LegalMoveGenerator.whiteQueenSideCastling) sb.append("Q");
        if (LegalMoveGenerator.blackKingSideCastling) sb.append("k");
        if (LegalMoveGenerator.blackQueenSideCastling) sb.append("q");
        // Append en passant target if any
        if (LegalMoveGenerator.enPassantTarget != null) {
            sb.append("ep").append(LegalMoveGenerator.enPassantTarget[0]).append(LegalMoveGenerator.enPassantTarget[1]);
        }
        return sb.toString();
    }

    /**
     * Checks for insufficient material on the board.
     *
     * @return true if neither side has sufficient material to force checkmate.
     */
    private boolean insufficientMaterial() {
        int whiteKnights = 0, whiteBishops = 0;
        int blackKnights = 0, blackBishops = 0;

        boolean hasWhiteLightBishop = false, hasWhiteDarkBishop = false;
        boolean hasBlackLightBishop = false, hasBlackDarkBishop = false;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board[row][col];
                switch (piece) {
                    case 'P', 'R', 'Q', 'p', 'r', 'q' -> {
                        return false;
                    }
                    case 'N' -> whiteKnights++;
                    case 'B' -> {
                        whiteBishops++;
                        if ((row + col) % 2 == 0) hasWhiteLightBishop = true;
                        else hasWhiteDarkBishop = true;
                    }
                    case 'n' -> blackKnights++;
                    case 'b' -> {
                        blackBishops++;
                        if ((row + col) % 2 == 0) hasBlackLightBishop = true;
                        else hasBlackDarkBishop = true;
                    }
                }
            }
        }

        // King vs. king
        if (whiteKnights + whiteBishops == 0 && blackKnights + blackBishops == 0) return true;

        // King with single minor piece vs. king
        if ((whiteKnights + whiteBishops == 1 && blackKnights + blackBishops == 0) ||
                (whiteKnights + whiteBishops == 0 && blackKnights + blackBishops == 1)) return true;

        // King and bishop vs. king and bishop with both bishops on same color
        if (whiteKnights == 0 && blackKnights == 0 && whiteBishops == 1 && blackBishops == 1) {
            if ((hasWhiteLightBishop && hasBlackLightBishop) || (hasWhiteDarkBishop && hasBlackDarkBishop)) {
                return true;
            }
        }

        // King and two knights vs. king
        return (whiteKnights == 2 && whiteBishops == 0 && blackKnights + blackBishops == 0) ||
                (blackKnights == 2 && blackBishops == 0 && whiteKnights + whiteBishops == 0);
    }

    /**
     * Resets the selection.
     */
    public void resetSelection() {
        selectedRow = -1;
        selectedCol = -1;
    }

    /**
     * Changes the player.
     *
     * @return true if now white is to move
     */
    public boolean changePlayer() {
        whiteToMove = !whiteToMove;
        if (!Chess2.isGameOver) System.out.println(whiteToMove ? "White to move now." : "Black to move now.");
        return whiteToMove;
    }

    /**
     * Prints the chess board in the console.
     */
    private void printBoard() {
        for (char[] rows : board) {
            for (char piece : rows) {
                System.out.print(piece + " ");
            }
            System.out.println();
        }
    }

    /**
     * Converts a position from the chess board into chess coordinate notation.
     *
     * @param position the position from the chess board
     * @return the chess coordinate notation
     */
    private String toChessCoordinate(int[] position) {
        int row = 8 - position[0];
        char column = (char) ('a' + position[1]);

        return "" + column + row;
    }
}
