package Game;

import java.util.Vector;

import static Piece.Piece.*;

public class GameLogic extends Board {

    protected int cur_player;

    protected boolean check;
    protected boolean checkmate;
    protected boolean choose_piece;
    private final boolean[] king_move;
    private final boolean[] rook_left_move;
    private final boolean[] rook_right_move;
    private final Vector<Move> escape_moves;

    public static class Move {
        public int x;
        public int y;
        public int x2;
        public int y2;
        public int piece;

        Move(int x, int y, int x2, int y2, int piece) {
            this.x = x;
            this.y = y;
            this.x2 = x2;
            this.y2 = y2;
            this.piece = piece;
        }
    }

    public GameLogic() {

        this.escape_moves = new Vector<>(16);
        this.king_move = new boolean[2];
        this.rook_left_move = new boolean[2];
        this.rook_right_move = new boolean[2];
    }

    protected void initLogic() {

        this.check = false;
        this.checkmate = false;
        this.cur_player = WHITE;
        this.choose_piece = false;

        this.king_move[WHITE] = false;
        this.king_move[BLACK] = false;
        this.rook_left_move[WHITE] = false;
        this.rook_left_move[BLACK] = false;
        this.rook_right_move[WHITE] = false;
        this.rook_right_move[BLACK] = false;
    }

    protected void lookSpecialMoves(int x, int y) {

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

    protected void lookIllegalMoves(int x, int y) {

        if (this.check) {
            for (Move escape_move : escape_moves) {
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

    protected boolean lookPawnPassant(int x, int y) {

        if (board_table[GAME][x][y].square_check == 3) {
            int[] y_add = {-1, 1};
            clearBoardPawn();
            board_table[GAME][x][y + y_add[cur_player]].square_pawn = true;
        }
        else if (board_table[GAME][x][y].square_pawn && board_table[GAME][x][y].piece == PAWN) {
            int[] y_add = {-1, 1};
            board_table[GAME][x][y + y_add[cur_player]].piece = NONE;
            board_table[GAME][x][y + y_add[cur_player]].piece_color = NONE;
            clearBoardPawn();
            return true;
        }
        else clearBoardPawn();

        return false;
    }

    protected int movePossibleCastling(int x, int y) {

        int castling = 0;

        if (board_table[GAME][x][y].square_check == 4) {

            board_table[GAME][0][y].piece = NONE;
            board_table[GAME][0][y].piece_color = NONE;
            board_table[GAME][3][y].piece = ROOK;
            board_table[GAME][3][y].piece_color = cur_player;
            castling = 1;
        }
        else if (board_table[GAME][x][y].square_check == 5) {

            board_table[GAME][7][y].piece = NONE;
            board_table[GAME][7][y].piece_color = NONE;
            board_table[GAME][5][y].piece = ROOK;
            board_table[GAME][5][y].piece_color = cur_player;
            castling = 2;
        }

        return castling;
    }

    protected void lookCastling(int x, int y, int player, int type) {

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

            if (lookCheck(player ^ 1, TEMP)) return;
        }

        board_table[GAME][x3][y].square_check = 4 + type;
    }

    protected void lookPlayerCheck(int x, int y) {

        if (lookCheck(board_table[GAME][x][y].piece_color, GAME)) {
            //System.out.println("Check");
            check = true;
            escape_moves.clear();

            if (lookMate(board_table[GAME][x][y].piece_color ^ 1)) {
                //System.out.println("Checkmate!!");
                checkmate = true;
            }
        }
        else check = false;
    }

    protected boolean lookCheck(int col, int table) {

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

    protected boolean lookMate(int col) {

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

    protected boolean escapeCheck(int x, int y) {
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
                        escape_moves.add(new Move(x, y, xx, yy, 0));
                        //System.out.printf("Escape %c%d->%c%d%n", ('A' + x), y + 1, ('A' + xx), yy + 1);
                    }
                }
            }
        }

        board_table[GAME][x][y].piece = cur_piece;
        board_table[GAME][x][y].piece_color = cur_color;

        return escape;
    }

    protected void removeIllegalMoves(int x, int y) {
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
