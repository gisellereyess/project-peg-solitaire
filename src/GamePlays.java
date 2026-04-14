import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a recorded Solitaire game from a text file and replays it.
 * Parses the board setup and move list saved by GameLogger.
 */
public class GamePlays {

    private int size;
    private SolitaireGame.BoardType boardType;
    private List<String> moves;

    /**
     * Loads a recorded game file and parses its contents.
     *
     * @param filename the file to read from
     * @return true if loaded successfully, false if file not found or invalid
     */
    public boolean loadGame(String filename) {
        moves = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("BOARD_SIZE:")) {
                    size = Integer.parseInt(line.substring(11));
                } else if (line.startsWith("BOARD_TYPE:")) {
                    boardType = SolitaireGame.BoardType.valueOf(line.substring(11));
                } else if (line.startsWith("MOVE:") || line.equals("RANDOMIZE")) {
                    moves.add(line);
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Could not load game: " + e.getMessage());
            return false;
        }
    }

    /** Returns the board size from the recorded game. */
    public int getSize() { return size; }

    /** Returns the board type from the recorded game. */
    public SolitaireGame.BoardType getBoardType() { return boardType; }

    /** Returns the list of recorded moves/events. */
    public List<String> getMoves() { return moves; }
}