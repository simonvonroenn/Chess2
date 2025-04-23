package test;

import main.chessboard.BoardEnv;
import main.chessboard.LegalMoveGenerator;
import main.chessboard.Move;
import main.engine.Engine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.List;

/**
 * Reference link: https://www.chessprogramming.org/Perft_Results
 */
public class MoveGenerationTest {

    private static final int MAX_DEPTH = 4;

    @Test
    public void testMoveGenerationForStartingPos() {
        final String FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        Assertions.assertTrue(testMoveGenerations(new BoardEnv(FEN), new int[]{20, 400, 8902, 197281, 4865609}));
    }

    @Test
    public void testMoveGenerationForCPWPos2() {
        final String FEN = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
        Assertions.assertTrue(testMoveGenerations(new BoardEnv(FEN), new int[]{48, 2039, 97862, 4085603, 193690690}));
    }

    @Test
    public void testMoveGenerationForCPWPos3() {
        final String FEN = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1";
        Assertions.assertTrue(testMoveGenerations(new BoardEnv(FEN), new int[]{14, 191, 2812, 43238, 674624}));
    }

    @Test
    public void testMoveGenerationForCPWPos4() {
        final String FEN = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
        Assertions.assertTrue(testMoveGenerations(new BoardEnv(FEN), new int[]{6, 264, 9467, 422333, 15833292}));
    }

    @Test
    public void testMoveGenerationForCPWPos5() {
        final String FEN = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";
        Assertions.assertTrue(testMoveGenerations(new BoardEnv(FEN), new int[]{44, 1486, 62379, 2103487, 89941194}));
    }

    @Test
    public void testMoveGenerationForCPWPos6() {
        final String FEN = "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10";
        Assertions.assertTrue(testMoveGenerations(new BoardEnv(FEN), new int[]{46, 2079, 89890, 3894594, 164075551}));
    }

    private boolean testMoveGenerations(BoardEnv board, int[] correctCounts) {
        boolean totalPass = true;
        for (int i = 1; i <= MAX_DEPTH; i++) {
            int count = testMoveGenerationForDepth(board, i);
            boolean passed = count == correctCounts[i-1];
            if (!passed) {
                totalPass = false;
            }
            System.out.printf("%s depth %d: %d\n", passed ? "✔" : "✘", i, count);
        }
        return totalPass;
    }

    private int testMoveGenerationForDepth(BoardEnv board, int depth) {
        if (depth == 0) {
            return 1;
        }

        List<Move> moves = Engine.generateAllLegalMoves(board);
        int numPositions = 0;

        for (Move move : moves) {
            char pieceCaptured = LegalMoveGenerator.applyMove(board, move);
            board.whiteToMove = !board.whiteToMove;
            numPositions += testMoveGenerationForDepth(board, depth - 1);
            board.whiteToMove = !board.whiteToMove;
            LegalMoveGenerator.undoMove(board, move, pieceCaptured);
        }

        return numPositions;
    }
}
