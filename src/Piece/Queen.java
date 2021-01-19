package Piece;

import Game.Board;
import javafx.scene.image.Image;

public class Queen extends Piece {

    public Queen() {
        img[WHITE] = new Image("queen_w.png");
        img[BLACK] = new Image("queen_b.png");
    }

    @Override
    public boolean lookMoves(Board.BoardTable[][] board, int x, int y) {
        int piece_col = board[x][y].piece_color;
        int[][] xy_add = {{ -1, -1}, { 0, -1}, {  1,  -1}, {  1,  0},
                          {  1,  1}, { 0,  1}, { -1,   1}, { -1,  0}};
        int xx = x;
        int yy = y;
        boolean checkmate = false;

        for (int i = 0; i < 8; i++) {
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

                    if (board[xx][yy].piece == KING) {
                        int xx2 = xx;
                        int yy2 = yy;

                        while(xx2 != x || yy2 != y) {
                            xx2 -= xy_add[i][0];
                            yy2 -= xy_add[i][1];
                            board[xx2][yy2].square_checkmate = true;
                        }
                        checkmate = true;
                    }

                    break;
                }
                else break;
            }

            xx = x;
            yy = y;
        }

        return checkmate;
    }
}