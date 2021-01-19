package Piece;

import static Game.Board.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public abstract class Piece {

    public static final int NONE  = -1;
    public static final int BLACK = 0;
    public static final int WHITE = 1;

    public static final int PAWN   = 0;
    public static final int KNIGHT = 1;
    public static final int BISHOP = 2;
    public static final int ROOK   = 3;
    public static final int QUEEN  = 4;
    public static final int KING   = 5;

    protected final Image[] img = new Image[2];

    protected boolean checkXY(int x, int y) {
        return (x >= 0 && x < 8 && y >= 0 && y < 8);
    }
    public void draw(GraphicsContext gc, int x, int y, int w, int h, int type) { gc.drawImage(img[type], x, y, w, h); }
    public abstract boolean lookMoves(BoardTable[][] board, int x, int y);
}
