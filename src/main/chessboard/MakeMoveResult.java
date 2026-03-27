package main.chessboard;

public class MakeMoveResult {
    public final GameOutcome outcome;
    public final UndoInfo undoInfo;

    public MakeMoveResult(GameOutcome outcome, UndoInfo undoInfo) {
        this.outcome = outcome;
        this.undoInfo = undoInfo;
    }
}