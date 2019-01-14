package simulator;

import java.util.Map;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GuiLauncher extends Application {

    private StackPane elevator;
    private Rectangle rectangle = new Rectangle(75, 50, Color.GREEN);
    private Text elevatorText = new Text("0");
    private Text floor1Text = new Text("0");
    private Text floor2Text = new Text("0");
    private Text floor3Text = new Text("0");
    private Text floor4Text = new Text("0");
    private Slider slider = new Slider(0, 5, 0.25);
    private RadioButton button1 = new RadioButton("Train     ");
    private RadioButton button2 = new RadioButton("Test");
    private Text infoText = new Text(250, 475, "Init");

    public long getIterationDelayMillis() {
        return (long) (1000 * slider.getValue());
    }

    public boolean isTrainingActive() {
        if (button1.isSelected() && !button2.isSelected()) {
            return true;
        } else if (!button1.isSelected() && button2.isSelected()) {
            return false;
        }
        throw new RuntimeException("Error with training toggle");
    }

    public void moveUp() {
        Platform.runLater(() -> elevator.setLayoutY(elevator.getLayoutY() - 20));
    }

    public void moveDown() {
        Platform.runLater(() -> elevator.setLayoutY(elevator.getLayoutY() + 20));
    }

    public void updatePeopleInElevator(int people) {
        Platform.runLater(() -> elevatorText.setText(String.valueOf(people)));
    }

    public void updatePeopleWaiting(Map<Integer, Integer> peopleWaiting) {
        Platform.runLater(() -> {
            Platform.runLater(() -> floor1Text.setText(String.valueOf(peopleWaiting.get(1))));
            Platform.runLater(() -> floor2Text.setText(String.valueOf(peopleWaiting.get(2))));
            Platform.runLater(() -> floor3Text.setText(String.valueOf(peopleWaiting.get(3))));
            Platform.runLater(() -> floor4Text.setText(String.valueOf(peopleWaiting.get(4))));
        });
    }

    public void setElevatorColour(Color color) {
        rectangle.setFill(color);
    }

    public void setInfoText(String text) {
        infoText.setText(text);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        elevatorText.setFont(new Font(20));
        elevator = new StackPane(rectangle, elevatorText);
        elevator.setLayoutX(260);
        elevator.setLayoutY(350);

        int start = 50;
        int increment = 80;
        floor4Text.setX(90);
        floor4Text.setY(start);
        floor3Text.setX(90);
        floor3Text.setY(start + increment);
        floor2Text.setX(90);
        floor2Text.setY(start + 2 * increment);
        floor1Text.setX(90);
        floor1Text.setY(start + 3 * increment);
        floor1Text.setFont(new Font(20));
        floor2Text.setFont(new Font(20));
        floor3Text.setFont(new Font(20));
        floor4Text.setFont(new Font(20));

        slider.setMajorTickUnit(1);
        slider.setSnapToTicks(true);
        slider.setMinorTickCount(3);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setLayoutX(50);
        slider.setLayoutY(450);
        slider.setScaleX(1.5);
        slider.setScaleY(1.5);
        Rectangle whiteBox = new Rectangle(390, 110, Paint.valueOf("WHITE"));
        whiteBox.setX(5);
        whiteBox.setY(402);

        ToggleGroup toggleGroup = new ToggleGroup();
        button1.setToggleGroup(toggleGroup);
        button1.setSelected(true);
        button2.setToggleGroup(toggleGroup);
        HBox toggleBox = new HBox(button1, button2);
        toggleBox.setLayoutX(250);
        toggleBox.setLayoutY(430);

        AnchorPane anchorPane = new AnchorPane(floor1Text, floor2Text, floor3Text, floor4Text, elevator, whiteBox, slider, toggleBox, infoText);

        primaryStage.setTitle("Elevator Simulator");
        primaryStage.setResizable(false);
        Scene scene = new Scene(anchorPane, 388, 500);
        scene.getStylesheets().add("grid.css");
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread(new Simulation(this)).start();
    }
}
