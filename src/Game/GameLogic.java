package Game;

import java.util.Vector;

import static Piece.Piece.*;

public class GameLogic extends Board {

    private BoardTable[][] temp_board;
    protected LogicRules rules;

    public static class LogicRules {
        public int cur_player;
        public boolean check;
        public boolean checkmate;
        public boolean choose_piece;
        public final boolean[] king_move;
        public final boolean[] rook_left_move;
        public final boolean[] rook_right_move;
        public Vector<Move> escape_moves;

        LogicRules() {
            this.escape_moves = new Vector<>();
            this.king_move = new boolean[2];
            this.rook_left_move = new boolean[2];
            this.rook_right_move = new boolean[2];
        }

        public void initRules() {
            this.cur_player = WHITE;
            this.choose_piece = false;
            this.king_move[WHITE] = false;
            this.king_move[BLACK] = false;
            this.rook_left_move[WHITE] = false;
            this.rook_left_move[BLACK] = false;
            this.rook_right_move[WHITE] = false;
            this.rook_right_move[BLACK] = false;
            this.check = false;
            this.checkmate = false;
            this.escape_moves.clear();
        }

        public void copyRules(LogicRules src) {
            this.cur_player = src.cur_player;
            this.choose_piece = src.choose_piece;
            this.king_move[WHITE] = src.king_move[WHITE];
            this.king_move[BLACK] = src.king_move[BLACK];
            this.rook_left_move[WHITE] = src.rook_left_move[WHITE];
            this.rook_left_move[BLACK] = src.rook_left_move[BLACK];
            this.rook_right_move[WHITE] = src.rook_right_move[WHITE];
            this.rook_right_move[BLACK] = src.rook_right_move[BLACK];
            this.check = src.check;
            this.checkmate = src.checkmate;
            this.escape_moves = new Vector<>(src.escape_moves);
        }
    }

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
        rules = new LogicRules();
        temp_board = new BoardTable[8][8];
        newBoard(temp_board);
    }

    protected GameLogic getLogic() {
        return this;
    }

    protected void initLogic() {

        rules.initRules();
    }

    protected void lookSpecialMoves(BoardTable[][] table, LogicRules rules, int x, int y) {

        switch (table[x][y].piece) {
            case PAWN:
                if (y == 7 * (rules.cur_player ^ 1)) {
                    setChooseBoxXY(x, 1 + 5 * (rules.cur_player ^ 1));
                    rules.choose_piece = true;
                }
                break;

            case ROOK:
                if (x == 0) rules.rook_left_move[rules.cur_player] = true;
                else if (x == 7) rules.rook_right_move[rules.cur_player] = true;
                break;

            case KING:
                rules.king_move[rules.cur_player] = true;
                break;
        }
    }

    protected boolean lookMoves(BoardTable[][] table, LogicRules rules, int x, int y) {

        boolean moves = false;

        if (rules.check) {
            for (Move escape_move : rules.escape_moves) {
                if (escape_move.x == x && escape_move.y == y) {
                    table[escape_move.x2][escape_move.y2].square_check = 2;
                    moves = true;
                }
            }
            //System.out.println(rules.escape_moves.size());
        }
        else {
            piece[table[x][y].piece].lookMoves(table, x, y);

            if (table[x][y].piece == KING && !rules.king_move[rules.cur_player]) {
                if (!rules.rook_left_move[rules.cur_player]) lookCastling(table, rules, x, y, rules.cur_player, 0);
                if (!rules.rook_right_move[rules.cur_player]) lookCastling(table, rules, x, y, rules.cur_player, 1);
            }

            moves = removeIllegalMoves(table,rules, x, y);
        }

        return moves;
    }

    protected boolean lookPawnPassant(BoardTable[][] table, LogicRules rules, int x, int y) {

        if (table[x][y].square_check == 3) {
            int[] y_add = {-1, 1};
            clearBoardPawn(table);
            table[x][y + y_add[rules.cur_player]].square_pawn = true;
        }
        else if (table[x][y].square_pawn && table[x][y].piece == PAWN) {
            int[] y_add = {-1, 1};
            table[x][y + y_add[rules.cur_player]].piece = NONE;
            table[x][y + y_add[rules.cur_player]].piece_color = NONE;
            clearBoardPawn(table);
            return true;
        }
        else clearBoardPawn(table);

        return false;
    }

    protected int movePossibleCastling(BoardTable[][] table, LogicRules rules, int x, int y) {

        int castling = 0;

        if (table[x][y].square_check == 4) {

            table[0][y].piece = NONE;
            table[0][y].piece_color = NONE;
            table[3][y].piece = ROOK;
            table[3][y].piece_color = rules.cur_player;
            castling = 1;
        }
        else if (board_table[x][y].square_check == 5) {

            table[7][y].piece = NONE;
            table[7][y].piece_color = NONE;
            table[5][y].piece = ROOK;
            table[5][y].piece_color = rules.cur_player;
            castling = 2;
        }

        return castling;
    }

    protected void lookCastling(BoardTable[][] table, LogicRules rules, int x, int y, int player, int type) {

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

        if (table[x4][y].piece_color != player) return;

        while(x1 != x2) {
            x1 += x_add;
            if (table[x1][y].piece > NONE) return;
        }

        x1 = x;
        copyBoard(table, temp_board);

        while(x1 != x3) {
            x1 += x_add;
            temp_board[x1][y].piece = KING;
            temp_board[x1][y].piece_color = player;

            if (lookCheck(temp_board, rules,player ^ 1)) return;
        }

        table[x3][y].square_check = 4 + type;
    }

    protected void lookPlayerCheck(BoardTable[][] table, LogicRules rules) {

        if (lookCheck(table, rules, rules.cur_player)) {
            //System.out.println("Check");
            rules.check = true;
            rules.escape_moves.clear();

            if (lookMate(table, rules,rules.cur_player ^ 1)) {
                //System.out.println("Checkmate!!");
                rules.checkmate = true;
            }
        }
        else rules.check = false;
    }

    protected boolean lookCheck(BoardTable[][] table, LogicRules rules, int col) {

        //clearBoardCheckMate(table);
        boolean check = false;

        for (int yy = 0; yy < 8; yy++) {
            for (int xx = 0; xx < 8; xx++) {
                if (table[xx][yy].piece_color == col) {
                    if (piece[table[xx][yy].piece].lookMoves(table, xx, yy)) check = true;
                }
            }
        }

        return check;
    }

    protected boolean lookMate(BoardTable[][] table, LogicRules rules, int col) {

        boolean mate = true;

        for (int yy = 0; yy < 8; yy++) {
            for (int xx = 0; xx < 8; xx++) {
                if (table[xx][yy].piece_color == col) {
                    if (escapeCheck(table, rules, xx, yy)) mate = false;
                }
            }
        }

        return mate;
    }

    protected boolean escapeCheck(BoardTable[][] table, LogicRules rules, int x, int y) {
        int cur_piece = table[x][y].piece;
        int cur_color = table[x][y].piece_color;
        boolean escape = false;

        clearBoard(table);
        piece[cur_piece].lookMoves(table, x, y);

        table[x][y].piece = NONE;
        table[x][y].piece_color = NONE;

        for (int yy = 0; yy < 8; yy++) {
            for (int xx = 0; xx < 8; xx++) {

                if (table[xx][yy].square_check > 1) {

                    copyBoard(board_table, temp_board);
                    temp_board[xx][yy].piece = cur_piece;
                    temp_board[xx][yy].piece_color = cur_color;

                    if (!lookCheck(temp_board, rules,cur_color ^ 1)) {
                        escape = true;
                        rules.escape_moves.add(new Move(x, y, xx, yy, 0));
                        //System.out.printf("Escape %c%d->%c%d%n", ('A' + x), y + 1, ('A' + xx), yy + 1);
                    }
                }
            }
        }

        table[x][y].piece = cur_piece;
        table[x][y].piece_color = cur_color;

        return escape;
    }

    protected boolean removeIllegalMoves(BoardTable[][] table, LogicRules rules, int x, int y) {
        int cur_piece = table[x][y].piece;
        int cur_color = table[x][y].piece_color;
        boolean moves = false;

        table[x][y].piece = NONE;
        table[x][y].piece_color = NONE;

        for (int yy = 0; yy < 8; yy++) {
            for (int xx = 0; xx < 8; xx++) {

                if (table[xx][yy].square_check > 1) {

                    copyBoard(table, temp_board);
                    temp_board[xx][yy].piece = cur_piece;
                    temp_board[xx][yy].piece_color = cur_color;

                    if (lookCheck(temp_board, rules,cur_color ^ 1)) {
                        table[xx][yy].square_check = 0;
                    }
                    else moves = true;
                }
            }
        }

        table[x][y].piece = cur_piece;
        table[x][y].piece_color = cur_color;

        return moves;
    }
}
