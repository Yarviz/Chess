package Game;

import static Piece.Piece.*;
import static Game.Board.*;

public class Computer {

    private int player;
    private GameLogic logic;

    public Computer(GameLogic logic) {
        this.logic = logic;
    }

    public void makeMove(int player) {
        this.player = player;

        calculateMove(player, 2);
    }

    private void calculateMove(int col, int deep) {

        for(int y = 0; y < 8; y++) {
            for(int x = 0; x < 8; x++) {
                if (logic.board_table[GAME][x][y].piece_color == col && logic.board_table[GAME][x][y].piece > NONE) {
                    logic.piece[logic.board_table[GAME][x][y].piece].lookMoves(logic.board_table[GAME], x, y);
                    testMoves(col, deep - 1);
                }
            }
        }
    }

    private void testMoves(int col, int deep) {
        for(int y = 0; y < 8; y++) {
            for(int x = 0; x < 8; x++) {
                if (logic.board_table[GAME][x][y].square_check > 1) {
                    logic.piece[logic.board_table[GAME][x][y].piece].lookMoves(logic.board_table[GAME], x, y);

                }
            }
        }
    }
}
