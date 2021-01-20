package Game;

import java.util.Random;
import java.util.Vector;

import static Piece.Piece.*;
import static Game.Board.*;
import static Game.GameLogic.*;

public class Computer {

    private GameLogic logic;
    private Random rnd;
    private int move_counter;
    private int best_value;
    private int temp_value;
    private int initial_deep;
    private Move[] move;
    private final Vector<Move> calc_move;

    private int[] piece_value;

    public Computer(GameLogic logic) {
        this.logic = logic;

        piece_value = new int[6];

        piece_value[PAWN] = 1;
        piece_value[KNIGHT] = 3;
        piece_value[BISHOP] = 4;
        piece_value[ROOK] = 5;
        piece_value[QUEEN] = 9;
        piece_value[KING] = 1;

        rnd = new Random();

        calc_move = new Vector<>();

        move = new Move[2];
        move[0] = new Move(0, 0, 0, 0, 0);
        move[1] = new Move(0, 0, 0, 0, 0);
    }

    public Move makeMove(int player) {

        move_counter = 0;
        best_value = -1000;
        temp_value = 0;
        initial_deep = 4;

        calc_move.clear();

        calculateMove(logic.board_table, logic.rules, initial_deep);
        System.out.printf("Moves: %d  Best: %d Move: %c%d->%c%d%n", move_counter, best_value, ('A' + move[1].x), move[1].y + 1, ('A' + move[1].x2), move[1].y2 + 1);

        return move[1];
    }

    private void calculateMove(BoardTable[][] table, LogicRules rules, int deep) {

        boolean moves;

        BoardTable[][] temp_board = new BoardTable[8][8];
        logic.newBoard(temp_board);
        logic.copyBoard(table, temp_board);

        LogicRules temp_rules = new LogicRules();
        temp_rules.copyRules(rules);

        for(int y = 0; y < 8; y++) {
            for(int x = 0; x < 8; x++) {
                if (temp_board[x][y].piece_color == temp_rules.cur_player && temp_board[x][y].piece > NONE) {

                    logic.clearBoard(temp_board);
                    moves = logic.lookMoves(temp_board, temp_rules, x, y);

                    if (moves) {

                        if (deep == initial_deep) {
                            move[0].x = x;
                            move[0].y = y;
                            //move[1].piece = best_value;
                            temp_value = 0;
                        }
                        testMoves(temp_board, temp_rules, x, y, deep - 1);
                    }
                }
            }
        }
    }

    private void testMoves(BoardTable[][] table, LogicRules rules, int xx, int yy, int deep) {

        BoardTable[][] temp_board = new BoardTable[8][8];
        logic.newBoard(temp_board);

        LogicRules temp_rules = new LogicRules();

        for(int y = 0; y < 8; y++) {
            for(int x = 0; x < 8; x++) {
                if (table[x][y].square_check > 1) {

                    logic.copyBoard(table, temp_board);
                    temp_rules.copyRules(rules);

                    temp_board[x][y].piece = temp_board[xx][yy].piece;
                    temp_board[x][y].piece_color = temp_board[xx][yy].piece_color;
                    temp_board[xx][yy].piece = NONE;
                    temp_board[xx][yy].piece_color = NONE;

                    if (deep == initial_deep - 1) {
                        move[0].x2 = x;
                        move[0].y2 = y;
                    }

                    logic.lookSpecialMoves(temp_board, temp_rules, x, y);
                    logic.lookPawnPassant(temp_board, temp_rules, x, y);
                    logic.lookPlayerCheck(temp_board, temp_rules, x, y);
                    logic.movePossibleCastling(temp_board, temp_rules, x, y);
                    logic.clearBoard(temp_board);

                    countPositionValue(temp_board, temp_rules, x, y, deep);

                    temp_rules.cur_player ^= 1;
                    if (deep > 0 && !temp_rules.checkmate) calculateMove(temp_board, temp_rules, deep);
                        else calc_move.add(move[1]);
                }
            }
        }
    }

    private int countValue(BoardTable[][] table, LogicRules rules)
    {
        int value = 0;
        int piece1 = 0;
        int piece2 = 0;

        for(int y = 0; y < 8; y++) {
            for(int x = 0; x < 8; x++) {
                if (table[x][y].piece > NONE) {
                    if (table[x][y].piece_color == logic.rules.cur_player) piece1 += piece_value[table[x][y].piece];
                      else piece2 += piece_value[table[x][y].piece];
                }
            }
        }

        value += piece1 - piece2;

        /*if (rules.check) {
            if (rules.cur_player == logic.rules.cur_player) value += 2;
            //else value -= 8;
        }*/

        if (rules.checkmate) {
            if (rules.cur_player == logic.rules.cur_player) value += 8;
            //else value -= 50;
        }

        return value;
    }

    private void countPositionValue(BoardTable[][] table, LogicRules rules, int xx, int yy, int deep) {

        temp_value += countValue(table, rules);

        if (deep == 0 || rules.checkmate) {
            if (temp_value > best_value || (temp_value == best_value && rnd.nextBoolean())) {

                best_value = temp_value;
                move[1].x = move[0].x;
                move[1].y = move[0].y;
                move[1].x2 = move[0].x2;
                move[1].y2 = move[0].y2;
            }
            temp_value = 0;
        }

        ++move_counter;
    }
}
