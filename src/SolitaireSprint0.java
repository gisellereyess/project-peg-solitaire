import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.geometry.Pos;

public class SolitaireSprint0 extends Application {
    @Override
    public void start(Stage stage) {
        // Logic: Stacking vertically with spacing
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #614c58;");

        // Requirement: Text
        Label title = new Label("Solitaire Game Settings");
        title.setStyle("-fx-font-family: 'Old English Text MT';-fx-text-fill: black; -fx-font-size: 35px; -fx-font-weight: bold;");

        // Requirement: Checkbox (your idea: Mute Sound)
        CheckBox muteMusic = new CheckBox("Mute Background Music");
        muteMusic.setStyle("-fx-text-fill: black; -fx-font-size: 15px;");

        // Requirement: Radio Buttons (your idea: Board Shapes)
        Label boardLabel = new Label("Choose Board Type:");
        RadioButton rb1 = new RadioButton("English (Cross)");
        RadioButton rb2 = new RadioButton("Hexagon (Star)");
        ToggleGroup group = new ToggleGroup();
        rb1.setToggleGroup(group);
        rb2.setToggleGroup(group);
        rb1.setStyle("-fx-text-fill: black; -fx-font-size: 15px;");
        rb2.setStyle("-fx-text-fill: black; -fx-font-size: 15px;");
        boardLabel.setStyle("-fx-text-fill: black; -fx-font-size: 15px; -fx-font-weight: bold;");

        // Requirement: Drawing a line with coordinates (artistic fan)
        Canvas canvas = new Canvas(300, 100);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(1.5);

        // Creative Loop: Multiple lines starting from (150, 0)
        for (int i = 0; i <= 300; i += 30) {
            gc.strokeLine(150, 0, i, 80);
        }

        // Add everything to the vertical stack
        root.getChildren().addAll(title, muteMusic, boardLabel, rb1, rb2, canvas);

        Scene scene = new Scene(root, 400, 500);
        stage.setTitle("CS 449 - Sprint 0");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}