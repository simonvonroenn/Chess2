package main.chessboard;

import main.engine.Engine;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.sound.*;

import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Chessboard {

    private final PApplet sketch;

    /** The size of each tile in pixel. */
    public final int TILE_SIZE;

    public final BoardEnv board;

    private Map<Character, PImage> images;
    private Map<String, SoundFile> sounds;
    public static List<List<String>> openings = new ArrayList<>();

    private int selectedRow, selectedCol = -1;

    private List<Move> legalPlayerMovesForSelectedPiece;
    private List<Move> allLegalPlayerMoves = new ArrayList<>();

    private static long debugStartTime = 0;

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
        String baseFilePath = "src/main/resources/images/";
        images = Map.ofEntries(
                Map.entry('b', sketch.loadImage(baseFilePath + "black_bishop.png")),
                Map.entry('k', sketch.loadImage(baseFilePath + "black_king.png")),
                Map.entry('n', sketch.loadImage(baseFilePath + "black_knight.png")),
                Map.entry('p', sketch.loadImage(baseFilePath + "black_pawn.png")),
                Map.entry('q', sketch.loadImage(baseFilePath + "black_queen.png")),
                Map.entry('r', sketch.loadImage(baseFilePath + "black_rook.png")),

                Map.entry('B', sketch.loadImage(baseFilePath + "white_bishop.png")),
                Map.entry('K', sketch.loadImage(baseFilePath + "white_king.png")),
                Map.entry('N', sketch.loadImage(baseFilePath + "white_knight.png")),
                Map.entry('P', sketch.loadImage(baseFilePath + "white_pawn.png")),
                Map.entry('Q', sketch.loadImage(baseFilePath + "white_queen.png")),
                Map.entry('R', sketch.loadImage(baseFilePath + "white_rook.png"))
        );
    }

    /**
     * Loads the sounds of the game.
     */
    public void loadSounds() {
        String baseFilePath = "src/main/resources/sounds/";
        sounds = Map.ofEntries(
                Map.entry("move", new SoundFile(sketch, baseFilePath + "move.wav")),
                Map.entry("capture", new SoundFile(sketch, baseFilePath + "capture.wav"))
        );
    }

    /**
     * Loads the opening database.
     */
    public void loadOpenings() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/openings/openings_db_3624.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Es wird angenommen, dass die Zeilen bereits ohne Häufigkeitsangabe vorliegen
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    List<String> moves = Arrays.asList(trimmed.split(" "));
                    openings.add(moves);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
     * Precomputes all legal moves for the current player.
     * Should be called once at the start of each player turn.
     */
    public void computeAllLegalMoves() {
        allLegalPlayerMoves = Engine.generateAllLegalMoves(board);
    }

    /**
     * Selects a piece and filters legal moves for it from the precomputed list.
     *
     * @param row the row, where the piece is
     * @param col the column, where the piece is
     */
    public void selectPiece(int row, int col) {
        char piece = board.state[row][col];
        if (Character.isLetter(piece) && board.whiteToMove == Character.isUpperCase(piece) && (row != selectedRow || col != selectedCol)) {
            selectedRow = row;
            selectedCol = col;
            // Filter from precomputed list instead of generating anew
            legalPlayerMovesForSelectedPiece = allLegalPlayerMoves.stream()
                    .filter(m -> m.fromRow == row && m.fromCol == col)
                    .toList();
            for (Move move : legalPlayerMovesForSelectedPiece) {
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

        for (Move move : legalPlayerMovesForSelectedPiece) {
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
     * @return the game outcome
     */
    public GameOutcome movePieceForPlayer(int toRow, int toCol) {
        Move move = allLegalPlayerMoves.stream()
                .filter(m -> m.fromRow == selectedRow && m.fromCol == selectedCol
                        && m.toRow == toRow && m.toCol == toCol)
                .findFirst()
                .orElse(null);

        MakeMoveResult result = makeMove(board, move, false);

        playSounds(move);
        resetSelection();
        allLegalPlayerMoves = new ArrayList<>();

        Engine.evaluatePosition(board); // for debugging
        printBoard();

        return result.outcome;
    }

    /**
     * Move a piece for the engine.
     *
     * @param engine the engine
     * @return the game outcome
     */
    public GameOutcome movePieceForEngine(Engine engine) {
        Engine.BestMove bestMove = engine.calculateBestMove(board.deepCopy());
        board.evaluation = bestMove.evaluation;
        Move move = bestMove.move;

        MakeMoveResult result = makeMove(board, move, false);

        playSounds(move);

        Engine.evaluatePosition(board); // for debugging
        printBoard();

        return result.outcome;
    }

    /**
     * Moves a piece for either the player or the engine.
     *
     * @param board the board state
     * @param move the move
     * @param skipPostMoveCalculations for skipping post move calculations (when determining checkmate or stalemate)
     *
     * @return the undo info for undoing the move and the game outcome
     */
    public static MakeMoveResult makeMove(BoardEnv board, Move move, boolean skipPostMoveCalculations) {
        debugStartTime = System.currentTimeMillis();

        // Remove old rights from zobrist hash
        updateZobristHashRights(board);

        char capturedPiece = '\0';
        UndoInfo undoInfo = new UndoInfo(board, '\0');
        undoInfo.didPostMoveCalculations = !skipPostMoveCalculations;
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
            undoInfo.wasEnPassant = true;
            board.state[move.toRow][move.toCol] = move.piece;
            board.state[move.fromRow][move.fromCol] = '\0';

            int capturedRow = (move.piece == 'P') ? move.toRow + 1 : move.toRow - 1;
            capturedPiece = board.state[capturedRow][move.toCol];
            undoInfo.capturedPawnPos = new int[]{capturedRow, move.toCol};
            board.state[capturedRow][move.toCol] = '\0';
        } else {
            capturedPiece = board.state[move.toRow][move.toCol];
            board.state[move.toRow][move.toCol] = move.piece;
            board.state[move.fromRow][move.fromCol] = '\0';
        }

        undoInfo.capturedPiece = capturedPiece;

        // Promotion
        if ((move.piece == 'P' && move.toRow == 0) || (move.piece == 'p' && move.toRow == 7)) {
            undoInfo.wasPromotion = true;
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
            } else { // Promotion without dialog for main.engine
                promotedPiece = move.promotionPiece;
            }
            board.state[move.toRow][move.toCol] = promotedPiece;
        }

        // Update king positions
        if (move.piece == 'K') {
            board.whiteKingPos = new int[]{move.toRow, move.toCol};
        } else if (move.piece == 'k') {
            board.blackKingPos = new int[]{move.toRow, move.toCol};
        }

        updateZobristHash(board, move, undoInfo);

        // Change player
        board.whiteToMove = !board.whiteToMove;

        if (skipPostMoveCalculations) {
            return new MakeMoveResult(GameOutcome.ONGOING, undoInfo);
        }

        GameOutcome outcome = postMoveCalculations(board, move, capturedPiece);

        undoInfo.postMoveZobristHash = board.zobristHash;

        return new MakeMoveResult(outcome, undoInfo);
    }

    /**
     *  Performs all post-move calculations
     *
     * @param move the move that has been played
     * @param capturedPiece the char of the captured piece or '\0' if no piece was captured
     * @return the game outcome
     */
    private static GameOutcome postMoveCalculations(BoardEnv board, Move move, char capturedPiece) {
        // Add played move and update half move count
        board.playedMoves.add(move);
        board.totalHalfMoveCount++;

        // Update castling rights and en passant target
        updateRightsAndEnPassant(board, move, capturedPiece);

        // Update half-move clock: reset if a pawn move or capture occurred, otherwise increment.
        if (Character.toLowerCase(move.piece) == 'p' || capturedPiece != '\0') {
            board.halfMoveClock = 0;
        } else {
            board.halfMoveClock++;
        }

        // Update position repetition count
        board.transpositionTable.merge(board.zobristHash, 1, Integer::sum);

        Engine._debugTime_ApplyMove += System.currentTimeMillis() - debugStartTime;

        // Check for draw conditions
        if (board.halfMoveClock >= 100) {
            return GameOutcome.FIFTY_MOVE_RULE;
        }
        if (board.transpositionTable.get(board.zobristHash) >= 3) {
            return GameOutcome.THREE_FOLD_REPETITION;
        }
        if (insufficientMaterial(board)) {
            return GameOutcome.INSUFFICIENT_MATERIAL;
        }

        // Check for checkmate or stalemate
        return LegalMoveGenerator.determineCheckmateOrStalemate(board);
    }

    /**
     * Update castling rights and en passant target based on the move performed.
     *
     * @param move the move that has been played
     * @param capturedPiece the char of the captured piece or '\0' if no piece was captured
     */
    public static void updateRightsAndEnPassant(BoardEnv board, Move move, char capturedPiece) {
        char movingPiece = board.state[move.toRow][move.toCol];
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
            if (move.fromRow == 7 && move.fromCol == 0) {
                board.whiteQueenSideCastling = false;
            }
            if (move.fromRow == 7 && move.fromCol == 7) {
                board.whiteKingSideCastling = false;
            }
        } else if (movingPiece == 'r') {
            if (move.fromRow == 0 && move.fromCol == 0) {
                board.blackQueenSideCastling = false;
            }
            if (move.fromRow == 0 && move.fromCol == 7) {
                board.blackKingSideCastling = false;
            }
        }
        // If a rook is captured from its original square, update castling rights
        if (capturedPiece == 'R') {
            if (move.toRow == 7 && move.toCol == 0) {
                board.whiteQueenSideCastling = false;
            }
            if (move.toRow == 7 && move.toCol == 7) {
                board.whiteKingSideCastling = false;
            }
        } else if (capturedPiece == 'r') {
            if (move.toRow == 0 && move.toCol == 0) {
                board.blackQueenSideCastling = false;
            }
            if (move.toRow == 0 && move.toCol == 7) {
                board.blackKingSideCastling = false;
            }
        }
        // En passant: if pawn moved two squares forward having an adjacent enemy pawn, set en passant target, else clear.
        if (Character.toLowerCase(movingPiece) == 'p' && Math.abs(move.toRow - move.fromRow) == 2
            && (move.toCol < 7 && board.state[move.toRow][move.toCol+1] == (Character.isUpperCase(movingPiece) ? 'p' : 'P')
                || move.toCol > 0 && board.state[move.toRow][move.toCol-1] == (Character.isUpperCase(movingPiece) ? 'p' : 'P'))) {
            int epRow = (move.fromRow + move.toRow) / 2;
            board.enPassantTarget = new int[]{epRow, move.fromCol};
        } else {
            board.enPassantTarget = null;
        }

        // Insert new rights into zobrist hash
        updateZobristHashRights(board);
    }

    /**
     * Updates the Zobrist hash incrementally for a given move.
     * Must be called after castling rights and en passant target have been
     * removed from the hash, but before the new rights are added.
     *
     * <p>Handles all special cases:
     * <ul>
     *   <li>Normal moves — removes piece from origin, places it on destination</li>
     *   <li>Captures — removes the captured piece from the hash</li>
     *   <li>En passant — removes the captured pawn from its actual square</li>
     *   <li>Promotion — places the promoted piece instead of the pawn</li>
     *   <li>Castling — additionally moves the rook</li>
     *   <li>Side to move — always toggled</li>
     * </ul>
     *
     * @param board      the board state whose {@code zobristHash} is updated in-place
     * @param move       the move being made
     * @param undoInfo   the undo info containing {@code wasEnPassant} and
     *                   {@code capturedPawnPos} for en passant detection,
     *                   and {@code capturedPiece} for capture detection
     */
    private static void updateZobristHash(BoardEnv board, Move move, UndoInfo undoInfo) {
        // Remove moving piece from origin square
        board.zobristHash ^= ZobristTable.PIECE_SQUARE[ZobristTable.pieceIndex(move.piece)][move.fromRow * 8 + move.fromCol];

        // Remove captured piece
        if (undoInfo.capturedPiece != '\0') {
            int capturedSq = undoInfo.wasEnPassant
                    ? undoInfo.capturedPawnPos[0] * 8 + undoInfo.capturedPawnPos[1]
                    : move.toRow * 8 + move.toCol;
            board.zobristHash ^= ZobristTable.PIECE_SQUARE[ZobristTable.pieceIndex(undoInfo.capturedPiece)][capturedSq];
        }

        // Place piece on destination (promotion: use promoted piece instead of pawn)
        char placedPiece = (move.promotionPiece != '\0') ? move.promotionPiece : move.piece;
        board.zobristHash ^= ZobristTable.PIECE_SQUARE[ZobristTable.pieceIndex(placedPiece)][move.toRow * 8 + move.toCol];

        // Castling: additionally move the rook
        if (Character.toLowerCase(move.piece) == 'k' && Math.abs(move.toCol - move.fromCol) == 2) {
            boolean kingside = move.toCol == 6;
            int rookFromCol = kingside ? 7 : 0;
            int rookToCol   = kingside ? 5 : 3;
            char rook = Character.isUpperCase(move.piece) ? 'R' : 'r';
            board.zobristHash ^= ZobristTable.PIECE_SQUARE[ZobristTable.pieceIndex(rook)][move.toRow * 8 + rookFromCol];
            board.zobristHash ^= ZobristTable.PIECE_SQUARE[ZobristTable.pieceIndex(rook)][move.toRow * 8 + rookToCol];
        }

        // Toggle side to move
        board.zobristHash ^= ZobristTable.SIDE_TO_MOVE;
    }

    private static void updateZobristHashRights(BoardEnv board) {
        if (board.whiteKingSideCastling)  board.zobristHash ^= ZobristTable.CASTLING[0];
        if (board.whiteQueenSideCastling) board.zobristHash ^= ZobristTable.CASTLING[1];
        if (board.blackKingSideCastling)  board.zobristHash ^= ZobristTable.CASTLING[2];
        if (board.blackQueenSideCastling) board.zobristHash ^= ZobristTable.CASTLING[3];
        if (board.enPassantTarget != null) {
            board.zobristHash ^= ZobristTable.EN_PASSANT_FILE[board.enPassantTarget[1]];
        }
    }

    public static void unmakeMove(BoardEnv board, Move move, UndoInfo undo) {
        // Restore simple fields
        board.whiteToMove            = undo.whiteToMove;
        board.whiteKingSideCastling  = undo.whiteKingSideCastling;
        board.whiteQueenSideCastling = undo.whiteQueenSideCastling;
        board.blackKingSideCastling  = undo.blackKingSideCastling;
        board.blackQueenSideCastling = undo.blackQueenSideCastling;
        board.halfMoveClock          = undo.halfMoveClock;
        board.enPassantTarget        = undo.enPassantTarget;
        board.whiteKingPos           = undo.whiteKingPos;
        board.blackKingPos           = undo.blackKingPos;
        board.zobristHash            = undo.preMoveZobristHash;
        board.totalHalfMoveCount     = undo.totalHalfMoveCount;

        // Restore pieces
        if (Character.toLowerCase(move.piece) == 'k' && Math.abs(move.toCol - move.fromCol) == 2) {
            // Undo castling
            board.state[move.fromRow][move.fromCol] = move.piece;
            board.state[move.toRow][move.toCol] = '\0';
            if (move.toCol == 6) { // kingside
                board.state[move.toRow][7] = (move.piece == 'K' ? 'R' : 'r');
                board.state[move.toRow][5] = '\0';
            } else { // queenside
                board.state[move.toRow][0] = (move.piece == 'K' ? 'R' : 'r');
                board.state[move.toRow][3] = '\0';
            }
        } else if (undo.wasEnPassant) {
            // Undo en passant
            board.state[move.fromRow][move.fromCol] = move.piece;
            board.state[move.toRow][move.toCol] = '\0';

            board.state[undo.capturedPawnPos[0]][undo.capturedPawnPos[1]] = undo.capturedPiece;

        } else {
            // Undo normal move or promotion
            if (undo.wasPromotion) {
                board.state[move.fromRow][move.fromCol] =
                        Character.isUpperCase(move.piece) ? 'P' : 'p';
            } else {
                board.state[move.fromRow][move.fromCol] = move.piece;
            }

            board.state[move.toRow][move.toCol] = undo.capturedPiece; // '\0' if no capture
        }

        if (undo.didPostMoveCalculations) {
            // Undo transposition table and move history
            int count = board.transpositionTable.getOrDefault(undo.postMoveZobristHash, 0) - 1;
            if (count <= 0) board.transpositionTable.remove(undo.postMoveZobristHash);
            else board.transpositionTable.put(undo.postMoveZobristHash, count);

            if (!board.playedMoves.isEmpty()) {
                board.playedMoves.remove(board.playedMoves.size() - 1);
            }
        }
    }

    /**
     * Checks for insufficient material on the board.
     *
     * @return true if neither side has sufficient material to force checkmate.
     */
    private static boolean insufficientMaterial(BoardEnv board) {
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
     * Plays the sounds according to the type of move.
     */
    private void playSounds(Move move) {
        if (move.isCapture) {
            sounds.get("capture").play();
        } else {
            sounds.get("move").play();
        }
    }

    /**
     * Resets the selection.
     */
    public void resetSelection() {
        selectedRow = -1;
        selectedCol = -1;
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
