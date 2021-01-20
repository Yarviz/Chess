package Game;

import static Piece.Piece.*;
import static Game.Board.*;

public class Computer {

    private GameLogic logic;

    public Computer(GameLogic logic) {
        this.logic = logic;

    }

    public void makeMove(int player) {

        calculateMove(logic.board_table, player, 2);
    }

    private void calculateMove(BoardTable[][] table, int col, int deep) {

        BoardTable[][] temp_board = new BoardTable[8][8];
        logic.newBoard(temp_board);
        logic.copyBoard(table, temp_board);

        for(int y = 0; y < 8; y++) {
            for(int x = 0; x < 8; x++) {
                if (temp_board[x][y].piece_color == col && temp_board[x][y].piece > NONE) {

                    logic.clearBoard(temp_board);
                    logic.lookMoves(temp_board, logic.rules, x, y);

                    testMoves(temp_board, x, y, deep - 1);
                }
            }
        }
    }

    private void testMoves(BoardTable[][] table, int xx, int yy, int deep) {

        BoardTable[][] temp_board = new BoardTable[8][8];
        logic.newBoard(temp_board);

        for(int y = 0; y < 8; y++) {
            for(int x = 0; x < 8; x++) {
                if (table[x][y].square_check > 1) {

                    logic.copyBoard(table, temp_board);

                    temp_board[x][y].piece = temp_board[xx][yy].piece;
                    temp_board[x][y].piece_color = temp_board[xx][yy].piece_color;
                    temp_board[xx][yy].piece = NONE;
                    temp_board[xx][yy].piece_color = NONE;

                    logic.lookSpecialMoves(temp_board, logic.rules, x, y);
                    logic.lookPawnPassant(temp_board, logic.rules, x, y);

                    logic.lookPlayerCheck(temp_board, logic.rules, x, y);

                    logic.movePossibleCastling(temp_board, logic.rules, x, y);
                    logic.clearBoard(temp_board);
                }
            }
        }
    }
}
