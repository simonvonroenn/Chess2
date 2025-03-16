/**
 * Represents a move on the chess board.
 */
public class Move {
    public int fromRow, fromCol, toRow, toCol;

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
    }

    @Override
    public String toString() {
        return "(" + fromRow + ", " + fromCol + ") -> (" + toRow + ", " + toCol + ")";
    }
}