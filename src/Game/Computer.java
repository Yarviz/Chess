package Game;

import java.util.*;
import java.util.List;

import static Piece.Piece.*;
import static Game.Board.*;
import static Game.GameLogic.*;

public class Computer {

    private final GameLogic logic;
    private final Random rnd;

    private int move_counter;
    private int initial_deep;

    private final int MAX_DEEP = 7;

    private final Move[] move;
    private final int[] piece_value;
    private final int[] deep_node;
    private final ArrayList<Calculated> calculated;

    private class Calculated {
        public final int[] deep_node;
        public int value;
        public Move move;

        Calculated(int[] deep_nodes, int value, Move move) {
            this.deep_node = deep_nodes;
            this.value = value;
            this.move = move;
        }
    }

    public Computer(GameLogic logic) {
        this.logic = logic;

        piece_value = new int[6];
        deep_node = new int[MAX_DEEP];

        piece_value[PAWN] = 1;
        piece_value[KNIGHT] = 3;
        piece_value[BISHOP] = 3;
        piece_value[ROOK] = 5;
        piece_value[QUEEN] = 9;
        piece_value[KING] = 1;

        rnd = new Random();

        calculated = new ArrayList<>();

        move = new Move[2];
        move[0] = new Move(0, 0, 0, 0, 0);
        move[1] = new Move(0, 0, 0, 0, 0);
    }

    public Move makeMove() {

        move_counter = 0;
        initial_deep = 3;
        //countPieces(logic.board_table);

        for (int i = 0; i < initial_deep; i++) {
            deep_node[i] = 0;
            calculated.clear();
        }

        calculateMove(logic.board_table, logic.rules, initial_deep);
        move[1] = countBestValue();

        return move[1];
    }

    private void countPieces(BoardTable[][] table) {

        int pieces = 0;
        for(int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (table[x][y].piece > NONE) ++pieces;
            }
        }

        if (pieces < 6) initial_deep = 5;
            else initial_deep = 3;
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
        Move new_move = new Move(xx, yy, 0, 0, 0);

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
                    new_move.x2 = x;
                    new_move.y2 = y;

                    logic.lookSpecialMoves(temp_board, temp_rules, x, y);
                    logic.lookPawnPassant(temp_board, temp_rules, x, y);
                    logic.lookPlayerCheck(temp_board, temp_rules);
                    logic.movePossibleCastling(temp_board, temp_rules, x, y);

                    if (temp_rules.choose_piece)
                    {
                        temp_board[x][y].piece = QUEEN;
                        temp_rules.choose_piece = false;
                    }

                    //boolean cont = countPositionValue(temp_board, temp_rules, new_move, deep);
                    temp_rules.cur_player ^= 1;
                    ++deep_node[deep];
                    ++move_counter;

                    if (deep > 0 && !temp_rules.checkmate /*&& cont*/) {
                        calculateMove(temp_board, temp_rules, deep);
                    }
                    countPositionValue(temp_board, temp_rules, new_move, deep);
                }
            }
        }
    }

    private Move countBestValue()
    {
        if (calculated.isEmpty()) {
            System.out.println("Computer Draw");
            return new Move(-1, -1, -1, -1,0);
        }

        int item = 0;
        int nodes_count;
        int best_value;
        List<Calculated> result2 = calculated;
        List<Calculated> result = new ArrayList<>();

        Collections.sort(result2, Comparator.comparingInt((Calculated c) -> c.deep_node[initial_deep - 1]).thenComparingInt(c -> c.value));

        nodes_count = result2.get(0).deep_node[initial_deep - 1];

        for(int i = 0; i < result2.size(); i++) {
            if (result2.get(i).deep_node[initial_deep - 1] == nodes_count) {
                result.add(result2.get(i));
                if (++nodes_count > deep_node[initial_deep - 1]) break;
            }
        }

        if (initial_deep % 2 == 0) {
            Collections.sort(result, Comparator.comparingInt((Calculated c) -> c.value));
        }
        else {
            Collections.sort(result, Comparator.comparingInt((Calculated c) -> c.value).reversed());
        }

        /*for (Calculated c: result) {
            System.out.printf("value: %d nodes: %d %d %d ", c.value, c.deep_node[0], c.deep_node[1], c.deep_node[2]);
            System.out.printf("%c%d->%c%d%n", ('A' + c.move.x), c.move.y + 1, ('A' + c.move.x2), c.move.y2 + 1);
        }*/

        best_value = result.get(0).value;
        for(item = 0; item < result.size(); item++) {
            if (result.get(item).value != best_value) break;
        }
        if (item > 0) item = rnd.nextInt(item);

        return result.get(item).move;
    }

    private boolean countPositionValue(BoardTable[][] table, LogicRules rules, Move mov, int deep) {

        int value = 0;
        int piece1 = 0;
        int piece2 = 0;
        boolean cont = true;

        for(int y = 0; y < 8; y++) {
            for(int x = 0; x < 8; x++) {
                if (table[x][y].piece > NONE) {
                    if (table[x][y].piece_color == logic.rules.cur_player) piece1 += piece_value[table[x][y].piece];
                    else piece2 += piece_value[table[x][y].piece];
                }
            }
        }

        value += piece1 - piece2;

        if (rules.checkmate) {
            if (rules.cur_player == logic.rules.cur_player) value -= 30;
                else value += 30;
        }
        else if (rules.check) {
            piece1 = NONE;
            for (Move m: rules.escape_moves) {
                if (table[m.x2][m.y2].piece > NONE) {
                    piece1 = table[m.x2][m.y2].piece;
                    break;
                }
            }

            if (piece1 > NONE) {
                if (rules.cur_player == logic.rules.cur_player) value -= piece_value[piece1];
                    else value += piece_value[piece1];
            }
            else if (rules.cur_player == logic.rules.cur_player) value -= 10;
                else value += 10;
        }

        if (deep == 0) {

            int[] d_nodes = new int[initial_deep];
            for (int i = 0; i < initial_deep; i++) {
                d_nodes[i] = deep_node[i];
            }

            calculated.add(new Calculated(d_nodes, value, new Move(move[0].x, move[0].y, move[0].x2, move[0].y2, rules.cur_player)));
        }

        return cont;
    }
}
