package Game;

import java.awt.*;
import java.util.*;
import java.util.List;

import static Piece.Piece.*;
import static Game.Board.*;
import static Game.GameLogic.*;

public class Computer {

    private final GameLogic logic;
    private final Random rnd;

    private int move_counter;
    private int best_value;
    private int initial_deep;

    private final int MAX_DEEP = 7;

    private final Move[] move;
    private final int[] piece_value;
    private final int[] deep_node;
    private final ArrayList<Calculated>[] calculated;

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
        piece_value[QUEEN] = 8;
        piece_value[KING] = 1;

        rnd = new Random();

        calculated = new ArrayList[MAX_DEEP];

        for (int i = 0; i < MAX_DEEP; i++) {
            calculated[i] = new ArrayList<>();
        }

        move = new Move[2];
        move[0] = new Move(0, 0, 0, 0, 0);
        move[1] = new Move(0, 0, 0, 0, 0);
    }

    public Move makeMove() {

        move_counter = 0;

        countPieces(logic.board_table);

        for (int i = 0; i < initial_deep; i++) {
            deep_node[i] = 0;
            calculated[i].clear();
        }

        calculateMove(logic.board_table, logic.rules, initial_deep);
        move[1] = countBestValue();
        //System.out.printf("Moves: %d  Best: %d Move: %c%d->%c%d%n", move_counter, best_value, ('A' + move[1].x), move[1].y + 1, ('A' + move[1].x2), move[1].y2 + 1);

        return move[1];
    }

    private void countPieces(BoardTable[][] table) {

        int pieces = 0;
        for(int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (table[x][y].piece > NONE) {
                    ++pieces;

                    if (table[x][y].piece_color == logic.rules.cur_player) best_value += piece_value[table[x][y].piece];
                        else best_value -= piece_value[table[x][y].piece];
                }
            }
        }

        if (pieces < 4) initial_deep = 6;
        else if (pieces < 8) initial_deep = 5;
        else if (pieces < 16) initial_deep = 4;
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

                    if (temp_rules.choose_piece)
                    {
                        temp_board[x][y].piece = QUEEN;
                        temp_rules.choose_piece = false;
                    }

                    ++deep_node[deep];
                    boolean cont = countPositionValue(temp_board, temp_rules, x, y, deep);

                    temp_rules.cur_player ^= 1;

                    if (deep > 0 && !temp_rules.checkmate && cont) {
                        calculateMove(temp_board, temp_rules, deep);
                    }
                }
            }
        }
    }

    private Move countBestValue()
    {
        int best_value;
        int node, item;
        List<Calculated> result = calculated[0];
        List<Calculated> new_result = new ArrayList<>();

        for (int i = 0; i < initial_deep; i++) {
            if (!result.isEmpty()) {

                if ((initial_deep % 2) == 0 && i == 0) {
                    Collections.sort(result, (c1, c2) -> {
                        if (c1.value < c2.value) return -1;
                        else if (c1.value == c2.value) return 0;
                        else return 1;
                    });
                }
                else {
                    Collections.sort(result, (c1, c2) -> {
                        if (c1.value > c2.value) return -1;
                        else if (c1.value == c2.value) return 0;
                        else return 1;
                    });
                }

                best_value = result.get(0).value;

                for (item = 0; item < result.size(); item++) {
                    if (result.get(item).value < best_value) break;
                }

                System.out.println(result.size());
                if (item > 0) item = rnd.nextInt(item);

                System.out.printf("Deep %d nodes : %d best: %d nodes: %d %d %d ", i, deep_node[i], result.get(0).value, result.get(0).deep_node[0], result.get(0).deep_node[1], result.get(0).deep_node[2]);
                System.out.printf("%c%d->%c%d%n", ('A' + result.get(0).move.x), result.get(0).move.y + 1, ('A' + result.get(0).move.x2), result.get(0).move.y2 + 1);

                if (i < initial_deep - 1) {
                    node = result.get(item).deep_node[i + 1];
                    result.clear();

                    for (int i2 = 0; i2 < calculated[i + 1].size(); i2++) {
                        if (calculated[i + 1].get(i2).deep_node[i + 1] == node) result.add(calculated[i + 1].get(i2));
                    }
                }
            }
            else result = calculated[i + 1];
        }

        if (result.isEmpty()) {
            System.out.println("Patti");
            return move[0];
        }
        System.out.printf("Choosen nodes: %d %d %d%n%n", result.get(0).deep_node[0], result.get(0).deep_node[1], result.get(0).deep_node[2]);
        return result.get(0).move;
        //return (temp_value > best_value || (temp_value == best_value && rnd.nextBoolean()));
    }

    private boolean countPositionValue(BoardTable[][] table, LogicRules rules, int xx, int yy, int deep) {

        int value = 0;
        int piece1 = 0;
        int piece2 = 0;
        boolean cont = true;

        for(int y = 0; y < 8; y++) {
            for(int x = 0; x < 8; x++) {
                if (table[x][y].piece > NONE) {
                    if (table[x][y].piece_color == rules.cur_player) piece1 += 1;//piece_value[table[x][y].piece];
                    else piece2 += 1;//piece_value[table[x][y].piece];
                }
            }
        }

        value += piece1 - piece2;

        if (rules.checkmate && rules.cur_player == logic.rules.cur_player) value += 30;

        int[] d_nodes = new int[initial_deep];
        for (int i = 0; i < initial_deep; i++) {
            d_nodes[i] = deep_node[i];
        }

        calculated[deep].add(new Calculated(d_nodes, value, new Move(move[0].x, move[0].y, move[0].x2, move[0].y2, 0)));
        ++move_counter;

        //if (rules.cur_player != logic.rules.cur_player && value > best_value) cont = false;
        return cont;

        //System.out.printf("%c%d->%c%d value:%d %d %d %n", ('A' + move[0].x), move[0].y + 1, ('A' + move[0].x2), move[0].y2 + 1, value, deep_node[0], deep_node[1]);
    }
}
