import java.util.List;

/**
 * Represents a computer-controlled Peg Solitaire game.
 * Extends SolitaireGame with autoMove() which automatically
 * finds and executes a valid move without player input.
 */
public class AutomatedGame extends SolitaireGame {

    // Stores the last move made for logging purposes
    private int[] lastMove;

    public AutomatedGame(int size, BoardType boardType) {
        super(size, boardType);
    }

    /**
     * Finds and executes the first available valid move on the board.
     * @return true if a move was made, false if no moves available
     */
    public boolean autoMove() {
        int[][] board = getBoard();
        int size = getSize();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == PEG) {
                    List<int[]> dests = getValidDestinations(r, c);
                    if (!dests.isEmpty()) {
                        int[] dest = dests.get(0);
                        makeMove(r, c, dest[0], dest[1]);
                        lastMove = new int[]{r, c, dest[0], dest[1]};
                        return true;
                    }
                }
            }
        }
        lastMove = null;
        return false;
    }

    /** Returns the last move made as {fromRow, fromCol, toRow, toCol} or null. */
    public int[] getLastMove() { return lastMove; }
}