package engine;

public class PieceValues {

    public static final int PAWN = 100;
    public static final int KNIGHT = 320;
    public static final int BISHOP = 330;
    public static final int ROOK = 500;
    public static final int QUEEN = 900;
    public static final int KING = 20000;

    public static final int MAX_PIECE_VALUE_SUM = 2 * QUEEN + 4 * BISHOP + 4 * KNIGHT + 4 * ROOK + 16 * PAWN;

    public static final int[] PAWN_TABLE = {
             0,  0,  0,  0,  0,  0,  0,  0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
             5,  5, 10, 25, 25, 10,  5,  5,
             0,  0,  0, 20, 20,  0,  0,  0,
             5, -5,-10,  0,  0,-10, -5,  5,
             5, 10, 10,-20,-20, 10, 10,  5,
             0,  0,  0,  0,  0,  0,  0,  0
    };
    public static final int[] KNIGHT_TABLE = {
            -50,-40,-30,-30,-30,-30,-40,-50,
            -40,-20,  0,  0,  0,  0,-20,-40,
            -30,  0, 10, 15, 15, 10,  0,-30,
            -30,  5, 15, 20, 20, 15,  5,-30,
            -30,  0, 15, 20, 20, 15,  0,-30,
            -30,  5, 10, 15, 15, 10,  5,-30,
            -40,-20,  0,  5,  5,  0,-20,-40,
            -50,-40,-30,-30,-30,-30,-40,-50
    };
    public static final int[] BISHOP_TABLE = {
            -20,-10,-10,-10,-10,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5, 10, 10,  5,  0,-10,
            -10,  5,  5, 10, 10,  5,  5,-10,
            -10,  0, 10, 10, 10, 10,  0,-10,
            -10, 10, 10, 10, 10, 10, 10,-10,
            -10,  5,  0,  0,  0,  0,  5,-10,
            -20,-10,-10,-10,-10,-10,-10,-20
    };
    public static final int[] ROOK_TABLE = {
             0,  0,  0,  0,  0,  0,  0,  0,
             5, 10, 10, 10, 10, 10, 10,  5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
             0,  0,  0,  5,  5,  0,  0,  0
    };
    public static final int[] QUEEN_TABLE = {
            -20,-10,-10, -5, -5,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5,  5,  5,  5,  0,-10,
             -5,  0,  5,  5,  5,  5,  0, -5,
              0,  0,  5,  5,  5,  5,  0, -5,
            -10,  5,  5,  5,  5,  5,  0,-10,
            -10,  0,  5,  0,  0,  0,  0,-10,
            -20,-10,-10, -5, -5,-10,-10,-20
    };
    public static final int[] KING_MIDGAME_TABLE = {
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -20,-30,-30,-40,-40,-30,-30,-20,
            -10,-20,-20,-20,-20,-20,-20,-10,
             20, 20,  0,  0,  0,  0, 20, 20,
             20, 30, 10,  0,  0, 10, 30, 20
    };
    public static final int[] KING_ENDGAME_TABLE = {
            -50,-40,-30,-20,-20,-30,-40,-50,
            -30,-20,-10,  0,  0,-10,-20,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-30,  0,  0,  0,  0,-30,-30,
            -50,-30,-30,-30,-30,-30,-30,-50
    };

    public static int getPieceValue(char piece) {
        return switch (Character.toLowerCase(piece)) {
            case 'p' -> PAWN;
            case 'n' -> KNIGHT;
            case 'b' -> BISHOP;
            case 'r' -> ROOK;
            case 'q' -> QUEEN;
            case 'k' -> KING;
            default -> 0;
        };
    }

    public static int getPieceTableValue(char piece, int row, int col, int pieceValueSum) {
        int whiteIdx = 8 * row + col;
        int blackIdx = 63 - (8 * row + col);
        return switch (piece) {
            case 'P' -> PAWN_TABLE[whiteIdx];
            case 'N' -> KNIGHT_TABLE[whiteIdx];
            case 'B' -> BISHOP_TABLE[whiteIdx];
            case 'R' -> ROOK_TABLE[whiteIdx];
            case 'Q' -> QUEEN_TABLE[whiteIdx];
            case 'K' -> {
                float frac = (float) pieceValueSum / MAX_PIECE_VALUE_SUM;
                yield  Math.round(frac * KING_MIDGAME_TABLE[whiteIdx] + (1 - frac) * KING_ENDGAME_TABLE[whiteIdx]);
            }
            case 'p' -> PAWN_TABLE[blackIdx];
            case 'n' -> KNIGHT_TABLE[blackIdx];
            case 'b' -> BISHOP_TABLE[blackIdx];
            case 'r' -> ROOK_TABLE[blackIdx];
            case 'q' -> QUEEN_TABLE[blackIdx];
            case 'k' -> {
                float frac = (float) pieceValueSum / MAX_PIECE_VALUE_SUM;
                yield  Math.round(frac * KING_MIDGAME_TABLE[blackIdx] + (1 - frac) * KING_ENDGAME_TABLE[blackIdx]);
            }
            default -> 0;
        };
    }
}
