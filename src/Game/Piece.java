package Game;

import javafx.scene.canvas.GraphicsContext;

public abstract class Piece {

    static final int NONE  = -1;
    static final int WHITE = 0;
    static final int BLACK = 1;

    static final int PAWN   = 0;
    static final int KNIGHT = 1;
    static final int BISHOP = 2;
    static final int ROCK   = 3;
    static final int QUEEN  = 4;
    static final int KING   = 5;

    protected boolean checkXY(int x, int y) {
        return (x >= 0 && x < 8 && y >= 0 && y < 8);
    }

    public abstract void draw(GraphicsContext gc, int x, int y, int type);
    public abstract void lookMoves(Board.BoardTable[][] board, int x, int y);
}
