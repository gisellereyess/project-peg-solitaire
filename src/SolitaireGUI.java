import javafx.application.Application;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.Animation;
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
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JavaFX graphical user interface for Peg Solitaire.
 * Handles rendering and user interaction only.
 * All game logic is delegated to SolitaireGame and its subclasses.
 */
public class SolitaireGUI extends Application {

  // ── Visual constants ──────────────────────────────────────────────────────
  private static final int    CANVAS_SIZE = 490;
  private static final String BG_DARK     = "#3b2b38";
  private static final String PANEL_COLOR = "#4e3848";
  private static final String PEG_COLOR   = "#d4a8c1";
  private static final String PEG_SHINE   = "#edcfde";
  private static final String SEL_COLOR   = "#ff6b9d";
  private static final String SEL_SHINE   = "#ffadc8";
  private static final String DEST_COLOR  = "#9b6e8a";
  private static final String HOLE_COLOR  = "#231920";
  private static final String CELL_BG     = "#2a1f28";

  // ── UI nodes ──────────────────────────────────────────────────────────────
  private Canvas           boardCanvas;
  private Label            statusLabel;
  private Spinner<Integer> sizeSpinner;
  private RadioButton      rbEnglish, rbHexagon, rbDiamond;
  private ToggleGroup      boardTypeGroup;
  private RadioButton      rbManual, rbAutomated;
  private ToggleGroup      gameModeGroup;

  // ── Game state ────────────────────────────────────────────────────────────
  private SolitaireGame    game;
  private int              selectedRow = -1;
  private int              selectedCol = -1;
  private final Set<String> validDests = new HashSet<>();

  // =========================================================================
  // JavaFX entry point
  // =========================================================================

  @Override
  public void start(Stage stage) {
    game = new ManualGame(7, SolitaireGame.BoardType.ENGLISH);

    BorderPane root = new BorderPane();
    root.setStyle("-fx-background-color: " + BG_DARK + ";");

    Label title = new Label("✦  Peg Solitaire  ✦");
    title.setFont(Font.font("Georgia", FontWeight.BOLD, 26));
    title.setTextFill(Color.web("#e8d5e0"));
    title.setPadding(new Insets(14, 0, 8, 0));
    BorderPane.setAlignment(title, Pos.CENTER);
    root.setTop(title);

    root.setLeft(buildControlPanel());

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

    statusLabel = new Label("Select a board type and click  New Game  to begin.");
    statusLabel.setTextFill(Color.web("#e8d5e0"));
    statusLabel.setFont(Font.font("Georgia", 13));
    statusLabel.setPadding(new Insets(6, 14, 12, 14));
    BorderPane.setAlignment(statusLabel, Pos.CENTER);
    root.setBottom(statusLabel);

    drawBoard();

    Scene scene = new Scene(root, 800, 650);
    stage.setTitle("Peg Solitaire – CS 449 Sprint 3");
    stage.setScene(scene);
    stage.setResizable(false);
    stage.show();
  }

  // =========================================================================
  // Control panel
  // =========================================================================

  private VBox buildControlPanel() {
    VBox panel = new VBox(12);
    panel.setPadding(new Insets(20, 14, 20, 14));
    panel.setStyle("-fx-background-color: " + PANEL_COLOR + ";");
    panel.setPrefWidth(165);
    panel.setAlignment(Pos.TOP_CENTER);

    Label sizeLabel = panelLabel("Board Size:");
    sizeSpinner = new Spinner<>(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 9, 7, 2));
    sizeSpinner.setPrefWidth(100);
    sizeSpinner.setStyle("-fx-font-size: 13; -fx-font-family: Georgia;");

    Label typeLabel = panelLabel("Board Type:");
    boardTypeGroup = new ToggleGroup();
    rbEnglish = styledRadio("English", true);
    rbHexagon = styledRadio("Hexagon", false);
    rbDiamond = styledRadio("Diamond", false);

    Label modeLabel = panelLabel("Game Mode:");
    gameModeGroup = new ToggleGroup();
    rbManual = styledModeRadio("Manual", true);
    rbAutomated = styledModeRadio("Automated", false);

