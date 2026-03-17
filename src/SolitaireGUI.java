import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JavaFX graphical user interface for Peg Solitaire.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Rendering the game board on a {@link Canvas}</li>
 *   <li>Handling player mouse clicks for peg selection and moves</li>
 *   <li>Providing board-size and board-type controls</li>
 * </ul>
 *
 * <p>All game logic is handled exclusively by {@link SolitaireGame}.
 */
public class SolitaireGUI extends Application {

  // ── Visual constants ──────────────────────────────────────────────────────
  private static final int    CANVAS_SIZE  = 490;
  private static final String BG_DARK      = "#3b2b38";
  private static final String PANEL_COLOR  = "#4e3848";
  private static final String PEG_COLOR    = "#d4a8c1";
  private static final String PEG_SHINE    = "#edcfde";
  private static final String SEL_COLOR    = "#ff6b9d";
  private static final String SEL_SHINE    = "#ffadc8";
  private static final String DEST_COLOR   = "#9b6e8a";
  private static final String HOLE_COLOR   = "#231920";
  private static final String CELL_BG      = "#2a1f28";

  // ── UI nodes ──────────────────────────────────────────────────────────────
  private Canvas              boardCanvas;
  private Label               statusLabel;
  private Spinner<Integer>    sizeSpinner;
  private RadioButton         rbEnglish;
  private RadioButton         rbHexagon;
  private RadioButton         rbDiamond;
  private ToggleGroup         boardTypeGroup;

  // ── Game state ────────────────────────────────────────────────────────────
  private SolitaireGame       game;
  private int                 selectedRow  = -1;
  private int                 selectedCol  = -1;
  private final Set<String>   validDests   = new HashSet<>();

  // =========================================================================
  // JavaFX entry point
  // =========================================================================

  @Override
  public void start(Stage stage) {
    // Create the initial game (English 7×7)
    game = new SolitaireGame(7, SolitaireGame.BoardType.ENGLISH);

    BorderPane root = new BorderPane();
    root.setStyle("-fx-background-color: " + BG_DARK + ";");

    // Title bar
    Label title = new Label("✦  Peg Solitaire  ✦");
    title.setFont(Font.font("Georgia", FontWeight.BOLD, 26));
    title.setTextFill(Color.web("#e8d5e0"));
    title.setPadding(new Insets(14, 0, 8, 0));
    BorderPane.setAlignment(title, Pos.CENTER);
    root.setTop(title);

    // Left controls
    root.setLeft(buildControlPanel());

    // Center: board canvas
    boardCanvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
    boardCanvas.setOnMouseClicked(e -> {
      double cellSize = (double) CANVAS_SIZE / game.getSize();
      int col = (int) (e.getX() / cellSize);
      int row = (int) (e.getY() / cellSize);
      handleCellClick(row, col);
    });
    StackPane canvasWrapper = new StackPane(boardCanvas);
    canvasWrapper.setPadding(new Insets(10, 10, 10, 0));
    root.setCenter(canvasWrapper);

    // Status bar
    statusLabel = new Label("Select a board type and click  New Game  to begin.");
    statusLabel.setTextFill(Color.web("#e8d5e0"));
    statusLabel.setFont(Font.font("Georgia", 13));
    statusLabel.setPadding(new Insets(6, 14, 12, 14));
    BorderPane.setAlignment(statusLabel, Pos.CENTER);
    root.setBottom(statusLabel);

    // Draw the default board immediately
    drawBoard();

    Scene scene = new Scene(root, 700, 570);
    stage.setTitle("Peg Solitaire – CS 449 Sprint 2");
    stage.setScene(scene);
    stage.setResizable(false);
    stage.show();
  }

  // =========================================================================
  // Control panel builder
  // =========================================================================

  /** Builds and returns the left-side panel with size, type, and New Game. */
  private VBox buildControlPanel() {
    VBox panel = new VBox(12);
    panel.setPadding(new Insets(20, 14, 20, 14));
    panel.setStyle("-fx-background-color: " + PANEL_COLOR + ";");
    panel.setPrefWidth(165);
    panel.setAlignment(Pos.TOP_CENTER);

    // ── Board size ──
    Label sizeLabel = panelLabel("Board Size:");
    SpinnerValueFactory<Integer> svf =
        new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 9, 7, 2);
    sizeSpinner = new Spinner<>(svf);
    sizeSpinner.setPrefWidth(100);
    sizeSpinner.setStyle("-fx-font-size: 13; -fx-font-family: Georgia;");

