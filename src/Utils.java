import java.util.Map;

public class Utils {
    private Utils() {}

    /** Maps the FEN pieces to the name of the images in the image/ directory */
    public static final Map<String, String> PIECES = Map.ofEntries(
            Map.entry("b", "black_bishop"),
            Map.entry("k", "black_king"),
            Map.entry("n", "black_knight"),
            Map.entry("p", "black_pawn"),
            Map.entry("q", "black_queen"),
            Map.entry("r", "black_rook"),

            Map.entry("B", "white_bishop"),
            Map.entry("K", "white_king"),
            Map.entry("N", "white_knight"),
            Map.entry("P", "white_pawn"),
            Map.entry("Q", "white_queen"),
            Map.entry("R", "white_rook")
    );
}
