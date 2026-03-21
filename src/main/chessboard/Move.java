package main.chessboard;

import java.util.StringJoiner;

/**
 * Represents a move on the chess board.
 */
public class Move {
    public final BoardEnv previousBoardEnv;
    public final char piece;
    public final int fromRow, fromCol, toRow, toCol;
    public boolean isCapture, isCheck, isCheckmate;
    public char promotionPiece;

    public Move(BoardEnv previousBoardEnv, char piece, int fromRow, int fromCol, int toRow, int toCol, boolean isCapture) {
        this.previousBoardEnv = previousBoardEnv;
        this.piece = piece;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.isCapture = isCapture;
        this.isCheck = false;
        this.isCheckmate = false;
        this.promotionPiece = '\0';
    }

    public void setCheck() {
        isCheck = true;
    }

    public void setCheckmate() {
        isCheckmate = true;
    }

    public void setPromotionPiece(char piece) {
        promotionPiece = piece;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("");
        if (Character.toLowerCase(piece) == 'k' && Math.abs(toCol - fromCol) == 2) {
            sj.add(toCol > fromCol ? "O-O" : "O-O-O");
        } else {
            if (Character.toLowerCase(piece) == 'p') {
                sj.add(isCapture ? "" + (char) ('a' + fromCol) : "");
            } else {
                sj.add(Character.toString(Character.toUpperCase(piece)));
            }
            sj.add(isCapture ? "x" : "");
            sj.add("" + (char) ('a' + toCol));
            sj.add(Integer.toString((8 - toRow)));
            sj.add(promotionPiece != '\0' ? "=" + Character.toUpperCase(promotionPiece) : "");
        }
        sj.add(isCheckmate ? "#" : (isCheck ? "+" : ""));
        return sj.toString();
    }
}