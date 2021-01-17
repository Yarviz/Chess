package Game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Bishop extends Piece {
    static final Image[] img = new Image[2];

    Bishop() {
        img[WHITE] = new Image("bishop_w.png");
        img[BLACK] = new Image("bishop_b.png");
    }

    @Override
    public void draw(GraphicsContext gc, int x, int y, int type) {
        gc.drawImage(img[type], x, y);
    }

    @Override
    public void lookMoves(Board.BoardTable[][] board, int x, int y) {
        int piece_col = board[x][y].piece_color;
        int[][] xy_add = {{ -1, -1}, { 1, -1}, { 1,  1}, { -1,  1}};
        int xx = x;
        int yy = y;

        for (int i = 0; i < 4; i++) {
            xx += xy_add[i][0];
            yy += xy_add[i][1];

            while (checkXY(xx, yy)) {
                if (board[xx][yy].piece_color == NONE) {
                    board[xx][yy].square_check = 2;

                    xx += xy_add[i][0];
                    yy += xy_add[i][1];
                }
                else if (board[xx][yy].piece_color != piece_col) {
                    board[xx][yy].square_check = 2;
                    break;
                }
                else break;
            }

            xx = x;
            yy = y;
        }
    }
}
