package Game;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Board extends VBox {
    private Canvas canvas;
    private GraphicsContext gc;

    static final int SQ_SIZE = 64;
    static final int SQ_PAD = SQ_SIZE / 2;
    static final int DIF_PIECES = 6;

    private BoardTable[][] board_table;
    private final Select select;
    private final Piece[] piece = new Piece[DIF_PIECES];

    public static class BoardTable {
        public int square_check;
        public int square_color;
        public int piece;
        public int piece_color;

        BoardTable() {
            this.square_check = 0;
            this.square_color = 0;
            this.piece_color = -1;
            this.piece = -1;
        }
    }

    private class Select {
        private int x, y;
        private boolean state;

        Select() {
            this.x = 0;
            this.y = 0;
            this.state = false;
        }

        public int getX() {return x;}
        public int getY() {return y;}
        public boolean isState() {return state;}

        public void setXY(int x, int y) {
            if (!state && board_table[x][y].piece == -1) return;

            if (board_table[x][y].square_check != 2 && state) {
                clearBoard();
                setState(false);
            }
            else if (state) {

                board_table[x][y].piece = board_table[this.x][this.y].piece;
                board_table[x][y].piece_color = board_table[this.x][this.y].piece_color;
                board_table[this.x][this.y].piece = -1;
                board_table[this.x][this.y].piece_color = -1;
                this.x = x;
                this.y = y;
                clearBoard();
                setState(false);
            }
            else {
                board_table[x][y].square_check = 1;
                piece[board_table[x][y].piece].lookMoves(board_table, x, y);
                this.x = x;
                this.y = y;
                setState(true);
            }
        }

        public void setState(boolean state) {
            this.state = state;
        }
    }

    Board() {

        canvas = new Canvas();
        canvas.setWidth(SQ_SIZE * 9);
        canvas.setHeight(SQ_SIZE * 9);

        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseClick((int)event.getSceneX(), (int)event.getSceneY());
            }
        });

        gc = canvas.getGraphicsContext2D();

        select = new Select();

        piece[Piece.PAWN] = new Pawn();
        piece[Piece.KNIGHT] = new Knight();
        piece[Piece.BISHOP] = new Bishop();

        initBoard();

        getChildren().add(canvas);
    }

    private void initBoard() {

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

        int[] white_pcs = {Piece.PAWN, Piece.KNIGHT, Piece.BISHOP, Piece.PAWN, Piece.PAWN, Piece.BISHOP, Piece.KNIGHT, Piece.PAWN,
                           Piece.PAWN, Piece.PAWN, Piece.PAWN, Piece.PAWN, Piece.PAWN, Piece.PAWN, Piece.PAWN, Piece.PAWN};

        int[] black_pcs = {Piece.PAWN, Piece.PAWN, Piece.PAWN, Piece.PAWN, Piece.PAWN, Piece.PAWN, Piece.PAWN, Piece.PAWN,
                           Piece.PAWN, Piece.KNIGHT, Piece.BISHOP, Piece.PAWN, Piece.PAWN, Piece.BISHOP, Piece.KNIGHT, Piece.PAWN};

        int x = 0;
        int y = 0;

        for (int i = 0; i < 16; i++) {
            board_table[x][y].piece = white_pcs[i];
            board_table[x][y].piece_color = Piece.WHITE;

            board_table[x][y + 6].piece = black_pcs[i];
            board_table[x][y + 6].piece_color = Piece.BLACK;

            if (++x == 8) {
                x = 0;
                ++y;
            }
        }
    }

    private void clearBoard() {
        for (int y = 0; y < 8; y++)
        {
            for (int x = 0; x < 8; x++) {
                board_table[x][y].square_check = 0;
            }
        }
    }

    public void draw() {
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

                if (board_table[x][y].square_check == 2) {
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

    public void mouseClick(int mx, int my) {

        if (mx > SQ_PAD && mx < SQ_PAD + 8 * SQ_SIZE && my > SQ_PAD && my < SQ_PAD + 8 * SQ_SIZE) {
            mx = (mx - SQ_PAD) / SQ_SIZE;
            my = (my - SQ_PAD) / SQ_SIZE;

            select.setXY(mx, my);
            draw();
        }
    }
}
