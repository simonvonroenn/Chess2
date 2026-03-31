package main.chessboard;

import java.util.Random;

public class ZobristTable {

    // [piece index 0-11][square 0-63]
    // piece index: P=0, N=1, B=2, R=3, Q=4, K=5, p=6, n=7, b=8, r=9, q=10, k=11
    public static final long[][] PIECE_SQUARE = new long[12][64];
    public static final long SIDE_TO_MOVE;
    public static final long[] CASTLING = new long[4]; // WK, WQ, BK, BQ
    public static final long[] EN_PASSANT_FILE = new long[8]; // one per file

    static {
        Random rng = new Random(12052024L); // fixed seed for reproducibility
        for (int p = 0; p < 12; p++) {
            for (int sq = 0; sq < 64; sq++) {
                PIECE_SQUARE[p][sq] = rng.nextLong();
            }
        }
        SIDE_TO_MOVE = rng.nextLong();
        for (int i = 0; i < 4; i++) CASTLING[i] = rng.nextLong();
        for (int i = 0; i < 8; i++) EN_PASSANT_FILE[i] = rng.nextLong();
    }

    /**
     * Maps a piece character to its Zobrist piece index.
     *
     * @param piece the piece character (e.g. 'P', 'n', 'K')
     * @return the index into {@link #PIECE_SQUARE}, or -1 if not a valid piece
     */
    public static int pieceIndex(char piece) {
        return switch (piece) {
            case 'P' -> 0; case 'N' -> 1; case 'B' -> 2;
            case 'R' -> 3; case 'Q' -> 4; case 'K' -> 5;
            case 'p' -> 6; case 'n' -> 7; case 'b' -> 8;
            case 'r' -> 9; case 'q' -> 10; case 'k' -> 11;
            default -> -1;
        };
    }

    /**
     * Computes the Zobrist hash for a board state from scratch.
     * Used once at initialization; afterward the hash is updated incrementally.
     *
     * @param board the board state to hash
     * @return the 64-bit Zobrist hash
     */
    public static long computeHash(BoardEnv board) {
        long hash = 0L;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board.state[row][col];
                if (piece != '\0') {
                    hash ^= PIECE_SQUARE[pieceIndex(piece)][row * 8 + col];
                }
            }
        }
        if (board.whiteToMove) hash ^= SIDE_TO_MOVE;
        if (board.whiteKingSideCastling)  hash ^= CASTLING[0];
        if (board.whiteQueenSideCastling) hash ^= CASTLING[1];
        if (board.blackKingSideCastling)  hash ^= CASTLING[2];
        if (board.blackQueenSideCastling) hash ^= CASTLING[3];
        if (board.enPassantTarget != null) {
            hash ^= EN_PASSANT_FILE[board.enPassantTarget[1]];
        }
        return hash;
    }
}