    // ── Board type ──
    Label typeLabel = panelLabel("Board Type:");
    boardTypeGroup = new ToggleGroup();
    rbEnglish = styledRadio("English",  true);
    rbHexagon = styledRadio("Hexagon",  false);
    rbDiamond = styledRadio("Diamond",  false);

    // ── New Game button ──
    Button newGameBtn = new Button("New Game");
    newGameBtn.setPrefWidth(135);
    newGameBtn.setStyle(
        "-fx-background-color: #8b5e7a;"
        + "-fx-text-fill: white;"
        + "-fx-font-size: 14;"
        + "-fx-font-family: Georgia;"
        + "-fx-background-radius: 6;"
        + "-fx-cursor: hand;");
    newGameBtn.setOnMouseEntered(e ->
        newGameBtn.setStyle(newGameBtn.getStyle().replace("#8b5e7a", "#a87090")));
    newGameBtn.setOnMouseExited(e ->
        newGameBtn.setStyle(newGameBtn.getStyle().replace("#a87090", "#8b5e7a")));
    newGameBtn.setOnAction(e -> startNewGame());

    panel.getChildren().addAll(
        sizeLabel, sizeSpinner,
        new Separator(),
        typeLabel, rbEnglish, rbHexagon, rbDiamond,
        new Separator(),
        newGameBtn
    );
    return panel;
  }

  /** Creates a styled label for the control panel. */
  private Label panelLabel(String text) {
    Label label = new Label(text);
    label.setTextFill(Color.web("#e8d5e0"));
    label.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
    return label;
  }

  /** Creates a styled radio button and adds it to the board-type toggle group. */
  private RadioButton styledRadio(String text, boolean selected) {
    RadioButton rb = new RadioButton(text);
    rb.setToggleGroup(boardTypeGroup);
    rb.setSelected(selected);
    rb.setTextFill(Color.web("#e8d5e0"));
    rb.setFont(Font.font("Georgia", 13));
    return rb;
  }

  // =========================================================================
  // Game interaction
  // =========================================================================

  /** Starts a new game using the currently selected size and board type. */
  private void startNewGame() {
    int size = sizeSpinner.getValue();
    SolitaireGame.BoardType type = getSelectedBoardType();
    game.newGame(size, type);
    clearSelection();
    setStatus("New game started!  Pegs on board: " + game.getPegCount());
    drawBoard();
  }

  /** Returns the {@link SolitaireGame.BoardType} matching the selected radio button. */
  private SolitaireGame.BoardType getSelectedBoardType() {
    if (rbHexagon.isSelected()) return SolitaireGame.BoardType.HEXAGON;
    if (rbDiamond.isSelected()) return SolitaireGame.BoardType.DIAMOND;
    return SolitaireGame.BoardType.ENGLISH;
  }

  /**
   * Processes a click on board cell (row, col).
   *
   * <ul>
   *   <li>First click on a peg: selects it and highlights valid destinations.</li>
   *   <li>Click on a highlighted destination: executes the move.</li>
   *   <li>Click on another peg: switches selection.</li>
   *   <li>Click elsewhere: deselects.</li>
   * </ul>
   *
   * @param row the clicked row
   * @param col the clicked column
   */
  private void handleCellClick(int row, int col) {
    if (game.isGameOver()) return;
    if (!game.isValidCell(row, col)) return;

    int[][] board = game.getBoard();

    if (selectedRow == -1) {
      // Nothing selected yet – try to select a peg
      if (board[row][col] == SolitaireGame.PEG) {
        selectPeg(row, col);
      }
    } else {
      // A peg is already selected
      String key = row + "," + col;
      if (validDests.contains(key)) {
        // Valid destination clicked – make the move
        game.makeMove(selectedRow, selectedCol, row, col);
        clearSelection();
        if (game.isGameOver()) {
          setStatus("Game Over!  Pegs remaining: " + game.getPegCount()
              + getRatingText(game.getPegCount()));
        } else {
          setStatus("Move made!  Pegs remaining: " + game.getPegCount());
        }
      } else if (board[row][col] == SolitaireGame.PEG) {
        // Clicked a different peg – switch selection
        selectPeg(row, col);
      } else {
        // Invalid click – deselect
        clearSelection();
        setStatus("Invalid destination. Select a peg to move.");
      }
    }
    drawBoard();
  }

  /** Selects the peg at (row, col) and computes its valid destinations. */
  private void selectPeg(int row, int col) {
    selectedRow = row;
    selectedCol = col;
    computeValidDests();
    if (validDests.isEmpty()) {
      setStatus("That peg has no valid moves. Try another.");
      clearSelection();
    } else {
      setStatus("Peg selected at (" + row + ", " + col + ").  Click a highlighted hole to move.");
    }
  }

  /** Computes and stores valid destination keys for the currently selected peg. */
  private void computeValidDests() {
    validDests.clear();
    List<int[]> dests = game.getValidDestinations(selectedRow, selectedCol);
    for (int[] dest : dests) {
      validDests.add(dest[0] + "," + dest[1]);
    }
  }

  /** Clears the current selection and valid destination highlights. */
  private void clearSelection() {
    selectedRow = -1;
    selectedCol = -1;
    validDests.clear();
  }

  /** Updates the status bar text. */
  private void setStatus(String message) {
    statusLabel.setText(message);
  }

  /**
   * Returns a rating string based on remaining peg count (per the game spec).
   *
   * @param pegs the number of pegs remaining
   * @return a rating label
   */
  private String getRatingText(int pegs) {
    if (pegs == 1)        return "  ★★★★  Outstanding!";
    else if (pegs == 2)   return "  ★★★☆  Very Good!";
    else if (pegs == 3)   return "  ★★☆☆  Good";
    else                  return "  ★☆☆☆  Average";
  }

  // =========================================================================
  // Board rendering
  // =========================================================================

  /**
   * Redraws the entire board canvas to reflect the current game state.
   * Highlights the selected peg and valid destination holes.
   */
  private void drawBoard() {
    GraphicsContext gc = boardCanvas.getGraphicsContext2D();
    int    size     = game.getSize();
    double cellSize = (double) CANVAS_SIZE / size;
    double radius   = cellSize * 0.38;
    int[][]board    = game.getBoard();

    // Clear background
    gc.setFill(Color.web(BG_DARK));
    gc.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        if (board[r][c] == SolitaireGame.INVALID) continue;

        double cx  = c * cellSize + cellSize / 2.0;
        double cy  = r * cellSize + cellSize / 2.0;
        String key = r + "," + c;

        boolean isSelected  = (r == selectedRow && c == selectedCol);
        boolean isValidDest = validDests.contains(key);

        // Cell background dimple
        gc.setFill(Color.web(CELL_BG));
        gc.fillOval(cx - cellSize * 0.44, cy - cellSize * 0.44,
                    cellSize * 0.88,      cellSize * 0.88);

        if (board[r][c] == SolitaireGame.PEG) {
          drawPeg(gc, cx, cy, radius, isSelected);
        } else {
          // EMPTY hole
          if (isValidDest) {
            drawValidDest(gc, cx, cy, radius);
          } else {
            drawHole(gc, cx, cy, radius);
          }
        }
      }
    }
  }

  /** Draws a marble/peg circle. Selected pegs are bright pink. */
  private void drawPeg(GraphicsContext gc, double cx, double cy,
                       double radius, boolean selected) {
    // Main marble body
    gc.setFill(Color.web(selected ? SEL_COLOR : PEG_COLOR));
    gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

    // Highlight spot (simulates a marble sheen)
    gc.setFill(Color.web(selected ? SEL_SHINE : PEG_SHINE, 0.55));
    gc.fillOval(cx - radius * 0.52, cy - radius * 0.58,
                radius * 0.68,      radius * 0.48);

    // Subtle edge shadow
    gc.setStroke(Color.web(selected ? "#c04070" : "#a87a9a", 0.7));
    gc.setLineWidth(1.2);
    gc.strokeOval(cx - radius, cy - radius, radius * 2, radius * 2);
  }

  /** Draws a highlighted empty hole indicating a valid move destination. */
  private void drawValidDest(GraphicsContext gc, double cx, double cy, double radius) {
    gc.setFill(Color.web(DEST_COLOR, 0.85));
    gc.fillOval(cx - radius * 0.55, cy - radius * 0.55,
                radius * 1.1,       radius * 1.1);
    gc.setStroke(Color.web("#c090b0", 0.9));
    gc.setLineWidth(1.5);
    gc.strokeOval(cx - radius * 0.55, cy - radius * 0.55,
                  radius * 1.1,       radius * 1.1);
  }

  /** Draws a regular empty hole. */
  private void drawHole(GraphicsContext gc, double cx, double cy, double radius) {
    gc.setFill(Color.web(HOLE_COLOR));
    gc.fillOval(cx - radius * 0.4, cy - radius * 0.4,
                radius * 0.8,      radius * 0.8);
  }

  // =========================================================================
  // Application entry point
  // =========================================================================

  public static void main(String[] args) {
    launch(args);
  }
}
