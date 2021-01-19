package Game;

import Main.Chess;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class Menu extends Pane {

    private Chess parent;

    private final int WIDTH = 512;
    private final int HEIGHT = 512;

    private Canvas canvas;
    private GraphicsContext gc;

    private MenuText[] text;

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
        getChildren().add(canvas);

        text = new MenuText[3];
        text[0] = new MenuText("Start Game", 224);
        text[1] = new MenuText("Level: Easy", 272);
        text[2] = new MenuText("Exit", 320);

        addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            lookMouseHover((int)event.getSceneX(), (int)event.getSceneY());
        });

        addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            lookMouseClick();
        });
    }

    public int getMenuWidth() {
        return WIDTH;
    }
    public int getMenuHeight() {
        return HEIGHT;
    }

    private void lookMouseHover(int x, int y) {

        boolean redraw = false;

        for (int i = 0; i < 3; i++) {
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
        if (text[0].hover) parent.startGame();
        else if (text[2].hover) System.exit(0);
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

        for(int i = 0; i < 3; i++) {
            drawText(text[i].text, text[i].y_pos, Font.font("Consolas", 24), color[text[i].color]);
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
