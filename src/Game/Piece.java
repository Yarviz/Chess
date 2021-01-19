package Game;

import static Game.Board.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public abstract class Piece {

    static final int NONE  = -1;
    static final int BLACK = 0;
    static final int WHITE = 1;

    static final int PAWN   = 0;
    static final int KNIGHT = 1;
    static final int BISHOP = 2;
    static final int ROOK   = 3;
    static final int QUEEN  = 4;
    static final int KING   = 5;

    protected final Image[] img = new Image[2];

    protected boolean checkXY(int x, int y) {
        return (x >= 0 && x < 8 && y >= 0 && y < 8);
    }
    public void draw(GraphicsContext gc, int x, int y, int w, int h, int type) { gc.drawImage(img[type], x, y, w, h); }
    public abstract boolean lookMoves(Board.BoardTable[][] board, int x, int y);
}
