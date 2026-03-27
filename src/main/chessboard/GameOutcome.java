package main.chessboard;

public enum GameOutcome {
    ONGOING(""),
    CHECKMATE_WHITE("Black wins by checkmate!"),
    CHECKMATE_BLACK("White wins by checkmate!"),
    STALEMATE("Draw by stalemate!"),
    INSUFFICIENT_MATERIAL("Draw by insufficient material!"),
    THREE_FOLD_REPETITION("Draw by threefold repetition!"),
    FIFTY_MOVE_RULE("Draw by 50-move rule!");

    private final String message;

    GameOutcome(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean isCheckmate() {
        return this.equals(CHECKMATE_WHITE) || this.equals(CHECKMATE_BLACK);
    }
}