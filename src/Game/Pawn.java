package Game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Pawn extends Piece {
    static final Image[] img = new Image[2];

    Pawn() {
        img[WHITE] = new Image("pawn_w.png");
        img[BLACK] = new Image("pawn_b.png");
    }

    @Override
    public void draw(GraphicsContext gc, int x, int y, int type) {
        gc.drawImage(img[type], x, y);
    }

    @Override
    public boolean lookMoves(Board.BoardTable[][] board, int x, int y) {
        int y_add = 1;
        int y_line = 1;
        int piece_col = board[x][y].piece_color;
        boolean checkmate = false;

        if (piece_col == WHITE) {
            y_add = -1;
            y_line = 6;
        }

        if (!checkXY(x, y + y_add)) return false;
        if (board[x][y + y_add].piece == NONE) board[x][y + y_add].square_check = 2;

        if (checkXY(x - 1, y + y_add)) {
            if ((board[x - 1][y + y_add].piece_color != piece_col && board[x - 1][y + y_add].piece > NONE)
                 || board[x - 1][y + y_add].square_pawn) {
                board[x - 1][y + y_add].square_check = 2;

                if (board[x - 1][y + y_add].piece == KING) {
                    board[x][y].square_checkmate = true;
                    checkmate = true;
                }
            }
        }

        if (checkXY(x + 1, y + y_add)) {
            if ((board[x + 1][y + y_add].piece_color != piece_col && board[x + 1][y + y_add].piece > NONE)
                 || board[x + 1][y + y_add].square_pawn) {
                board[x + 1][y + y_add].square_check = 2;

                if (board[x + 1][y + y_add].piece == KING) {
                    board[x][y].square_checkmate = true;
                    checkmate = true;
                }
            }
        }

        if (y == y_line) {
            if (board[x][y + (y_add << 1)].piece == NONE && board[x][y + y_add].piece == NONE) {
                board[x][y + (y_add << 1)].square_check = 3;

                if (board[x][y + (y_add << 1)].piece == KING) {
                    board[x][y].square_checkmate = true;
                    board[x][y + y_add].square_checkmate = true;
                    checkmate = true;
                }
            }
        }

        return checkmate;
    }
}
