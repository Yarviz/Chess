package Main;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import Game.GameLogic;

public class Chess extends Application {

    private HBox mainPane;
    private VBox infoPane;
    private HBox buttonPane;
    private GameLogic logic;
    private Button menuButton;
    private Button replayButton;

    @Override
    public void start(Stage primaryStage) {

        mainPane = new HBox();
        infoPane = new VBox();
        buttonPane = new HBox();
        logic = new GameLogic(this);

        menuButton = new Button("End Game");
        replayButton = new Button();

        menuButton.setOnMouseClicked(event -> gameEnded("Replay Game"));

        buttonPane.setMinHeight(64);
        buttonPane.setSpacing(16);
        buttonPane.setAlignment(Pos.CENTER_LEFT);
        buttonPane.getChildren().addAll(menuButton, replayButton);
        infoPane.getChildren().addAll(buttonPane, logic.getInfoBox());
        mainPane.getChildren().addAll(logic.getCanvas(), infoPane);

        replayButton.setVisible(false);

        Scene game_scene = new Scene(mainPane, logic.getGameWidth(), logic.getGameHeight());

        primaryStage.setScene(game_scene);
        primaryStage.setTitle("Chess");
        primaryStage.setResizable(false);
        primaryStage.show();

        logic.drawCanvas();
    }

    public void gameEnded(String button_string) {

        menuButton.setText("MainMenu");
        replayButton.setText(button_string);
        replayButton.setVisible(true);
        replayButton.setOnMouseClicked(event -> {

            replayButton.setText("Next Step");
            logic.startReplay();
            replayButton.setOnMouseClicked(event_ -> logic.replayStep());
        });
    }

    public static void main(String[] args) {
        launch();
    }

}
