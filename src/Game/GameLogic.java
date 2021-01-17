package Game;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

import static Game.Piece.NONE;
import static Game.Piece.PAWN;

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

    public void mouseClick(int mx, int my) {

        if (mx > SQ_PAD && mx < SQ_PAD + 8 * SQ_SIZE && my > SQ_PAD && my < SQ_PAD + 8 * SQ_SIZE) {
            mx = (mx - SQ_PAD) / SQ_SIZE;
            my = (my - SQ_PAD) / SQ_SIZE;

            setXY(mx, my);
            drawBoard();
        }
    }
}
