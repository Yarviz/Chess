package Piece;

import Game.Board;
import javafx.scene.image.Image;

public class King extends Piece {

    public King() {
        img[WHITE] = new Image("king_w.png");
        img[BLACK] = new Image("king_b.png");
    }

    @Override
    public boolean lookMoves(Board.BoardTable[][] board, int x, int y) {
        int piece_col = board[x][y].piece_color;
        int[][] xy_add = {{ -1, -1}, { 0, -1}, {  1,  -1}, {  1,  0},
                {  1,  1}, { 0,  1}, { -1,   1}, { -1,  0}};
        boolean checkmate = false;

        for (int i = 0; i < 8; i++) {
            if (checkXY(x + xy_add[i][0], y + xy_add[i][1])) {
                if (board[x + xy_add[i][0]][y + xy_add[i][1]].piece_color != piece_col) {
                    board[x + xy_add[i][0]][y + xy_add[i][1]].square_check = 2;

                    if (board[x + xy_add[i][0]][y + xy_add[i][1]].piece == KING) {
                        //board[x][y].square_checkmate = true;
                        checkmate = true;
                    }
                }
            }
        }

        return checkmate;
    }
}
