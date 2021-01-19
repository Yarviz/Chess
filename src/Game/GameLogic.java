package Game;

import static Piece.Piece.*;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class GameLogic extends Board{

    private int x, y;
    private int cur_player;
    private boolean state;
    private boolean check;
    private boolean checkmate;
    private boolean choose_piece;
    private final boolean[] king_move;
    private final boolean[] rook_left_move;
    private final boolean[] rook_right_move;
    private final int[] pieces_win;

    private final Vector<Escape> escape_moves;
    private TextArea infoText;
    private Timer checkTimer;

    private static class Escape {
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

    public GameLogic() {

        this.king_move = new boolean[2];
        this.rook_left_move = new boolean[2];
        this.rook_right_move = new boolean[2];
        this.escape_moves = new Vector<Escape>(16);
        this.pieces_win = new int[2];

        infoText = new TextArea();
        infoText.setFont(Font.font("Arial", SQ_SIZE / 4));
        infoText.setMaxSize(SQ_SIZE * 3, SQ_SIZE * 8);
        infoText.setMinHeight(SQ_SIZE * 8);
        infoText.setEditable(false);
        infoText.setWrapText(true);

        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseClick((int)(event.getSceneX() - canvas.getLayoutX()),
                               (int)(event.getSceneY() - canvas.getLayoutY()));
            }
        });

        checkTimer = new Timer();

        initGame();
    }

    public void initGame() {
        this.x = 0;
        this.y = 0;
        this.state = false;
        this.check = false;
        this.checkmate = false;
        this.choose_piece = false;
        this.cur_player = WHITE;
        this.pieces_win[WHITE] = 0;
        this.pieces_win[BLACK] = 0;

        this.king_move[WHITE] = false;
        this.king_move[BLACK] = false;
        this.rook_left_move[WHITE] = false;
        this.rook_left_move[BLACK] = false;
        this.rook_right_move[WHITE] = false;
        this.rook_right_move[BLACK] = false;

        initBoard();
    }

    public void drawCanvas() {
        drawBoard();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public TextArea getInfoBox() {
        return infoText;
    }

    public int getGameWidth() {
        return SQ_SIZE * 14;
    }

    public int getGameHeight() {
        return SQ_SIZE * 10;
    }

    public void mouseClick(int mx, int my) {

        if (!choose_piece) {
            if (mx > SQ_SIZE && mx < SQ_SIZE + 8 * SQ_SIZE && my > SQ_SIZE && my < SQ_SIZE + 8 * SQ_SIZE) {
                mx = (mx - SQ_SIZE) / SQ_SIZE;
                my = (my - SQ_SIZE) / SQ_SIZE;

                setXY(mx, my);
                if (choose_piece) drawChooseBox(cur_player);
            }
        }
        else choosePiece(mx, my);
    }

    private void choosePiece(int mx, int my) {
        if (mx < box_x || mx > box_x + BOX_W || my < box_y || my > box_y + SQ_SIZE) return;

        mx = (mx - box_x) / SQ_SIZE;

        board_table[GAME][x][y].piece = KNIGHT + mx;
        choose_piece = false;
        lookPlayerCheck();
        clearBoard();

        cur_player ^= 1;
        drawBoard();
    }

    private void setTimer() {
        checkTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                drawBoard();
            }
        }, 3000);
    }

    private void addMoveText(int x, int y) {
        String text = Character.toString('A' + this.x);
        text += (this.y + 1) + ":";
        text += Character.toString('A' + x);
        text += (y + 1) + "  ";

        infoText.appendText(text);
    }

    private void setXY(int x, int y) {
        if (!state && board_table[GAME][x][y].piece == -1) return;

        if (board_table[GAME][x][y].square_check < 2 && state) {
            clearBoard();
            drawBoard();
            this.state = false;
        }

        if (state) {

            removePiece(x, y);
            addMoveText(x, y);

            board_table[GAME][x][y].piece = board_table[GAME][this.x][this.y].piece;
            board_table[GAME][x][y].piece_color = cur_player;
            board_table[GAME][this.x][this.y].piece = NONE;
            board_table[GAME][this.x][this.y].piece_color = NONE;
            this.x = x;
            this.y = y;

            lookSpecialMoves();
            lookPawnPassant();
            lookPlayerCheck();

            movePossibleCastling(x, y);
            clearBoard();
            drawBoard();

            this.state = false;

            if (!choose_piece) cur_player ^= 1;
            if (check) {
                if (!checkmate) drawText("Check");
                    else drawText("Checkmate");
                setTimer();
            }
        }
        else if (board_table[GAME][x][y].piece_color == cur_player) {

            board_table[GAME][x][y].square_check = 1;
            this.x = x;
            this.y = y;
            this.state = true;

            lookIllegalMoves();
            drawBoard();
        }
    }

    private void lookPlayerCheck() {

        if (lookCheck(board_table[GAME][x][y].piece_color, GAME)) {
            System.out.println("Check");
            this.check = true;
            escape_moves.clear();

            if (lookMate(board_table[GAME][x][y].piece_color ^ 1)) {
                System.out.println("Checkmate!!");
                this.checkmate = true;
            }
        }
        else this.check = false;
    }

    private void removePiece(int x, int y) {

        if (board_table[GAME][x][y].piece > NONE) {

            if (cur_player == WHITE) {
                piece[board_table[GAME][x][y].piece].draw(gc, SQ_SIZE - (SQ_SIZE / 8) + pieces_win[WHITE] * (SQ_SIZE / 4),
                        SQ_SIZE * 9 + SQ_SIZE / 3, SQ_SIZE / 2, SQ_SIZE / 2, BLACK);
                ++pieces_win[WHITE];
            }
            else {
                ++pieces_win[BLACK];
                piece[board_table[GAME][x][y].piece].draw(gc, SQ_SIZE * 9 - pieces_win[BLACK] * (SQ_SIZE / 4) - (SQ_SIZE / 8),
                        SQ_SIZE * 9 + SQ_SIZE / 3, SQ_SIZE / 2, SQ_SIZE / 2, WHITE);
            }

            board_table[GAME][x][y].piece = NONE;
            board_table[GAME][x][y].piece_color = NONE;
        }
    }

    private void lookPawnPassant() {

        if (board_table[GAME][x][y].square_check == 3) {
            int[] y_add = {-1, 1};
            clearBoardPawn();
            board_table[GAME][x][y + y_add[cur_player]].square_pawn = true;
        }
        else if (board_table[GAME][x][y].square_pawn && board_table[GAME][x][y].piece == PAWN) {
            int[] y_add = {-1, 1};
            removePiece(x, y + y_add[cur_player]);
            clearBoardPawn();
        }
        else clearBoardPawn();
    }

    private void lookIllegalMoves() {

        if (this.check) {
            for (Escape escape_move : escape_moves) {
                if (escape_move.x == x && escape_move.y == y) {
                    board_table[GAME][escape_move.x2][escape_move.y2].square_check = 2;
                }
            }
        }
        else {
            piece[board_table[GAME][x][y].piece].lookMoves(board_table[GAME], x, y);

            if (board_table[GAME][x][y].piece == KING && !king_move[cur_player]) {
                if (!rook_left_move[cur_player]) lookCastling(x, y, cur_player, 0);
                if (!rook_right_move[cur_player]) lookCastling(x, y, cur_player, 1);
            }

            removeIllegalMoves(x, y);
        }
    }

    private void lookSpecialMoves() {

        switch (board_table[GAME][x][y].piece) {
            case PAWN:
                if (y == 7 * (cur_player ^ 1)) {
                    setChooseBoxXY(x, 1 + 5 * (cur_player ^ 1));
                    this.choose_piece = true;
                }
                break;

            case ROOK:
                if (x == 0) rook_left_move[cur_player] = true;
                else if (x == 7) rook_right_move[cur_player] = true;
                break;

            case KING:
                king_move[cur_player] = true;
                break;
        }
    }

    private void movePossibleCastling(int x, int y) {

        if (board_table[GAME][x][y].square_check == 4) {

            board_table[GAME][0][y].piece = NONE;
            board_table[GAME][0][y].piece_color = NONE;
            board_table[GAME][3][y].piece = ROOK;
            board_table[GAME][3][y].piece_color = cur_player;
        }
        else if (board_table[GAME][x][y].square_check == 5) {

            board_table[GAME][7][y].piece = NONE;
            board_table[GAME][7][y].piece_color = NONE;
            board_table[GAME][5][y].piece = ROOK;
            board_table[GAME][5][y].piece_color = cur_player;
        }
    }

    private void lookCastling(int x, int y, int player, int type) {

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

        if (board_table[GAME][x4][y].piece_color != player) return;

        while(x1 != x2) {
            x1 += x_add;
            if (board_table[GAME][x1][y].piece > NONE) return;
        }

        x1 = x;
        copyBoard();

        while(x1 != x3) {
            x1 += x_add;
            board_table[TEMP][x1][y].piece = KING;
            board_table[TEMP][x1][y].piece_color = player;

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

        for (int yy = 0; yy < 8; yy++) {
            for (int xx = 0; xx < 8; xx++) {

                if (board_table[GAME][xx][yy].square_check > 1 && (board_table[GAME][xx][yy].piece > NONE || cur_piece == KING)) {

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