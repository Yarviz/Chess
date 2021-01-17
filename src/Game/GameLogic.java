package Game;

import static Game.Piece.*;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class GameLogic extends Board{

    private int x, y;
    private boolean state;

    GameLogic() {

        this.x = 0;
        this.y = 0;
        this.state = false;

        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseClick((int)event.getSceneX(), (int)event.getSceneY());
            }
        });
    }

    public void drawCanvas() {
        drawBoard();
    }

    public void mouseClick(int mx, int my) {

        if (mx > SQ_PAD && mx < SQ_PAD + 8 * SQ_SIZE && my > SQ_PAD && my < SQ_PAD + 8 * SQ_SIZE) {
            mx = (mx - SQ_PAD) / SQ_SIZE;
            my = (my - SQ_PAD) / SQ_SIZE;

            setXY(mx, my);
            drawBoard();
        }
    }

    private void setXY(int x, int y) {
        if (!state && board_table[x][y].piece == -1) return;

        if (board_table[x][y].square_check < 2 && state) {
            clearBoard();
            this.state = false;
        }
        else if (state) {

            board_table[x][y].piece = board_table[this.x][this.y].piece;
            board_table[x][y].piece_color = board_table[this.x][this.y].piece_color;
            board_table[this.x][this.y].piece = -1;
            board_table[this.x][this.y].piece_color = -1;
            this.x = x;
            this.y = y;

            if (board_table[x][y].square_check == 3) {
                int[] y_add = {-1, 1};
                clearBoardPawn();
                board_table[x][y + y_add[board_table[x][y].piece_color]].square_pawn = true;
            }
            else if (board_table[x][y].square_pawn && board_table[x][y].piece == PAWN) {
                int[] y_add = {-1, 1};
                board_table[x][y + y_add[board_table[x][y].piece_color]].piece = NONE;
                board_table[x][y + y_add[board_table[x][y].piece_color]].piece_color = NONE;
                clearBoardPawn();
            }
            else clearBoardPawn();

            if (lookCheck(board_table[x][y].piece_color)) {
                System.out.println("Check");
            }

            clearBoard();
            this.state = false;
        }
        else {
            board_table[x][y].square_check = 1;
            piece[board_table[x][y].piece].lookMoves(board_table, x, y);
            this.x = x;
            this.y = y;
            this.state = true;
        }
    }

    private boolean lookCheck(int col) {

        for (int yy = 0; yy < 8; yy++) {
            for (int xx = 0; xx < 8; xx++) {
                if (board_table[xx][yy].piece_color == col) {
                    piece[board_table[xx][yy].piece].lookMoves(board_table, xx, yy);
                }
            }
        }

        for (int yy = 0; yy < 8; yy++) {
            for (int xx = 0; xx < 8; xx++) {
                if (board_table[xx][yy].piece == KING) {
                    if (board_table[xx][yy].piece_color == ((col + 1) % 2) && board_table[xx][yy].square_check > 1) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean lookMate(int col) {

        for (int yy = 0; yy < 8; yy++) {
            for (int xx = 0; xx < 8; xx++) {
                if (board_table[xx][yy].piece_color == col) {
                    piece[board_table[xx][yy].piece].lookMoves(board_table, xx, yy);
                }
            }
        }

        for (int yy = 0; yy < 8; yy++) {
            for (int xx = 0; xx < 8; xx++) {
                if (board_table[xx][yy].piece == KING) {
                    if (board_table[xx][yy].piece_color == ((col + 1) % 2) && board_table[xx][yy].square_check > 1) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
