import java.util.ArrayList;
import java.util.List;

/**
 * Manages all game logic for Peg Solitaire (BrainVita).
 * This class is completely independent of the user interface.
 *
 * <p>Board cell values:
 * <ul>
 *   <li>{@link #INVALID} – outside the playable area</li>
 *   <li>{@link #PEG}     – a marble/peg is present</li>
 *   <li>{@link #EMPTY}   – an empty hole</li>
 * </ul>
 */
public class SolitaireGame {

  /** The available board layout types. */
  public enum BoardType {
    ENGLISH,
    HEXAGON,
    DIAMOND
  }

  /** Cell is not part of the playable board. */
  public static final int INVALID = 0;

  /** Cell contains a peg (marble). */
  public static final int PEG = 1;

  /** Cell is an empty hole. */
  public static final int EMPTY = 2;

  /**
   * All eight jump directions (2 steps each).
   * A player may jump orthogonally or diagonally per the game rules.
   */
  private static final int[][] DIRECTIONS = {
          {-2,  0}, { 2,  0}, { 0, -2}, { 0,  2}    // orthogonal only
  };

  private int[][] board;
  private int size;
  private BoardType boardType;

  /**
   * Creates a new game with the specified board size and type.
   *
   * @param size      the grid size (must be odd and >= 5)
   * @param boardType the board layout (ENGLISH, HEXAGON, or DIAMOND)
   */
  public SolitaireGame(int size, BoardType boardType) {
    this.size = size;
    this.boardType = boardType;
    initializeBoard();
  }

  /**
   * Resets the board to its initial state.
   * All valid cells are filled with pegs except the center, which is empty.
   */
  public void initializeBoard() {
    board = new int[size][size];
    int center = size / 2;

    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        board[r][c] = isValidCell(r, c) ? PEG : INVALID;
      }
    }
    board[center][center] = EMPTY;
  }

  /**
   * Starts a new game with the given size and board type, resetting the board.
   *
   * @param size      the new grid size
   * @param boardType the new board type
   */
  public void newGame(int size, BoardType boardType) {
    this.size = size;
    this.boardType = boardType;
    initializeBoard();
  }

  /**
   * Returns whether the cell at (row, col) belongs to the playable board area.
   *
   * @param row the row index
   * @param col the column index
   * @return true if the cell is part of this board layout
   */
  public boolean isValidCell(int row, int col) {
    if (row < 0 || row >= size || col < 0 || col >= size) {
      return false;
    }
    switch (boardType) {
      case ENGLISH: return isEnglishCell(row, col);
      case HEXAGON: return isHexagonCell(row, col);
      case DIAMOND: return isDiamondCell(row, col);
      default:      return false;
    }
  }

  /**
   * English cross shape: corner regions (one-third of board size) are cut off,
   * leaving a plus/cross pattern.
   */
  private boolean isEnglishCell(int row, int col) {
    int third = size / 3;
    return (row >= third && row < size - third)
        || (col >= third && col < size - third);
  }

  /**
   * Hexagon (octagonal) shape: the entire grid is valid except for the
   * four absolute corner cells, creating a near-rectangular octagon.
   */
  private boolean isHexagonCell(int row, int col) {
    int center = size / 2;
    return !(Math.abs(row - center) == center && Math.abs(col - center) == center);
  }

  /**
   * Diamond shape: only cells within taxicab (Manhattan) distance of
   * {@code size/2} from the center are valid.
   */
  private boolean isDiamondCell(int row, int col) {
    int center = size / 2;
    return Math.abs(row - center) + Math.abs(col - center) <= center;
  }

  /**
   * Returns whether moving from (fromRow, fromCol) to (toRow, toCol) is legal.
   *
   * <p>A move is valid when:
   * <ol>
   *   <li>Both cells are on the board.</li>
   *   <li>The source has a peg and the destination is empty.</li>
   *   <li>The move is exactly 2 steps in a straight direction (orthogonal or diagonal).</li>
   *   <li>The cell between source and destination contains a peg to jump over.</li>
   * </ol>
   *
   * @param fromRow source row
   * @param fromCol source column
   * @param toRow   destination row
   * @param toCol   destination column
   * @return true if the move is legal
   */
  public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
    if (!isValidCell(fromRow, fromCol) || !isValidCell(toRow, toCol)) return false;
    if (board[fromRow][fromCol] != PEG)  return false;
    if (board[toRow][toCol]     != EMPTY) return false;

    int dr = toRow - fromRow;
    int dc = toCol - fromCol;

// Must move exactly 2 steps in one orthogonal direction
    if ((Math.abs(dr) == 2 && dc != 0) || (Math.abs(dc) == 2 && dr != 0)) return false;
    if (Math.abs(dr) != 2 && Math.abs(dc) != 2) return false;
    
    // The jumped cell must hold a peg
    int midRow = fromRow + dr / 2;
    int midCol = fromCol + dc / 2;
    if (!isValidCell(midRow, midCol)) return false;
    return board[midRow][midCol] == PEG;
  }

  /**
   * Executes the move if it is valid: moves the peg and removes the jumped peg.
   *
   * @param fromRow source row
   * @param fromCol source column
   * @param toRow   destination row
   * @param toCol   destination column
   * @return true if the move was successfully made
   */
  public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
    if (!isValidMove(fromRow, fromCol, toRow, toCol)) return false;
    int midRow = (fromRow + toRow) / 2;
    int midCol = (fromCol + toCol) / 2;
    board[fromRow][fromCol] = EMPTY;
    board[midRow][midCol]   = EMPTY;
    board[toRow][toCol]     = PEG;
    return true;
  }

  /**
   * Returns all valid destination cells for a peg at (fromRow, fromCol).
   *
   * @param fromRow source row
   * @param fromCol source column
   * @return list of {toRow, toCol} arrays for each valid move
   */
  public List<int[]> getValidDestinations(int fromRow, int fromCol) {
    List<int[]> destinations = new ArrayList<>();
    for (int[] dir : DIRECTIONS) {
      int toRow = fromRow + dir[0];
      int toCol = fromCol + dir[1];
      if (isValidMove(fromRow, fromCol, toRow, toCol)) {
        destinations.add(new int[]{toRow, toCol});
      }
    }
    return destinations;
  }

  /**
   * Returns true if at least one valid move exists anywhere on the board.
   *
   * @return true if a valid move is available
   */
  public boolean hasValidMoves() {
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        if (board[r][c] == PEG && !getValidDestinations(r, c).isEmpty()) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns true when the game is over (no valid moves remain).
   *
   * @return true if game over
   */
  public boolean isGameOver() {
    return !hasValidMoves();
  }

  /**
   * Returns the current number of pegs on the board.
   *
   * @return the peg count
   */
  public int getPegCount() {
    int count = 0;
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        if (board[r][c] == PEG) count++;
      }
    }
    return count;
  }

  /**
   * Returns a deep copy of the current board state.
   * Modifying the returned array does not affect the game.
   *
   * @return copy of the board grid
   */
  public int[][] getBoard() {
    int[][] copy = new int[size][size];
    for (int r = 0; r < size; r++) {
      copy[r] = board[r].clone();
    }
    return copy;
  }

  /** Returns the current grid size. */
  public int getSize() { return size; }

  /** Returns the current board type. */
  public BoardType getBoardType() { return boardType; }
}
