import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 unit tests for {@link SolitaireGame}.
 *
 * <p>Covers: board initialization, valid/invalid moves, peg count,
 * game-over detection, and new game reset.
 *
 * <p>Tests {@code testEnglishInitialPegCount} and {@code testCenterEmptyAfterInit}
 * were generated with ChatGPT assistance and then verified/corrected manually.
 */
public class SolitaireGameTest {

  /** Default game used by most tests: English board, size 7. */
  private SolitaireGame game;

  @BeforeEach
  public void setUp() {
    game = new ManualGame(7, SolitaireGame.BoardType.ENGLISH);
  }

  // =========================================================================
  // User Story 1 & 2: Board initialization (English, Hexagon, Diamond)
  // =========================================================================

  /**
   * Generated with ChatGPT.
   * English 7×7 has 33 valid cells total; minus 1 center = 32 pegs at start.
   */
  @Test
  public void testEnglishInitialPegCount() {
    assertEquals(32, game.getPegCount(),
        "English 7x7 board should start with 32 pegs");
  }

  /**
   * Generated with ChatGPT.
   * The center cell (3, 3) of a 7×7 board must be EMPTY after initialization.
   */
  @Test
  public void testCenterEmptyAfterInit() {
    int center = 7 / 2; // = 3
    int[][] board = game.getBoard();
    assertEquals(SolitaireGame.EMPTY, board[center][center],
        "Center cell should be EMPTY at game start");
  }

  /** Corner cell (0,0) is outside the English cross and must be INVALID. */
  @Test
  public void testEnglishCornerCellIsInvalid() {
    assertFalse(game.isValidCell(0, 0),
        "(0,0) is a cut corner and should not be a valid cell");
  }

  /** Middle-row cell (3,0) is inside the English cross and must be valid. */
  @Test
  public void testEnglishMiddleRowCellIsValid() {
    assertTrue(game.isValidCell(3, 0),
        "(3,0) is on the cross arm and should be a valid cell");
  }

  /** Diamond board center cell must be EMPTY after initialization. */
  @Test
  public void testDiamondCenterEmptyAfterInit() {
    SolitaireGame diamondGame = new ManualGame(7, SolitaireGame.BoardType.DIAMOND);
    int[][] board = diamondGame.getBoard();
    assertEquals(SolitaireGame.EMPTY, board[3][3],
        "Diamond board center should be EMPTY");
  }

  /** Hexagon board center cell must be EMPTY after initialization. */
  @Test
  public void testHexagonCenterEmptyAfterInit() {
    SolitaireGame hexGame = new ManualGame(7, SolitaireGame.BoardType.HEXAGON);
    int[][] board = hexGame.getBoard();
    assertEquals(SolitaireGame.EMPTY, board[3][3],
        "Hexagon board center should be EMPTY");
  }

  // =========================================================================
  // User Story 2: New Game
  // =========================================================================

  /** After calling newGame(), the peg count resets to the initial value. */
  @Test
  public void testNewGameResetsPegCount() {
    game.makeMove(3, 1, 3, 3); // Make one move (peg count drops to 31)
    game.newGame(7, SolitaireGame.BoardType.ENGLISH);
    assertEquals(32, game.getPegCount(),
        "New game should reset peg count to 32");
  }

  /** After calling newGame(), the center cell is EMPTY again. */
  @Test
  public void testNewGameResetsCenterCell() {
    game.makeMove(3, 1, 3, 3);
    game.newGame(7, SolitaireGame.BoardType.ENGLISH);
    int[][] board = game.getBoard();
    assertEquals(SolitaireGame.EMPTY, board[3][3],
        "New game should restore center to EMPTY");
  }

  /** newGame() can switch board types; the new type is reflected correctly. */
  @Test
  public void testNewGameSwitchesBoardType() {
    game.newGame(7, SolitaireGame.BoardType.DIAMOND);
    assertEquals(SolitaireGame.BoardType.DIAMOND, game.getBoardType(),
        "Board type should update to DIAMOND after newGame");
  }

  // =========================================================================
  // User Story 3: Making a move
  // =========================================================================

  /**
   * A valid orthogonal move:
   * (3,1) has a peg, (3,2) has a peg, (3,3) is empty.
   * After the move the source is empty, the jumped peg is removed,
   * and the destination has a peg.
   */
  @Test
  public void testValidMoveUpdatesBoard() {
    assertTrue(game.isValidMove(3, 1, 3, 3),
        "Move (3,1)->(3,3) should be valid at game start");

    game.makeMove(3, 1, 3, 3);
    int[][] board = game.getBoard();

    assertEquals(SolitaireGame.EMPTY, board[3][1], "Source (3,1) should be EMPTY after move");
    assertEquals(SolitaireGame.EMPTY, board[3][2], "Jumped peg at (3,2) should be removed");
    assertEquals(SolitaireGame.PEG,   board[3][3], "Destination (3,3) should have a PEG");
  }

