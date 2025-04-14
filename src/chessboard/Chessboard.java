package chessboard;

import engine.Engine;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

import javax.swing.JOptionPane;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Chessboard {

    private final PApplet sketch;

    /** The size of each tile in pixel. */
    public final int TILE_SIZE;

    public final BoardEnv board;

    private Map<Character, PImage> images;
    private int selectedRow, selectedCol = -1;

    // Castling rights management

    private List<Move> legalMoves;

    /**
     * Integrates Processing in Java.
     */
    public Chessboard(PApplet sketch, int TILE_SIZE, String FEN) {
        this.sketch = sketch;
        this.TILE_SIZE = TILE_SIZE;
        this.board = new BoardEnv(FEN);
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
                if (row == selectedRow && col == selectedCol && (Character.isUpperCase(board.state[selectedRow][selectedCol]) == board.whiteToMove)) {
                    sketch.fill(129, 183, 131); // clicked piece
                } else if (((col%2)+row+1)%2 == 0) {
                    sketch.fill(181 + lum, 136 + lum, 99 + lum); // dark squares
                } else {
                    sketch.fill(240 + lum, 217 + lum, 181 + lum); // light squares
                }
                sketch.rect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                if (Character.isLetter(board.state[row][col])) {
                    sketch.image(images.get(board.state[row][col]), col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    sketch.fill(255, 0, 0);
                    sketch.textSize(15);
                    sketch.textAlign(PConstants.CENTER, PConstants.CENTER);
                    sketch.text(BoardEnv._debug_pieceValues[row][col], col * TILE_SIZE + TILE_SIZE/2,row * TILE_SIZE + TILE_SIZE/2);
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
        char piece = board.state[row][col];
        if (Character.isLetter(piece) && (row != selectedRow || col != selectedCol)) {
            selectedRow = row;
            selectedCol = col;
            legalMoves = LegalMoveGenerator.generateLegalMoves(board, selectedRow, selectedCol);
            for (Move move : legalMoves) {
                System.out.println(move.toString());
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

        for (Move move : legalMoves) {
            if (move.toRow == row && move.toCol == col) return true;
        }
        return false;
    }

    /**
     * Move a piece for the player.
     *
     * @param toRow the row to which the piece is moved
     * @param toCol the col to which the piece is moved
     *
     * @return true if the game is over (win, draw, lose), else false
     */
    public boolean movePieceForPlayer(int toRow, int toCol) {
        Move move = new Move(board.state[selectedRow][selectedCol], selectedRow, selectedCol, toRow, toCol, board.state[toRow][toCol] != '\0');

        boolean isGameOver = movePiece(move);

        Engine.evaluatePosition(board); // for debugging
        printBoard();
        resetSelection();

        return isGameOver;
    }

    /**
     * Move a piece for the engine.
     *
     * @param engine the engine
     * @return true if the game is over (win, draw, lose), else false
     */
    public boolean movePieceForEngine(Engine engine) {
        Engine.BestMove bestMove = engine.calculateBestMove(board);
        board.evaluation = bestMove.evaluation;
        Move move = bestMove.move;

        boolean isGameOver = movePiece(move);

        Engine.evaluatePosition(board); // for debugging
        printBoard();

        return isGameOver;
    }

    /**
     * Moves a piece for either the player or the engine.
     *
     * @param move the move
     *
     * @return true if the game is over (win, draw, lose), else false
     */
    public boolean movePiece(Move move) {
        char capturedPiece = '\0';
        // Check for castling move
        if (Character.toLowerCase(move.piece) == 'k' && Math.abs(move.toCol - move.fromCol) == 2) {
            board.state[move.toRow][move.toCol] = move.piece;
            board.state[move.fromRow][move.fromCol] = '\0';
            if (move.toCol == 6) { // kingside castling
                capturedPiece = board.state[move.toRow][7]; // not really captured, but for rights update
                board.state[move.toRow][5] = (move.piece == 'K' ? 'R' : 'r');
                board.state[move.toRow][7] = '\0';
            } else if (move.toCol == 2) { // queenside castling
                capturedPiece = board.state[move.toRow][0]; // not really captured, but for rights update
                board.state[move.toRow][3] = (move.piece == 'K' ? 'R' : 'r');
                board.state[move.toRow][0] = '\0';
            }
        } else if (Character.toLowerCase(move.piece) == 'p'
                && move.fromCol != move.toCol
                && board.enPassantTarget != null
                && move.toRow == board.enPassantTarget[0]
                && move.toCol == board.enPassantTarget[1]) {
            // En passant capture
            board.state[move.toRow][move.toCol] = move.piece;
            board.state[move.fromRow][move.fromCol] = '\0';
            capturedPiece = (move.piece == 'P' ? board.state[move.toRow + 1][move.toCol] : board.state[move.toRow - 1][move.toCol]);
            if (move.piece == 'P') {
                board.state[move.toRow + 1][move.toCol] = '\0';
            } else {
                board.state[move.toRow - 1][move.toCol] = '\0';
            }
        } else {
            capturedPiece = board.state[move.toRow][move.toCol];
            board.state[move.toRow][move.toCol] = move.piece;
            board.state[move.fromRow][move.fromCol] = '\0';
        }

        // Promotion
        if ((move.piece == 'P' && move.toRow == 0) || (move.piece == 'p' && move.toRow == 7)) {
            char promotedPiece;
            if (move.promotionPiece == '\0') { // Promotion with dialog for player
                String[] options = {"Queen", "Rook", "Bishop", "Knight"};
                int choice = JOptionPane.showOptionDialog(null,
                        "Choose a piece for promotion:",
                        "Piece Promotion",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        options[0]);
                promotedPiece = board.whiteToMove ? 'Q' : 'q';
                if (choice == 1) promotedPiece = board.whiteToMove ? 'R' : 'r';
                else if (choice == 2) promotedPiece = board.whiteToMove ? 'B' : 'b';
                else if (choice == 3) promotedPiece = board.whiteToMove ? 'N' : 'n';
            } else { // Promotion without dialog for engine
                promotedPiece = move.promotionPiece;
            }
            board.state[move.toRow][move.toCol] = promotedPiece;
        }

        return postMoveCalculations(move.toRow, move.toCol, move.piece, capturedPiece);
    }

    /**
     *  Performs all post-move calculations
     * @param row the row to which the piece is moved
     * @param col the col to which the piece is moved
     * @param movedPiece the char of the movedPiece
     * @param capturedPiece the char of the captured piece or '\0' if no piece was captured
     * @return true if game is over (win, draw, lose), else false
     */
    private boolean postMoveCalculations(int row, int col, char movedPiece, char capturedPiece) {
        // Update castling rights and en passant target
        updateRightsAndEnPassant(selectedRow, selectedCol, row, col, capturedPiece);

        // Update half-move clock: reset if a pawn move or capture occurred, otherwise increment.
        if (Character.toLowerCase(movedPiece) == 'p' || capturedPiece != '\0') {
            board.halfMoveClock = 0;
        } else {
            board.halfMoveClock++;
        }

        // Update position repetition count
        String state = getBoardStateSignature();
        board.transpositionTable.put(state, board.transpositionTable.getOrDefault(state, 0) + 1);

        // Check for draw conditions
        if (board.halfMoveClock >= 100) {
            System.out.println("Draw by 50-move rule!");
            return true;
        } else if (board.transpositionTable.get(state) >= 3) {
            System.out.println("Draw by threefold repetition!");
            return true;
        } else if (insufficientMaterial()) {
            System.out.println("Draw by insufficient material!");
            return true;
        }
        // Check for checkmate or stalemate if none of the above conditions apply
        else if (LegalMoveGenerator.isCheckmate(board)) {
            System.out.println((board.whiteToMove ? "Black" : "White") + " wins by checkmate!");
            return true;
        } else if (LegalMoveGenerator.isStalemate(board)) {
            System.out.println("Draw by stalemate!");
            return true;
        }
        return false;
    }



    /**
     * Update castling rights and en passant target based on the move performed.
     *
     * @param fromRow move from row
     * @param fromCol move from col
     * @param toRow move to row
     * @param toCol move to col
     * @param capturedPiece the char of the captured piece or '\0' if no piece was captured
     */
    public void updateRightsAndEnPassant(int fromRow, int fromCol, int toRow, int toCol, char capturedPiece) {
        char movingPiece = board.state[toRow][toCol];
        // For kings: remove castling rights if moved
        if (movingPiece == 'K') {
            board.whiteKingSideCastling = false;
            board.whiteQueenSideCastling = false;
        } else if (movingPiece == 'k') {
            board.blackKingSideCastling = false;
            board.blackQueenSideCastling = false;
        }
        // For rooks: if rook moved, remove corresponding rights
        if (movingPiece == 'R') {
            if (fromRow == 7 && fromCol == 0) {
                board.whiteQueenSideCastling = false;
            }
            if (fromRow == 7 && fromCol == 7) {
                board.whiteKingSideCastling = false;
            }
        } else if (movingPiece == 'r') {
            if (fromRow == 0 && fromCol == 0) {
                board.blackQueenSideCastling = false;
            }
            if (fromRow == 0 && fromCol == 7) {
                board.blackKingSideCastling = false;
            }
        }
        // If a rook is captured from its original square, update castling rights
        if (capturedPiece == 'R') {
            if (toRow == 7 && toCol == 0) {
                board.whiteQueenSideCastling = false;
            }
            if (toRow == 7 && toCol == 7) {
                board.whiteKingSideCastling = false;
            }
        } else if (capturedPiece == 'r') {
            if (toRow == 0 && toCol == 0) {
                board.blackQueenSideCastling = false;
            }
            if (toRow == 0 && toCol == 7) {
                board.blackKingSideCastling = false;
            }
        }
        // En passant: if pawn moved two squares forward having an adjacent enemy pawn, set en passant target, else clear.
        if (Character.toLowerCase(movingPiece) == 'p' && Math.abs(toRow - fromRow) == 2
            && (board.state[toRow][toCol+1] == (Character.isUpperCase(movingPiece) ? 'p' : 'P')
                || board.state[toRow][toCol-1] == (Character.isUpperCase(movingPiece) ? 'p' : 'P'))) {
            int epRow = (fromRow + toRow) / 2;
            board.enPassantTarget = new int[]{epRow, fromCol};
        } else {
            board.enPassantTarget = null;
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
                sb.append(board.state[i][j] == '\0' ? '.' : board.state[i][j]);
            }
        }
        // Append current turn
        sb.append(board.whiteToMove ? "w" : "b");
        // Append castling rights from Chessboard.LegalMoveGenerator
        if (board.whiteKingSideCastling) sb.append("K");
        if (board.whiteQueenSideCastling) sb.append("Q");
        if (board.blackKingSideCastling) sb.append("k");
        if (board.blackQueenSideCastling) sb.append("q");
        // Append en passant target if any
        if (board.enPassantTarget != null) {
            sb.append("ep").append(board.enPassantTarget[0]).append(board.enPassantTarget[1]);
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
                char piece = board.state[row][col];
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
        board.whiteToMove = !board.whiteToMove;
        System.out.println(board.whiteToMove ? "White to move now." : "Black to move now.");
        return board.whiteToMove;
    }

    /**
     * Prints the chess board in the console.
     */
    private void printBoard() {
        for (char[] rows : board.state) {
            for (char piece : rows) {
                System.out.print(piece + " ");
            }
            System.out.println();
        }
    }
}