    Button newGameBtn  = buildButton("New Game",  "#8b5e7a", e -> startNewGame());
    Button autoplayBtn = buildButton("Autoplay",  "#5e7a8b", e -> startAutoplay());
    Button randomizeBtn = buildButton("Randomize", "#5e8b6a", e -> randomizeBoard());

    panel.getChildren().addAll(
        sizeLabel, sizeSpinner,
        new Separator(),
        typeLabel, rbEnglish, rbHexagon, rbDiamond,
        new Separator(),
        modeLabel, rbManual, rbAutomated,
        new Separator(),
        newGameBtn, autoplayBtn, randomizeBtn
    );
    return panel;
  }

  private Label panelLabel(String text) {
    Label label = new Label(text);
    label.setTextFill(Color.web("#e8d5e0"));
    label.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
    return label;
  }

  private RadioButton styledRadio(String text, boolean selected) {
    RadioButton rb = new RadioButton(text);
    rb.setToggleGroup(boardTypeGroup);
    rb.setSelected(selected);
    rb.setTextFill(Color.web("#e8d5e0"));
    rb.setFont(Font.font("Georgia", 13));
    return rb;
  }

  private RadioButton styledModeRadio(String text, boolean selected) {
    RadioButton rb = new RadioButton(text);
    rb.setToggleGroup(gameModeGroup);
    rb.setSelected(selected);
    rb.setTextFill(Color.web("#e8d5e0"));
    rb.setFont(Font.font("Georgia", 13));
    return rb;
  }

  private Button buildButton(String text, String color,
                             javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
    Button btn = new Button(text);
    btn.setPrefWidth(135);
    btn.setStyle("-fx-background-color: " + color + ";"
        + "-fx-text-fill: white; -fx-font-size: 14;"
        + "-fx-font-family: Georgia; -fx-background-radius: 6; -fx-cursor: hand;");
    btn.setOnAction(handler);
    return btn;
  }

  // =========================================================================
  // Game interaction
  // =========================================================================

  private void startNewGame() {
    int size = sizeSpinner.getValue();
    SolitaireGame.BoardType type = getSelectedBoardType();
    game = rbAutomated.isSelected()
        ? new AutomatedGame(size, type)
        : new ManualGame(size, type);
    clearSelection();
    setStatus("New game started!  Pegs on board: " + game.getPegCount());
    drawBoard();
  }

  private void startAutoplay() {
    if (!(game instanceof AutomatedGame)) {
      setStatus("Switch to Automated mode and start a New Game first!");
      return;
    }
    AutomatedGame ag = (AutomatedGame) game;
    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), event -> {
      boolean moved = ag.autoMove();
      drawBoard();
      if (!moved || game.isGameOver()) {
        setStatus("Automated game over! Pegs remaining: "
            + game.getPegCount() + getRatingText(game.getPegCount()));
      } else {
        setStatus("Auto move made! Pegs remaining: " + game.getPegCount());
      }
    }));
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
  }

  private void randomizeBoard() {
    int size = game.getSize();
    int[][] board = game.getBoard();
    int pegCount = game.getPegCount();

    List<int[]> validCells = new ArrayList<>();
    for (int r = 0; r < size; r++)
      for (int c = 0; c < size; c++)
        if (board[r][c] != SolitaireGame.INVALID)
          validCells.add(new int[]{r, c});

    java.util.Collections.shuffle(validCells);
    for (int i = 0; i < validCells.size(); i++) {
      int[] cell = validCells.get(i);
      game.setCell(cell[0], cell[1], i < pegCount ? SolitaireGame.PEG : SolitaireGame.EMPTY);
    }
    clearSelection();
    setStatus("Board randomized! Pegs: " + game.getPegCount());
    drawBoard();
  }

  private SolitaireGame.BoardType getSelectedBoardType() {
    if (rbHexagon.isSelected()) return SolitaireGame.BoardType.HEXAGON;
    if (rbDiamond.isSelected()) return SolitaireGame.BoardType.DIAMOND;
    return SolitaireGame.BoardType.ENGLISH;
  }

  private void handleCellClick(int row, int col) {
    if (game.isGameOver()) return;
    if (!game.isValidCell(row, col)) return;

    int[][] board = game.getBoard();

    if (selectedRow == -1) {
      if (board[row][col] == SolitaireGame.PEG) selectPeg(row, col);
    } else {
      String key = row + "," + col;
      if (validDests.contains(key)) {
        game.makeMove(selectedRow, selectedCol, row, col);
        clearSelection();
        if (game.isGameOver()) {
          setStatus("Game Over!  Pegs remaining: " + game.getPegCount()
              + getRatingText(game.getPegCount()));
        } else {
          setStatus("Move made!  Pegs remaining: " + game.getPegCount());
        }
      } else if (board[row][col] == SolitaireGame.PEG) {
        selectPeg(row, col);
      } else {
        clearSelection();
        setStatus("Invalid destination. Select a peg to move.");
      }
    }
    drawBoard();
  }

  private void selectPeg(int row, int col) {
    selectedRow = row;
    selectedCol = col;
    computeValidDests();
    if (validDests.isEmpty()) {
      setStatus("That peg has no valid moves. Try another.");
      clearSelection();
    } else {
      setStatus("Peg selected at (" + row + ", " + col
          + ").  Click a highlighted hole to move.");
    }
  }

  private void computeValidDests() {
    validDests.clear();
    for (int[] dest : game.getValidDestinations(selectedRow, selectedCol))
      validDests.add(dest[0] + "," + dest[1]);
  }

  private void clearSelection() {
    selectedRow = -1;
    selectedCol = -1;
    validDests.clear();
  }

  private void setStatus(String message) { statusLabel.setText(message); }

  private String getRatingText(int pegs) {
    if (pegs == 1)      return "  ★★★★  Outstanding!";
    else if (pegs == 2) return "  ★★★☆  Very Good!";
    else if (pegs == 3) return "  ★★☆☆  Good";
    else                return "  ★☆☆☆  Average";
  }

  // =========================================================================
  // Board rendering
  // =========================================================================

  private void drawBoard() {
    GraphicsContext gc = boardCanvas.getGraphicsContext2D();
    int    size     = game.getSize();
    double cellSize = (double) CANVAS_SIZE / size;
    double radius   = cellSize * 0.38;
    int[][] board   = game.getBoard();

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

        gc.setFill(Color.web(CELL_BG));
        gc.fillOval(cx - cellSize * 0.44, cy - cellSize * 0.44,
                    cellSize * 0.88, cellSize * 0.88);

        if (board[r][c] == SolitaireGame.PEG) {
          drawPeg(gc, cx, cy, radius, isSelected);
        } else if (isValidDest) {
          drawValidDest(gc, cx, cy, radius);
        } else {
          drawHole(gc, cx, cy, radius);
        }
      }
    }
  }

  private void drawPeg(GraphicsContext gc, double cx, double cy,
                       double radius, boolean selected) {
    gc.setFill(Color.web(selected ? SEL_COLOR : PEG_COLOR));
    gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
    gc.setFill(Color.web(selected ? SEL_SHINE : PEG_SHINE, 0.55));
    gc.fillOval(cx - radius * 0.52, cy - radius * 0.58,
                radius * 0.68, radius * 0.48);
    gc.setStroke(Color.web(selected ? "#c04070" : "#a87a9a", 0.7));
    gc.setLineWidth(1.2);
    gc.strokeOval(cx - radius, cy - radius, radius * 2, radius * 2);
  }

  private void drawValidDest(GraphicsContext gc, double cx, double cy, double radius) {
    gc.setFill(Color.web(DEST_COLOR, 0.85));
    gc.fillOval(cx - radius * 0.55, cy - radius * 0.55,
                radius * 1.1, radius * 1.1);
    gc.setStroke(Color.web("#c090b0", 0.9));
    gc.setLineWidth(1.5);
    gc.strokeOval(cx - radius * 0.55, cy - radius * 0.55,
                  radius * 1.1, radius * 1.1);
  }

  private void drawHole(GraphicsContext gc, double cx, double cy, double radius) {
    gc.setFill(Color.web(HOLE_COLOR));
    gc.fillOval(cx - radius * 0.4, cy - radius * 0.4,
                radius * 0.8, radius * 0.8);
  }

  public static void main(String[] args) { launch(args); }
}
