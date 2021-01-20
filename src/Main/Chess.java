package Main;

import Game.Menu;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import Game.Game;

public class Chess extends Application {

    private Game game;
    private Menu menu;

    private Button menuButton;
    private Button replayButton;
    private Scene game_scene;
    private Scene menu_scene;
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {

        stage = primaryStage;

        primaryStage.setOnCloseRequest(event -> System.exit(0));

        HBox gamePane = new HBox();
        VBox infoPane = new VBox();
        HBox buttonPane = new HBox();

        game = new Game(this);

        menuButton = new Button();
        replayButton = new Button();

        buttonPane.setMinHeight(64);
        buttonPane.setSpacing(16);
        buttonPane.setAlignment(Pos.CENTER_LEFT);
        buttonPane.getChildren().addAll(menuButton, replayButton);
        infoPane.getChildren().addAll(buttonPane, game.getInfoBox());
        gamePane.getChildren().addAll(game.getCanvas(), infoPane);

        menu = new Menu(this);

        game_scene = new Scene(gamePane, game.getGameWidth(), game.getGameHeight());
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
        game.initGame();

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
        game.pauseGame();

        if (game.getMoveCount() > 0) {
            replayButton.setVisible(true);
            replayButton.setOnMouseClicked(event -> {

                replayButton.setText("Next Step");
                game.startReplay();
                replayButton.setOnMouseClicked(event_ -> game.replayStep());
            });
        }
        else openMenu();

    }

    public static void main(String[] args) {
        launch();
    }

}
