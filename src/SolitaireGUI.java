import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for Peg Solitaire game logic.
 * Manages board state, move validation, and game-over detection.
 * Completely independent of the user interface.
 *
 * Subclasses: ManualGame, AutomatedGame
 */
public abstract class SolitaireGame {

  public enum BoardType { ENGLISH, HEXAGON, DIAMOND }

  public static final int INVALID = 0;
  public static final int PEG     = 1;
  public static final int EMPTY   = 2;

  private static final int[][] DIRECTIONS = {
      {-2, 0}, {2, 0}, {0, -2}, {0, 2}
  };

  private int[][] board;
  private int size;
  private BoardType boardType;

  public SolitaireGame(int size, BoardType boardType) {
    this.size = size;
    this.boardType = boardType;
    initializeBoard();
  }

  public void initializeBoard() {
    board = new int[size][size];
    int center = size / 2;
    for (int r = 0; r < size; r++)
      for (int c = 0; c < size; c++)
        board[r][c] = isValidCell(r, c) ? PEG : INVALID;
    board[center][center] = EMPTY;
  }

  public void newGame(int size, BoardType boardType) {
    this.size = size;
    this.boardType = boardType;
    initializeBoard();
  }

  public void setCell(int row, int col, int value) {
    board[row][col] = value;
  }

  public boolean isValidCell(int row, int col) {
    if (row < 0 || row >= size || col < 0 || col >= size) return false;
    switch (boardType) {
      case ENGLISH: return isEnglishCell(row, col);
      case HEXAGON: return isHexagonCell(row, col);
      case DIAMOND: return isDiamondCell(row, col);
      default:      return false;
    }
  }

  private boolean isEnglishCell(int row, int col) {
    int third = size / 3;
    return (row >= third && row < size - third)
        || (col >= third && col < size - third);
  }

  private boolean isHexagonCell(int row, int col) {
    int center = size / 2;
    return !(Math.abs(row - center) == center && Math.abs(col - center) == center);
  }

  private boolean isDiamondCell(int row, int col) {
    int center = size / 2;
    return Math.abs(row - center) + Math.abs(col - center) <= center;
  }

  public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
    if (!isValidCell(fromRow, fromCol) || !isValidCell(toRow, toCol)) return false;
    if (board[fromRow][fromCol] != PEG)   return false;
    if (board[toRow][toCol]     != EMPTY) return false;
    int dr = toRow - fromRow;
    int dc = toCol - fromCol;
    if ((Math.abs(dr) == 2 && dc != 0) || (Math.abs(dc) == 2 && dr != 0)) return false;
    if (Math.abs(dr) != 2 && Math.abs(dc) != 2) return false;
    int midRow = fromRow + dr / 2;
    int midCol = fromCol + dc / 2;
    if (!isValidCell(midRow, midCol)) return false;
    return board[midRow][midCol] == PEG;
  }

  public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
    if (!isValidMove(fromRow, fromCol, toRow, toCol)) return false;
    int midRow = (fromRow + toRow) / 2;
    int midCol = (fromCol + toCol) / 2;
    board[fromRow][fromCol] = EMPTY;
    board[midRow][midCol]   = EMPTY;
    board[toRow][toCol]     = PEG;
    return true;
  }

  public List<int[]> getValidDestinations(int fromRow, int fromCol) {
    List<int[]> destinations = new ArrayList<>();
    for (int[] dir : DIRECTIONS) {
      int toRow = fromRow + dir[0];
      int toCol = fromCol + dir[1];
      if (isValidMove(fromRow, fromCol, toRow, toCol))
        destinations.add(new int[]{toRow, toCol});
    }
    return destinations;
  }

  public boolean hasValidMoves() {
    for (int r = 0; r < size; r++)
      for (int c = 0; c < size; c++)
        if (board[r][c] == PEG && !getValidDestinations(r, c).isEmpty())
          return true;
    return false;
  }

  public boolean isGameOver() { return !hasValidMoves(); }

  public int getPegCount() {
    int count = 0;
    for (int r = 0; r < size; r++)
      for (int c = 0; c < size; c++)
        if (board[r][c] == PEG) count++;
    return count;
  }

  public int[][] getBoard() {
    int[][] copy = new int[size][size];
    for (int r = 0; r < size; r++)
      copy[r] = board[r].clone();
    return copy;
  }

  public int getSize()          { return size; }
  public BoardType getBoardType() { return boardType; }
}
