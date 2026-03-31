package main.chessboard;

public class UndoInfo {

    public char capturedPiece;

    public boolean whiteToMove;
    public boolean whiteKingSideCastling;
    public boolean whiteQueenSideCastling;
    public boolean blackKingSideCastling;
    public boolean blackQueenSideCastling;

    public int halfMoveClock;
    public int[] enPassantTarget;

    public int[] whiteKingPos;
    public int[] blackKingPos;

    public boolean wasEnPassant;
    public int[] capturedPawnPos;

    public boolean wasPromotion;

    public boolean didPostMoveCalculations;

    public long preMoveZobristHash;
    public long postMoveZobristHash;
    public int totalHalfMoveCount;

    public UndoInfo(BoardEnv board, char capturedPiece) {
        this.capturedPiece = capturedPiece;

        this.whiteToMove = board.whiteToMove;
        this.whiteKingSideCastling = board.whiteKingSideCastling;
        this.whiteQueenSideCastling = board.whiteQueenSideCastling;
        this.blackKingSideCastling = board.blackKingSideCastling;
        this.blackQueenSideCastling = board.blackQueenSideCastling;

        this.halfMoveClock = board.halfMoveClock;
        this.enPassantTarget = board.enPassantTarget == null ? null : board.enPassantTarget.clone();

        this.whiteKingPos = board.whiteKingPos.clone();
        this.blackKingPos = board.blackKingPos.clone();

        this.preMoveZobristHash = board.zobristHash;
        this.totalHalfMoveCount = board.totalHalfMoveCount;

        this.wasEnPassant = false;
        this.wasPromotion = false;
        this.didPostMoveCalculations = true;
    }
}