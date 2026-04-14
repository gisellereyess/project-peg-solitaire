import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Records a Solitaire game session to a text file.
 * Saves board setup and every move made during the game.
 */
public class GameLogger {

    private PrintWriter writer;
    private boolean active;

    /**
     * Starts a new recording session and writes the game header.
     *
     * @param filename  the file to write to
     * @param size      the board size
     * @param boardType the board type
     */
    public void startRecording(String filename, int size, SolitaireGame.BoardType boardType) {
        try {
            writer = new PrintWriter(new FileWriter(filename));
            writer.println("BOARD_SIZE:" + size);
            writer.println("BOARD_TYPE:" + boardType.name());
            writer.flush();
            active = true;
        } catch (IOException e) {
            System.err.println("Could not start recording: " + e.getMessage());
            active = false;
        }
    }

    /**
     * Records a single move to the file.
     *
     * @param fromRow source row
     * @param fromCol source column
     * @param toRow   destination row
     * @param toCol   destination column
     */
    public void logMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!active || writer == null) return;
        writer.println("MOVE:" + fromRow + "," + fromCol + "," + toRow + "," + toCol);
        writer.flush();
    }

    /**
     * Records a randomize event to the file.
     */
    public void logRandomize() {
        if (!active || writer == null) return;
        writer.println("RANDOMIZE");
        writer.flush();
    }

    /**
     * Stops the recording and closes the file.
     */
    public void stopRecording() {
        if (writer != null) {
            writer.close();
            writer = null;
        }
        active = false;
    }

    /** Returns whether recording is currently active. */
    public boolean isActive() { return active; }
}