  /** Peg count decreases by exactly 1 after any valid move. */
  @Test
  public void testPegCountDecreasesAfterMove() {
    int before = game.getPegCount();
    game.makeMove(3, 1, 3, 3);
    assertEquals(before - 1, game.getPegCount(),
        "Peg count should decrease by 1 after a valid move");
  }

  /** makeMove() returns false and leaves the board unchanged for an invalid move. */
  @Test
  public void testMakeMoveReturnsFalseForInvalidMove() {
    int before = game.getPegCount();
    boolean result = game.makeMove(0, 0, 0, 2); // (0,0) is not a valid cell
    assertFalse(result, "makeMove should return false for an invalid move");
    assertEquals(before, game.getPegCount(),
        "Board should be unchanged after a rejected move");
  }

  /** A move whose destination already holds a peg is invalid. */
  @Test
  public void testInvalidMoveDestinationOccupied() {
    // (3,0),(3,1),(3,2) all have pegs; destination (3,2) is occupied
    assertFalse(game.isValidMove(3, 0, 3, 2),
        "Cannot move to a cell that already has a peg");
  }

  /** Moving more than 2 steps is not a legal jump. */
  @Test
  public void testInvalidMoveTooFarAway() {
    assertFalse(game.isValidMove(3, 0, 3, 4),
        "A jump of 4 steps should be invalid");
  }

  /** Jumping over an empty cell (no peg to remove) is invalid. */
  @Test
  public void testInvalidMoveNoMiddlePeg() {
    // (3,3) is empty; a move from (3,1) over (3,2) is fine,
    // but from (1,3) over (2,3) to (3,3) requires (2,3) to have a peg –
    // then we clear (2,3) by making a move first to expose the empty middle.
    game.makeMove(3, 1, 3, 3); // (3,2) is now EMPTY
    // Try to jump from (3,0) over now-empty (3,1) to (3,2) – invalid (no middle peg)
    assertFalse(game.isValidMove(3, 0, 3, 2),
        "Cannot jump over an empty cell");
  }

  // =========================================================================
  // User Story 4: Game-over detection
  // =========================================================================

  /** At the start of a fresh game there are many moves available. */
  @Test
  public void testGameNotOverAtStart() {
    assertFalse(game.isGameOver(),
        "Game should not be over at the start");
    assertTrue(game.hasValidMoves(),
        "There should be valid moves at the start");
  }

  /** A 5×5 Diamond board has valid moves at start as well. */
  @Test
  public void testDiamondGameNotOverAtStart() {
    SolitaireGame diamondGame = new ManualGame(5, SolitaireGame.BoardType.DIAMOND);
    assertFalse(diamondGame.isGameOver(),
        "Diamond game should not be over at start");
  }

  /** getBoard() returns a copy; mutating it should not affect the actual game. */
  @Test
  public void testGetBoardReturnsCopy() {
    int[][] copy = game.getBoard();
    copy[3][3] = SolitaireGame.PEG; // Mutate the copy
    int[][] original = game.getBoard();
    assertEquals(SolitaireGame.EMPTY, original[3][3],
        "Mutating getBoard() copy should not change the actual board");
  }

  // =========================================================================
// User Story 6 & 7: Automated game
// =========================================================================

  /** AutomatedGame should start with the same peg count as ManualGame. */
  @Test
  public void testAutomatedGameInitialPegCount() {
    AutomatedGame autoGame = new AutomatedGame(7, SolitaireGame.BoardType.ENGLISH);
    assertEquals(32, autoGame.getPegCount(),
            "Automated game should start with 32 pegs");
  }

  /** autoMove() should return true and decrease peg count by 1. */
  @Test
  public void testAutoMoveDecreasesPegCount() {
    AutomatedGame autoGame = new AutomatedGame(7, SolitaireGame.BoardType.ENGLISH);
    int before = autoGame.getPegCount();
    boolean moved = autoGame.autoMove();
    assertTrue(moved, "autoMove should return true when moves are available");
    assertEquals(before - 1, autoGame.getPegCount(),
            "Peg count should decrease by 1 after autoMove");
  }

  /** autoMove() should return false when no moves are available. */
  @Test
  public void testAutoMoveReturnsFalseWhenGameOver() {
    AutomatedGame autoGame = new AutomatedGame(7, SolitaireGame.BoardType.ENGLISH);
    while (!autoGame.isGameOver()) {
      autoGame.autoMove();
    }
    assertFalse(autoGame.autoMove(),
            "autoMove should return false when game is over");
  }

  /** AutomatedGame should not be over at the start. */
  @Test
  public void testAutomatedGameNotOverAtStart() {
    AutomatedGame autoGame = new AutomatedGame(7, SolitaireGame.BoardType.ENGLISH);
    assertFalse(autoGame.isGameOver(),
            "Automated game should not be over at start");
  }
}
