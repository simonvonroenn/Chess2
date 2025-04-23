package main.chessboard;

import main.engine.Engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BoardEnv {
    public final char[][] state = new char[8][8];
    public boolean whiteToMove;
    public boolean whiteKingSideCastling;
    public boolean whiteQueenSideCastling;
    public boolean blackKingSideCastling ;
    public boolean blackQueenSideCastling;
    public int[] enPassantTarget; // En passant target square: null if none, else [row, col]
    public int[] previousEnPassantTarget; // Store en passant target to later undo a move (only for evaluation)
    public int halfMoveClock = 0; // Counts half moves since last pawn move or capture
    public int totalHalfMoveCount = 0;
    public List<Move> playedMoves = new ArrayList<>();
    public final Map<String, Integer> transpositionTable = new java.util.HashMap<>();

    public Integer evaluation;

    public int pieceValueSum;

    public static int[][] _debug_pieceValues = new int[8][8];

    public BoardEnv() {}

    public BoardEnv(String FEN) {
        String[] rows = FEN.split(" ")[0].split("/");
        for (int i = 0; i < 8; i++) {
            int offset = 0;
            for (int j = 0; j < rows[i].length(); j++) {
                if (Character.isDigit(rows[i].charAt(j))) {
                    offset += Character.getNumericValue(rows[i].charAt(j)) - 1;
                } else {
                    state[i][j + offset] = rows[i].charAt(j);
                }
            }
        }
        whiteToMove = FEN.split(" ")[1].equals("w");
        whiteKingSideCastling = FEN.split(" ")[2].contains("K");
        whiteQueenSideCastling = FEN.split(" ")[2].contains("Q");
        blackKingSideCastling = FEN.split(" ")[2].contains("k");
        blackQueenSideCastling = FEN.split(" ")[2].contains("q");
        String epSquare = FEN.split(" ")[3];
        if (!epSquare.equals("-")) {
            enPassantTarget = new int[]{epSquare.charAt(0) - 'a', 8 - Integer.parseInt(String.valueOf(epSquare.charAt(1)))};
        }
        halfMoveClock = Integer.parseInt(FEN.split(" ")[4]);
        int[] evalInfo = Engine.evaluatePosition(this);
        pieceValueSum = evalInfo[1];
        System.out.println("whiteToMove: " + whiteToMove);
        System.out.println("whiteKingSideCastling: " + whiteKingSideCastling);
        System.out.println("whiteQueenSideCastling: " + whiteQueenSideCastling);
        System.out.println("blackKingSideCastling: " + blackKingSideCastling);
        System.out.println("blackQueenSideCastling: " + blackQueenSideCastling);
        System.out.println("enPassantTarget: " + Arrays.toString(enPassantTarget));
        System.out.println("halfMoveClock: " + halfMoveClock);
    }

    public BoardEnv deepCopy() {
        BoardEnv copy = new BoardEnv();

        for (int i = 0; i < 8; i++) {
            System.arraycopy(this.state[i], 0, copy.state[i], 0, 8);
        }

        copy.whiteToMove = this.whiteToMove;
        copy.whiteKingSideCastling = this.whiteKingSideCastling;
        copy.whiteQueenSideCastling = this.whiteQueenSideCastling;
        copy.blackKingSideCastling = this.blackKingSideCastling;
        copy.blackQueenSideCastling = this.blackQueenSideCastling;

        if (this.enPassantTarget != null) {
            copy.enPassantTarget = new int[] { this.enPassantTarget[0], this.enPassantTarget[1] };
        }

        copy.halfMoveClock = this.halfMoveClock;
        copy.evaluation = this.evaluation;
        copy.pieceValueSum = this.pieceValueSum;

        // Deep copy der Transpositionstabelle
        copy.transpositionTable.putAll(this.transpositionTable);

        return copy;
    }
}
