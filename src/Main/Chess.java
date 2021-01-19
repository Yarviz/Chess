package Main;

import Game.Menu;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import Game.GameLogic;

public class Chess extends Application {

    private HBox gamePane;
    private VBox infoPane;
    private HBox buttonPane;
    private GameLogic logic;
    private Button menuButton;
    private Button replayButton;
    private Scene game_scene;
    private Scene menu_scene;
    private Stage stage;

    private Menu menu;

    @Override
    public void start(Stage primaryStage) {

        stage = primaryStage;

        primaryStage.setOnCloseRequest(event -> System.exit(0));

        gamePane = new HBox();
        infoPane = new VBox();
        buttonPane = new HBox();
        logic = new GameLogic(this);

        menuButton = new Button();
        replayButton = new Button();

        buttonPane.setMinHeight(64);
        buttonPane.setSpacing(16);
        buttonPane.setAlignment(Pos.CENTER_LEFT);
        buttonPane.getChildren().addAll(menuButton, replayButton);
        infoPane.getChildren().addAll(buttonPane, logic.getInfoBox());
        gamePane.getChildren().addAll(logic.getCanvas(), infoPane);

        menu = new Menu(this);

        game_scene = new Scene(gamePane, logic.getGameWidth(), logic.getGameHeight());
        menu_scene = new Scene(menu, menu.getMenuWidth(), menu.getMenuHeight());

        openMenu();

        stage.setTitle("Chess");
        stage.setResizable(false);
        stage.show();
    }

    public void startGame() {

        menuButton.setText("End Game");
        menuButton.setOnMouseClicked(event -> gameEnded("Replay Game"));

        replayButton.setVisible(false);
        logic.initGame();

        stage.setScene(game_scene);
    }

    public void openMenu() {
        stage.setScene(menu_scene);
        menu.drawMenu();
    }

    public void gameEnded(String button_string) {

        menuButton.setText("MainMenu");
        menuButton.setOnMouseClicked(event -> openMenu());

        replayButton.setText(button_string);
        logic.pauseGame();

        if (logic.getMoveCount() > 0) {
            replayButton.setVisible(true);
            replayButton.setOnMouseClicked(event -> {

                replayButton.setText("Next Step");
                logic.startReplay();
                replayButton.setOnMouseClicked(event_ -> logic.replayStep());
            });
        }
        else openMenu();

    }

    public static void main(String[] args) {
        launch();
    }

}
