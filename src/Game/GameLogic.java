package Game;

import static Game.Piece.*;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

import java.util.Vector;

public class GameLogic extends Board{

    private int x, y;
    private int cur_player;
    private boolean state;
    private boolean check;
    private final boolean[] king_move;
    private final boolean[] rock_left_move;
    private final boolean[] rock_right_move;

    private Vector<Escape> escape_moves;

    private class Escape {
        public int x;
        public int y;
        public int x2;
        public int y2;

        Escape(int x, int y, int x2, int y2) {
            this.x = x;
            this.y = y;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    GameLogic() {

        this.king_move = new boolean[2];
        this.rock_left_move = new boolean[2];
        this.rock_right_move = new boolean[2];
        this.escape_moves = new Vector<Escape>(16);

        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseClick((int)event.getSceneX(), (int)event.getSceneY());
            }
        });

        initGame();
    }

    public void initGame() {
        this.x = 0;
        this.y = 0;
        this.state = false;
        this.check = false;
        this.cur_player = WHITE;

        this.king_move[WHITE] = false;
        this.king_move[BLACK] = false;
        this.rock_left_move[WHITE] = false;
        this.rock_left_move[BLACK] = false;
        this.rock_right_move[WHITE] = false;
        this.rock_right_move[BLACK] = false;

        initBoard();
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

        if (state) {

            if (board_table[GAME][this.x][this.y].piece == KING) {
                king_move[cur_player] = true;
            }
            else if (board_table[GAME][this.x][this.y].piece == ROCK) {
                if (this.x == 0) rock_left_move[cur_player] = true;
                    else if (this.x == 7) rock_right_move[cur_player] = true;
            }

            board_table[GAME][x][y].piece = board_table[GAME][this.x][this.y].piece;
            board_table[GAME][x][y].piece_color = cur_player;
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
                this.check = true;
                escape_moves.clear();

                if (lookMate(board_table[GAME][x][y].piece_color ^ 1)) {
                    System.out.println("Checkmate!!");
                }
                //else lookCheck(board_table[GAME][x][y].piece_color, GAME);
            }
            else this.check = false;

            if (board_table[GAME][x][y].square_check == 4) {

                board_table[GAME][0][y].piece = -1;
                board_table[GAME][0][y].piece_color = -1;
                board_table[GAME][3][y].piece = ROCK;
                board_table[GAME][3][y].piece_color = cur_player;
            }
            else if (board_table[GAME][x][y].square_check == 5) {

                board_table[GAME][7][y].piece = -1;
                board_table[GAME][7][y].piece_color = -1;
                board_table[GAME][5][y].piece = ROCK;
                board_table[GAME][5][y].piece_color = cur_player;
            }

            clearBoard();
            this.state = false;
            cur_player ^= 1;
        }
        else if (board_table[GAME][x][y].piece_color == cur_player) {
            board_table[GAME][x][y].square_check = 1;
            if (this.check) {
                for (int i = 0; i < escape_moves.size(); i++) {
                    if (escape_moves.get(i).x == x && escape_moves.get(i).y == y) {
                        board_table[GAME][escape_moves.get(i).x2][escape_moves.get(i).y2].square_check = 2;
                    }
                }
            }
            else {
                piece[board_table[GAME][x][y].piece].lookMoves(board_table[GAME], x, y);

                if (board_table[GAME][x][y].piece == KING && !king_move[cur_player]) {
                    if (!rock_left_move[cur_player]) lookCastling(x, y, cur_player, 0);
                    if (!rock_right_move[cur_player]) lookCastling(x, y, cur_player, 1);
                }

                removeIllegalMoves(x, y);
            }

            this.x = x;
            this.y = y;
            this.state = true;
        }
    }

    private void lookCastling(int x, int y, int col, int type) {
        int x_add = 1;
        int x1 = x;
        int x2 = x + 2;
        int x3 = x + 2;
        int x4 = 7;

        if (type == 0) {
            x_add = -1;
            x2 = x - 3;
            x3 = x - 2;
            x4 = 0;
        }

        if (board_table[GAME][x4][y].piece_color != cur_player) return;

        while(x1 != x2) {
            x1 += x_add;
            if (board_table[GAME][x1][y].piece > -1) return;
        }

        x1 = x;
        copyBoard();

        while(x1 != x3) {
            x1 += x_add;
            board_table[TEMP][x1][y].piece = KING;
            board_table[TEMP][x1][y].piece_color = cur_player;

            if (lookCheck(cur_player ^ 1, TEMP)) return;
        }

        board_table[GAME][x3][y].square_check = 4 + type;
    }

    private boolean lookCheck(int col, int table) {

        clearBoardCheckMate(table);
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

                if (board_table[GAME][xx][yy].square_check > 1 && (board_table[GAME][xx][yy].square_checkmate || cur_piece == KING)) {

                    copyBoard();
                    board_table[TEMP][xx][yy].piece = cur_piece;
                    board_table[TEMP][xx][yy].piece_color = cur_color;

                    if (!lookCheck(cur_color ^ 1, TEMP)) {
                        escape = true;
                        escape_moves.add(new Escape(x, y, xx, yy));
                        System.out.printf("Escape %c%d->%c%d%n", ('A' + x), y + 1, ('A' + xx), yy + 1);
                    }
                }
            }
        }

        board_table[GAME][x][y].piece = cur_piece;
        board_table[GAME][x][y].piece_color = cur_color;

        return escape;
    }

    private void removeIllegalMoves(int x, int y) {
        int cur_piece = board_table[GAME][x][y].piece;
        int cur_color = board_table[GAME][x][y].piece_color;

        board_table[GAME][x][y].piece = NONE;
        board_table[GAME][x][y].piece_color = NONE;

        if (cur_piece != KING) {
            copyBoard();
            if (lookCheck(cur_color ^ 1, TEMP)) clearBoardIllegal();
        }

        for (int yy = 0; yy < 8; yy++) {
            for (int xx = 0; xx < 8; xx++) {

                if (board_table[GAME][xx][yy].square_check > 1 && (board_table[GAME][xx][yy].piece > -1 || cur_piece == KING)) {

                    copyBoard();
                    board_table[TEMP][xx][yy].piece = cur_piece;
                    board_table[TEMP][xx][yy].piece_color = cur_color;

                    if (lookCheck(cur_color ^ 1, TEMP)) {
                        board_table[GAME][xx][yy].square_check = 0;
                    }
                }
            }
        }

        board_table[GAME][x][y].piece = cur_piece;
        board_table[GAME][x][y].piece_color = cur_color;
    }
}