package Game;

import static Game.Piece.*;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


public class Board extends VBox {
    protected Canvas canvas;
    protected GraphicsContext gc;

    static final int SQ_SIZE = 64;
    static final int SQ_PAD = SQ_SIZE / 2;
    static final int DIF_PIECES = 6;

    protected BoardTable[][] board_table;
    protected final Piece[] piece = new Piece[DIF_PIECES];

    public static class BoardTable {
        public boolean square_pawn;
        public int square_check;
        public int square_color;
        public int piece;
        public int piece_color;

        BoardTable() {
            this.square_pawn = false;
            this.square_check = 0;
            this.square_color = 0;
            this.piece_color = -1;
            this.piece = -1;
        }
    }

    Board() {

        canvas = new Canvas();
        canvas.setWidth(SQ_SIZE * 9);
        canvas.setHeight(SQ_SIZE * 9);

        gc = canvas.getGraphicsContext2D();

        piece[PAWN] = new Pawn();
        piece[KNIGHT] = new Knight();
        piece[BISHOP] = new Bishop();
        piece[ROCK] = new Rock();
        piece[QUEEN] = new Queen();
        piece[KING] = new King();

        initBoard();

        getChildren().add(canvas);
    }

    protected void initBoard() {

        board_table = new BoardTable[8][8];

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                board_table[x][y] = new BoardTable();
            }
        }

        clearBoard();

        int col = 1;
        for (int y = 0; y < 8; y++)
        {
            for (int x = 0; x < 8; x++)
            {
                col = (col + 1) % 2;
                board_table[x][y].square_color = col;
            }
            col = (col + 1) % 2;
        }

        int[] white_pcs = {ROCK, KNIGHT, BISHOP, KING, QUEEN, BISHOP, KNIGHT, ROCK,
                           PAWN, PAWN  , PAWN  , PAWN , PAWN, PAWN  , PAWN  , PAWN};

        int[] black_pcs = {PAWN, PAWN  , PAWN  , PAWN , PAWN, PAWN  , PAWN  , PAWN,
                           ROCK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROCK};

        int x = 0;
        int y = 0;

        for (int i = 0; i < 16; i++) {
            board_table[x][y].piece = white_pcs[i];
            board_table[x][y].piece_color = WHITE;

            board_table[x][y + 6].piece = black_pcs[i];
            board_table[x][y + 6].piece_color = BLACK;

            if (++x == 8) {
                x = 0;
                ++y;
            }
        }
    }

    protected void clearBoard() {
        for (int y = 0; y < 8; y++)
        {
            for (int x = 0; x < 8; x++) {
                board_table[x][y].square_check = 0;
            }
        }
    }

    protected void clearBoardPawn() {
        for (int y = 0; y < 8; y++)
        {
            for (int x = 0; x < 8; x++) {
                board_table[x][y].square_pawn = false;
            }
        }
    }

    protected void drawBoard() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.GOLD);
        gc.fillRect(SQ_PAD - (SQ_PAD / 8), SQ_PAD - (SQ_PAD / 8),
                    8 * SQ_SIZE + SQ_PAD / 4, 8 * SQ_SIZE + SQ_PAD / 4);

        Color[]  col = {Color.GOLDENROD, Color.BROWN};
        int xx = SQ_PAD;
        int yy = SQ_PAD;

        for (int y = 0; y < 8; y++)
        {
            for (int x = 0; x < 8; x++)
            {
                if (board_table[x][y].square_check == 1) {
                    gc.setFill(Color.GRAY);
                    gc.fillRect(xx, yy, SQ_SIZE, SQ_SIZE);
                    gc.setFill(col[board_table[x][y].square_color]);
                    gc.fillRect(xx + 4, yy + 4, SQ_SIZE - 8, SQ_SIZE - 8);
                }
                else {
                    gc.setFill(col[board_table[x][y].square_color]);
                    gc.fillRect(xx, yy, SQ_SIZE, SQ_SIZE);
                }

                if (board_table[x][y].piece > -1) {
                    piece[board_table[x][y].piece].draw(gc, xx, yy, board_table[x][y].piece_color);
                }

                if (board_table[x][y].square_check > 1) {
                    gc.setFill(Color.GREEN);
                    gc.fillOval(xx + SQ_SIZE / 3, yy + SQ_SIZE / 3, SQ_SIZE / 3, SQ_SIZE / 3);
                }

                xx += SQ_SIZE;
            }
            xx = SQ_PAD;
            yy += SQ_SIZE;
        }

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Verdana", SQ_SIZE / 4));

        for (int i = 0; i < 8; i++) {
            gc.fillText(Character.toString('A' + i), SQ_PAD + SQ_SIZE / 2 + i * SQ_SIZE - 4, SQ_PAD / 2);
        }

        for (int i = 0; i < 8; i++) {
            gc.fillText(Integer.toString(i + 1), SQ_PAD / 4, SQ_PAD + SQ_SIZE / 2 + i * SQ_SIZE + 4);
        }
    }
}
