package Game;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Game extends Application {

    @Override
    public void start(Stage primaryStage) {

        BorderPane mainPane = new BorderPane();
        GameLogic logic = new GameLogic();
        mainPane.setLeft(logic.canvas);

        Scene game_scene = new Scene(mainPane,Board.SQ_SIZE * 14,Board.SQ_SIZE * 10);

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
