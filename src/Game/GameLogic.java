package Game;

import static Game.Piece.*;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class GameLogic extends Board{

    private int x, y;
    private int cur_player;
    private int game_flags;
    private boolean state;

    static final int F_KING_MOVE   = 0x01;
    static final int F_B_KING_MOVE = 0x02;
    static final int F_ROCK_MOVE   = 0x04;
    static final int F_B_ROCK_MOVE = 0x08;

    GameLogic() {

        this.x = 0;
        this.y = 0;
        this.state = false;
        this.cur_player = WHITE;
        this.game_flags = 0x00;

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
        if (!state && board_table[GAME][x][y].piece == -1) return;

        if (board_table[GAME][x][y].square_check < 2 && state) {
            clearBoard();
            this.state = false;
        }
        else if (state) {

            if (board_table[GAME][this.x][this.y].piece == KING) {
                game_flags |= (F_KING_MOVE << board_table[GAME][this.x][this.y].piece_color);
            }
            else if (board_table[GAME][this.x][this.y].piece == ROCK) {
                game_flags |= (F_ROCK_MOVE << board_table[GAME][this.x][this.y].piece_color);
            }

            board_table[GAME][x][y].piece = board_table[GAME][this.x][this.y].piece;
            board_table[GAME][x][y].piece_color = board_table[GAME][this.x][this.y].piece_color;
            board_table[GAME][this.x][this.y].piece = -1;
            board_table[GAME][this.x][this.y].piece_color = -1;
            this.x = x;
            this.y = y;

            if (board_table[GAME][x][y].square_check == 3) {
                int[] y_add = {-1, 1};
                clearBoardPawn();
                board_table[GAME][x][y + y_add[board_table[GAME][x][y].piece_color]].square_pawn = true;
            }
            else if (board_table[GAME][x][y].square_pawn && board_table[GAME][x][y].piece == PAWN) {
                int[] y_add = {-1, 1};
                board_table[GAME][x][y + y_add[board_table[GAME][x][y].piece_color]].piece = NONE;
                board_table[GAME][x][y + y_add[board_table[GAME][x][y].piece_color]].piece_color = NONE;
                clearBoardPawn();
            }
            else clearBoardPawn();

            if (lookCheck(board_table[GAME][x][y].piece_color, GAME)) {
                System.out.println("Check");
                if (lookMate(board_table[GAME][x][y].piece_color ^ 1)) {
                    System.out.println("Checkmate!!");
                }
                else lookCheck(board_table[GAME][x][y].piece_color, GAME);
            }

            clearBoard();
            this.state = false;
            cur_player ^= 1;
            //System.out.println(game_flags);
        }
        else if (board_table[GAME][x][y].piece_color == cur_player) {
            board_table[GAME][x][y].square_check = 1;
            piece[board_table[GAME][x][y].piece].lookMoves(board_table[GAME], x, y);
            this.x = x;
            this.y = y;
            this.state = true;
        }
    }

    private boolean lookCheck(int col, int table) {

        clearBoardCheckMate();
        boolean check = false;

        for (int yy = 0; yy < 8; yy++) {
            for (int xx = 0; xx < 8; xx++) {
                if (board_table[table][xx][yy].piece_color == col) {
                    if (piece[board_table[table][xx][yy].piece].lookMoves(board_table[table], xx, yy)) check = true;
                }
            }
        }

        return check;
    }

    private boolean lookMate(int col) {

        boolean mate = true;

        for (int yy = 0; yy < 8; yy++) {
            for (int xx = 0; xx < 8; xx++) {
                if (board_table[GAME][xx][yy].piece_color == col) {
                    if (escapeCheck(xx, yy)) mate = false;
                }
            }
        }

        return mate;
    }

    private boolean escapeCheck(int x, int y) {
        int cur_piece = board_table[GAME][x][y].piece;
        int cur_color = board_table[GAME][x][y].piece_color;
        boolean escape = false;

        clearBoard();
        piece[cur_piece].lookMoves(board_table[GAME], x, y);

        board_table[GAME][x][y].piece = NONE;
        board_table[GAME][x][y].piece_color = NONE;

        for (int yy = 0; yy < 8; yy++) {
            for (int xx = 0; xx < 8; xx++) {

                if (board_table[GAME][xx][yy].square_check == 2 && cur_piece == KING) System.out.printf("Square %c%d%n", ('A' + xx), yy + 1);
                if (board_table[GAME][xx][yy].square_check == 2 && (board_table[GAME][xx][yy].square_checkmate || cur_piece == KING)) {

                    copyBoard();
                    board_table[TEMP][xx][yy].piece = cur_piece;
                    board_table[TEMP][xx][yy].piece_color = cur_color;

                    if (!lookCheck(cur_color ^ 1, TEMP)) {
                        escape = true;
                        System.out.printf("Escape %c%d->%c%d%n", ('A' + x), y + 1, ('A' + xx), yy + 1);
                    }
                }
            }
        }

        board_table[GAME][x][y].piece = cur_piece;
        board_table[GAME][x][y].piece_color = cur_color;

        return escape;
    }
}
