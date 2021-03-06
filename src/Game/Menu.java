package Game;

import Main.Chess;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class Menu extends BorderPane {

    private Chess parent;

    private final int WIDTH = 512;
    private final int HEIGHT = 512;
    private final int HUMAN = 0;
    private final int COMPUTER = 1;

    private Canvas canvas;
    private GraphicsContext gc;

    private MenuText[] text;
    private int player;
    private boolean animations;

    private class MenuText {
        public int color;
        public int y_pos;
        public String text;
        public boolean hover;

        public MenuText(String text, int y_pos) {
            this.text = text;
            this.color = 0;
            this.y_pos = y_pos;
            this.hover = false;
        }
    }

    public Menu(Chess parent) {
        this.parent = parent;

        canvas = new Canvas();
        canvas.setWidth(WIDTH);
        canvas.setHeight(HEIGHT);

        gc = canvas.getGraphicsContext2D();
        setCenter(canvas);

        player = COMPUTER;
        animations = true;

        text = new MenuText[4];
        text[0] = new MenuText("Start Game", 224);
        text[1] = new MenuText("Opponent: Computer", 272);
        text[2] = new MenuText("Animations: On", 320);
        text[3] = new MenuText("Exit", 368);

        canvas.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            lookMouseHover((int)(event.getSceneX() - canvas.getLayoutX()),
                           (int)(event.getSceneY() - canvas.getLayoutY()));
        });

        canvas.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            lookMouseClick();
        });
    }

    private void lookMouseHover(int x, int y) {

        boolean redraw = false;

        for (int i = 0; i < 4; i++) {
            if (y > text[i].y_pos - 8 && y < text[i].y_pos + 8 && x > 128 && x < 384 && !text[i].hover) {
                text[i].hover = true;
                text[i].color = 1;
                redraw = true;
            }
            else if ((y <= text[i].y_pos - 8 || y >= text[i].y_pos + 8 || x <= 128 || x >= 384) && text[i].hover) {
                text[i].hover = false;
                text[i].color = 0;
                redraw = true;
            }
        }

        if (redraw) drawMenuText();
    }

    private void lookMouseClick() {
        if (text[0].hover) parent.startGame(player, animations);
        else if (text[1].hover) {
            player ^= 1;
            if (player == HUMAN) text[1].text = "Opponent: Human";
                else text[1].text = "Opponent: Computer";

            drawMenu();
            drawMenuText();
        }
        else if (text[2].hover) {

            if (animations == false) {
                text[2].text = "Animations: On";
                animations = true;
            }
            else {
                text[2].text = "Animations: Off";
                animations = false;
            }

            drawMenu();
            drawMenuText();
        }
        else if (text[3].hover) System.exit(0);
    }

    private void drawText(String text, int y, Font font, Color color) {
        gc.setFont(font);
        gc.setFill(color);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, canvas.getWidth() / 2, y);
    }

    private void drawMenuText() {
        Color[] color = {Color.GRAY, Color.GREEN};

        for(int i = 0; i < 4; i++) {
            drawText(text[i].text, text[i].y_pos, Font.font("Consolas", 26), color[text[i].color]);
        }
    }

    public void drawMenu() {
        gc.setFill(Color.GRAY);
        gc.fillRect(64, 64, 384, 384);

        gc.setFill(Color.GOLDENROD);
        gc.fillRect(72, 72, 368, 368);

        drawText("CHESS", 128, Font.font("Verdana", 32), Color.WHITE);
        drawMenuText();
    }
}
