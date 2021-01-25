package Game;

import static Piece.Piece.*;

import Piece.*;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class Board {
    protected Canvas canvas;
    protected GraphicsContext gc;

    static final int SQ_SIZE = 64;
    static final int BOARD_X = SQ_SIZE - (SQ_SIZE / 8);
    static final int BOARD_W = 8 * SQ_SIZE + SQ_SIZE / 4;
    static final int BOX_W = 4 * SQ_SIZE;
    static final int OVAL_SIZE = SQ_SIZE / 3;

    static final int DIF_PIECES = 6;

    protected BoardTable[][] board_table;
    protected final Piece[] piece = new Piece[DIF_PIECES];
    protected int box_x;
    protected int box_y;

    public static class BoardTable {
        public boolean square_pawn;
        //public boolean square_checkmate;
        public int square_check;
        public int piece;
        public int piece_color;

        BoardTable() {
            this.square_pawn = false;
            //this.square_checkmate = false;
            this.square_check = 0;
            this.piece_color = NONE;
            this.piece = NONE;
        }

        public void copyBoard(BoardTable board) {
            this.square_pawn = board.square_pawn;
            this.square_check = board.square_check;
            this.piece_color = board.piece_color;
            this.piece = board.piece;
        }
    }

    Board() {

        canvas = new Canvas();
        canvas.setWidth(SQ_SIZE * 10);
        canvas.setHeight(SQ_SIZE * 10);

        gc = canvas.getGraphicsContext2D();

        piece[PAWN] = new Pawn();
        piece[KNIGHT] = new Knight();
        piece[BISHOP] = new Bishop();
        piece[ROOK] = new Rook();
        piece[QUEEN] = new Queen();
        piece[KING] = new King();

        board_table = new BoardTable[8][8];
        newBoard(board_table);
    }

    protected void initBoard() {

        clearBoard(board_table);
        clearBoardPieces(board_table);

        int[] black_pcs = {ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK,
                           PAWN, PAWN  , PAWN  , PAWN , PAWN, PAWN  , PAWN  , PAWN};

        int[] white_pcs = {PAWN, PAWN  , PAWN  , PAWN , PAWN, PAWN  , PAWN  , PAWN,
                           ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK};

        int x = 0;
        int y = 0;

        for (int i = 0; i < 16; i++) {
            board_table[x][y].piece = black_pcs[i];
            board_table[x][y].piece_color = BLACK;

            board_table[x][y + 6].piece = white_pcs[i];
            board_table[x][y + 6].piece_color = WHITE;

            if (++x == 8) {
                x = 0;
                ++y;
            }
        }

        /*board_table[4][5].piece = KING;
        board_table[4][5].piece_color = WHITE;

        board_table[4][1].piece = PAWN;
        board_table[4][1].piece_color = WHITE;

        board_table[0][1].piece = QUEEN;
        board_table[0][1].piece_color = BLACK;

        board_table[3][4].piece = KNIGHT;
        board_table[3][4].piece_color = BLACK;*/

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    protected void newBoard(BoardTable[][] table) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                table[x][y] = new BoardTable();
            }
        }
    }

    protected void clearBoard(BoardTable[][] table) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                table[x][y].square_check = 0;
            }
        }
    }

    protected void clearBoardPieces(BoardTable[][] table) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                table[x][y].piece = NONE;
                table[x][y].piece_color = NONE;
            }
        }
    }

    protected void clearBoardPawn(BoardTable[][] table) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                table[x][y].square_pawn = false;
            }
        }
    }

    /*protected void clearBoardCheckMate(BoardTable[][] table) {
        for (int y = 0; y < 8; y++)
        {
            for (int x = 0; x < 8; x++) {
                table[x][y].square_checkmate = false;
            }
        }
    }*/

    protected void copyBoard(BoardTable[][] src, BoardTable[][] dest) {
        for (int y = 0; y < 8; y++)
        {
            for (int x = 0; x < 8; x++) {
                dest[x][y].copyBoard(src[x][y]);
            }
        }
    }

    protected void setChooseBoxXY(int x, int y) {

        x = BOARD_X + x * SQ_SIZE + (SQ_SIZE / 2) - (SQ_SIZE * 2);
        y = BOARD_X + y * SQ_SIZE + SQ_SIZE / 8;

        if (x < BOARD_X + SQ_SIZE / 8) x = BOARD_X + SQ_SIZE / 8;
        else if (x + BOX_W + SQ_SIZE / 4 > BOARD_X + BOARD_W) x = (BOARD_X + BOARD_W) - (BOX_W + SQ_SIZE / 4);

        box_x = x;
        box_y = y;
    }

    protected void drawText(String text) {
        gc.setFont(Font.font("Verdana", SQ_SIZE / 2));
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, canvas.getWidth() / 2, canvas.getHeight() / 2);
    }

    protected void drawChooseBox(int col) {
        gc.setFill(Color.GRAY);
        gc.fillRect(box_x - SQ_SIZE / 8, box_y - SQ_SIZE / 8, BOX_W + SQ_SIZE / 4, SQ_SIZE + SQ_SIZE / 4);

        gc.setFill(Color.GOLD);
        gc.fillRect(box_x, box_y, BOX_W, SQ_SIZE);

        for (int i = 0; i < 4; i++) {
            piece[KNIGHT + i].draw(gc, box_x + SQ_SIZE * i, box_y, SQ_SIZE, SQ_SIZE, col);
        }
    }

    protected void drawPositionText() {

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Verdana", (float)SQ_SIZE / 4));

        for (int i = 0; i < 8; i++) {
            gc.fillText(Character.toString('A' + i), SQ_SIZE + SQ_SIZE / 2 + i * SQ_SIZE - 4, SQ_SIZE - SQ_SIZE / 4);
        }

        for (int i = 0; i < 8; i++) {
            gc.fillText(Integer.toString(i + 1), SQ_SIZE - SQ_SIZE / 2, SQ_SIZE + SQ_SIZE / 2 + i * SQ_SIZE + 4);
        }
    }

    protected void drawBoard(BoardTable[][] table) {

        gc.setFill(Color.GOLD);
        gc.fillRect(BOARD_X, BOARD_X, BOARD_W, BOARD_W);

        Color[]  color = {Color.GOLDENROD, Color.BROWN};
        int col = 1;
        int xx = SQ_SIZE;
        int yy = SQ_SIZE;

        for (int y = 0; y < 8; y++)
        {
            for (int x = 0; x < 8; x++)
            {
                if (table[x][y].square_check == 1) {
                    gc.setFill(Color.GRAY);
                    gc.fillRect(xx, yy, SQ_SIZE, SQ_SIZE);
                    gc.setFill(color[col]);
                    gc.fillRect(xx + 4, yy + 4, SQ_SIZE - 8, SQ_SIZE - 8);
                }
                else {
                    gc.setFill(color[col]);
                    gc.fillRect(xx, yy, SQ_SIZE, SQ_SIZE);
                }

                if (table[x][y].piece > NONE) {
                    piece[table[x][y].piece].draw(gc, xx, yy, SQ_SIZE, SQ_SIZE, table[x][y].piece_color);
                }

                if (table[x][y].square_check > 3) {
                    gc.setFill(Color.BLUE);
                    gc.fillOval(xx + OVAL_SIZE, yy + OVAL_SIZE, OVAL_SIZE, OVAL_SIZE);
                }
                else if (table[x][y].square_check > 1) {
                    gc.setFill(Color.GREEN);
                    gc.fillOval(xx + OVAL_SIZE, yy + OVAL_SIZE, OVAL_SIZE, OVAL_SIZE);
                }

                col ^= 1;
                xx += SQ_SIZE;
            }
            col ^= 1;
            xx = SQ_SIZE;
            yy += SQ_SIZE;
        }

    }
}
