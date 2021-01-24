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
    private int initial_ply;

    private final int MAX_PLY = 7;

    private final Move[] move;
    private final int[] piece_value;
    private final int[] ply;
    private final ArrayList<Calculated> calculated;

    private class Calculated {
        public final int[] ply;
        public int value;
        public Move move;

        Calculated(int[] plys, int value, Move move) {
            this.ply = plys;
            this.value = value;
            this.move = move;
        }
    }

    public Computer(GameLogic logic) {
        this.logic = logic;

        piece_value = new int[6];
        ply = new int[MAX_PLY];

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
        //initial_ply = 3;
        countPieces(logic.board_table);

        for (int i = 0; i < initial_ply + 1; i++) {
            ply[i] = 0;
            calculated.clear();
        }

        calculateMove(logic.board_table, logic.rules, 0);
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

        if (pieces < 8) initial_ply = 4;
            else initial_ply = 2;
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

                        if (deep == 0) {
                            move[0].x = x;
                            move[0].y = y;
                        }
                        testMoves(temp_board, temp_rules, x, y, deep);
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

                    if (deep == 0) {
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
                    ++ply[deep];
                    ++move_counter;

                    if (deep < initial_ply && !temp_rules.checkmate/* && cont*/) {
                        temp_rules.cur_player ^= 1;
                        calculateMove(temp_board, temp_rules, deep + 1);
                    }
                    countPositionValue(temp_board, temp_rules, new_move, deep);
                }
            }
        }
    }

    private Move countBestValue()
    {
        if (ply[0] == 0) {
            System.out.println("Computer Draw");
            return new Move(-1, -1, -1, -1,0);
        }

        int item = 0;
        int nodes_count;
        int best_value;
        List<Calculated> result = calculated;
        List<Calculated> calc_result = new ArrayList<>();

        for (int i = initial_ply; i > 0; i--) {
            final int node = i - 1;
            if (i % 2 == 1) {
                Collections.sort(result, Comparator.comparing((Calculated c) -> c.ply[node]).thenComparing(c -> c.value));
            }
            else {
                Collections.sort(result, Comparator.comparing((Calculated c) -> c.ply[node]).thenComparing(c -> c.value, Comparator.reverseOrder()));
            }

            nodes_count = result.get(0).ply[node];
            calc_result.clear();

            for(int i2 = 0; i2 < result.size(); i2++) {
                if (result.get(i2).ply[node] == nodes_count) {
                    calc_result.add(result.get(i2));
                    if (++nodes_count > ply[node]) break;
                }
            }

            result = new ArrayList<>(calc_result);
        }

        Collections.sort(result, Comparator.comparing((Calculated c) -> c.value).reversed());

        /*for (Calculated c: result) {
            System.out.printf("value: %d nodes: %d %d %d ", c.value, c.ply[0], c.ply[1], c.ply[2]);
            System.out.printf("%c%d->%c%d%n", ('A' + c.move.x), c.move.y + 1, ('A' + c.move.x2), c.move.y2 + 1);
        }
        System.out.println("");*/

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
            if (rules.cur_player == logic.rules.cur_player) value += 30;
                else value -= 30;
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
                if (rules.cur_player == logic.rules.cur_player) value += piece_value[piece1];
                    else value -= piece_value[piece1];
            }
            else if (rules.cur_player == logic.rules.cur_player) value += 10;
                else value -= 10;
        }

        int[] ply_nodes = new int[initial_ply + 1];
        for (int i = 0; i < initial_ply + 1; i++) {
            ply_nodes[i] = ply[i];
        }

        calculated.add(new Calculated(ply_nodes, value, new Move(move[0].x, move[0].y, move[0].x2, move[0].y2, rules.cur_player)));

        return cont;
    }
}
