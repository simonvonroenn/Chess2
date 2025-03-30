package chessboard;

/**
 * Represents a move on the chess board.
 */
public class Move {
    public int fromRow, fromCol, toRow, toCol;
    public boolean isCapture, isCheck;

    public Move(int fromRow, int fromCol, int toRow, int toCol, boolean isCapture) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.isCapture = isCapture;
        this.isCheck = false;
    }

    public void setCheck() {
        isCheck = true;
    }

    @Override
    public String toString() {
        //return "(" + fromRow + ", " + fromCol + ") -> (" + toRow + ", " + toCol + ")";
        return "" + (char) ('a' + fromCol) + (8 - fromRow) + (char) ('a' + toCol) + (8 - toRow);
    }
}