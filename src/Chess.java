import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import Game.GameLogic;

public class Chess extends Application {

    @Override
    public void start(Stage primaryStage) {

        HBox mainPane = new HBox();
        VBox infoPane = new VBox();
        HBox buttonPane = new HBox();
        GameLogic logic = new GameLogic();

        Button btn = new Button("End Game");

        buttonPane.setMinHeight(64);
        buttonPane.setAlignment(Pos.CENTER_LEFT);
        buttonPane.getChildren().add(btn);
        infoPane.getChildren().addAll(buttonPane, logic.getInfoBox());
        mainPane.getChildren().addAll(logic.getCanvas(), infoPane);

        Scene game_scene = new Scene(mainPane, logic.getGameWidth(), logic.getGameHeight());

        primaryStage.setScene(game_scene);
        primaryStage.setTitle("Chess");
        primaryStage.setResizable(false);
        primaryStage.show();

        logic.drawCanvas();
    }

    public static void main(String[] args) {
        launch();
    }

}
