package Game;

import static Piece.Piece.*;
import static Game.GameLogic.*;

import Main.Chess;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class Game extends GameLogic {

    private int x, y;
    private final int[] pieces_win;
    private int replay_count;
    private boolean state;

    private enum GameType {
        PLAY,
        REPLAY
    }

    private GameType game_state;
    private final Vector<Move> moves;
    private TextArea infoText;
    private Timer checkTimer;
    private Chess parent;

    public Game(Chess parent) {

        this.parent = parent;
        this.pieces_win = new int[2];
        this.moves = new Vector<>(64);

        infoText = new TextArea();
        infoText.setFont(Font.font("Consolas", SQ_SIZE / 4));
        infoText.setMaxSize(SQ_SIZE * 3, SQ_SIZE * 8);
        infoText.setMinHeight(SQ_SIZE * 8);
        infoText.setEditable(false);
        infoText.setWrapText(true);

        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (checkmate || game_state == GameType.REPLAY) return;
            mouseClick((int)(event.getSceneX() - canvas.getLayoutX()),
                           (int)(event.getSceneY() - canvas.getLayoutY()));
        });

        checkTimer = new Timer();
    }

    public void initGame() {

        this.x = 0;
        this.y = 0;
        this.state = false;
        this.pieces_win[WHITE] = 0;
        this.pieces_win[BLACK] = 0;

        this.game_state = GameType.PLAY;
        this.replay_count = 0;
        this.infoText.clear();

        initLogic();
        initBoard();
        drawBoard();
    }

    public Canvas getCanvas() {
        return canvas;
    }
    public TextArea getInfoBox() {
        return infoText;
    }
    public int getGameWidth() {
        return SQ_SIZE * 14;
    }
    public int getGameHeight() {
        return SQ_SIZE * 10;
    }
    public int getMoveCount() {return replay_count;}
    public void pauseGame() {game_state = GameType.REPLAY;}

    public void mouseClick(int mx, int my) {

        if (!choose_piece) {
            if (mx > SQ_SIZE && mx < SQ_SIZE + 8 * SQ_SIZE && my > SQ_SIZE && my < SQ_SIZE + 8 * SQ_SIZE) {
                mx = (mx - SQ_SIZE) / SQ_SIZE;
                my = (my - SQ_SIZE) / SQ_SIZE;

                setXY(mx, my);
                if (choose_piece) drawChooseBox(cur_player);
            }
        }
        else choosePiece(mx, my);
    }

    public void startReplay() {
        initGame();
        game_state = GameType.REPLAY;
    }

    public void replayStep() {

        int x = moves.get(replay_count).x;
        int y = moves.get(replay_count).y;
        int x2 = moves.get(replay_count).x2;
        int y2 = moves.get(replay_count).y2;
        int pcs = moves.get(replay_count).piece;

        board_table[GAME][x][y].piece = NONE;

        switch(pcs) {

            case (KING + 1):
                board_table[GAME][0][y].piece = NONE;
                board_table[GAME][0][y].piece = NONE;
                board_table[GAME][3][y].piece = ROOK;
                board_table[GAME][3][y].piece_color = (replay_count + 1) % 2;
                pcs = KING;
                break;

            case (KING + 2):
                board_table[GAME][7][y].piece = NONE;
                board_table[GAME][7][y].piece = NONE;
                board_table[GAME][5][y].piece = ROOK;
                board_table[GAME][5][y].piece_color = (replay_count + 1) % 2;
                pcs = KING;
                break;

            case (KING + 3):
                removePiece(x2, y2 - 1);
                pcs = PAWN;
                break;

            case (KING + 4):
                removePiece(x2, y2 + 1);
                pcs = PAWN;
                break;

            default:
                removePiece(x2, y2);
        }

        board_table[GAME][x2][y2].piece = pcs;
        board_table[GAME][x2][y2].piece_color = (replay_count + 1) % 2;

        addMove(x, y, x2, y2);
        drawBoard();

        if (replay_count == moves.size()) {
            game_state = GameType.PLAY;
            parent.gameEnded("Replay Again");
        }
    }

    private void choosePiece(int mx, int my) {
        if (mx < box_x || mx > box_x + BOX_W || my < box_y || my > box_y + SQ_SIZE) return;

        mx = (mx - box_x) / SQ_SIZE;

        board_table[GAME][x][y].piece = KNIGHT + mx;
        moves.get(replay_count - 1).piece = KNIGHT + mx;
        choose_piece = false;
        lookPlayerCheck(x, y);
        clearBoard();

        cur_player ^= 1;
        drawBoard();
    }

    private void setTimer() {
        checkTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                drawBoard();
            }
        }, 3000);
    }

    private void addMove(int x, int y, int x2, int y2) {
        String text = Character.toString('A' + x);
        text += (y + 1) + ":";
        text += Character.toString('A' + x2);
        text += (y2 + 1) + " ";

        infoText.appendText(text);

        if (game_state == GameType.PLAY){
            Move mov = new Move(x, y, x2, y2, board_table[GAME][this.x][this.y].piece);
            moves.add(mov);
        }

        ++replay_count;
    }

    private void setXY(int x, int y) {
        if (!state && board_table[GAME][x][y].piece == -1) return;

        if (board_table[GAME][x][y].square_check < 2 && state) {
            clearBoard();
            drawBoard();
            this.state = false;
        }

        if (state) {

            removePiece(x, y);
            addMove(this.x, this.y, x, y);

            board_table[GAME][x][y].piece = board_table[GAME][this.x][this.y].piece;
            board_table[GAME][x][y].piece_color = cur_player;
            board_table[GAME][this.x][this.y].piece = NONE;
            board_table[GAME][this.x][this.y].piece_color = NONE;
            this.x = x;
            this.y = y;

            lookSpecialMoves(x, y);
            if (lookPawnPassant(x, y)) {
                addPiece(PAWN, cur_player ^ 1);
                moves.get(replay_count - 1).piece = KING + 3 + cur_player;
            }

            lookPlayerCheck(x, y);

            int castling = movePossibleCastling(x, y);
            if (castling > 0) moves.get(replay_count - 1).piece = castling + 1;

            clearBoard();
            drawBoard();

            this.state = false;

            if (!choose_piece) cur_player ^= 1;
            if (check) {
                if (!checkmate) {
                    drawText("Check");
                    setTimer();
                }
                else {
                    drawText("Checkmate");
                    parent.gameEnded("Replay Game");
                }
            }
        }
        else if (board_table[GAME][x][y].piece_color == cur_player) {

            board_table[GAME][x][y].square_check = 1;
            this.x = x;
            this.y = y;
            this.state = true;

            lookIllegalMoves(x, y);
            drawBoard();
        }
    }

    private void removePiece(int x, int y) {

        if (board_table[GAME][x][y].piece > NONE) {

            addPiece(board_table[GAME][x][y].piece, board_table[GAME][x][y].piece_color);
            board_table[GAME][x][y].piece_color = NONE;
            board_table[GAME][x][y].piece = NONE;
        }
    }

    private void addPiece(int pcs, int col) {

        if (col == BLACK) {
            piece[pcs].draw(gc, SQ_SIZE - (SQ_SIZE / 8) + pieces_win[WHITE] * (SQ_SIZE / 4),
                    SQ_SIZE * 9 + SQ_SIZE / 3, SQ_SIZE / 2, SQ_SIZE / 2, BLACK);
            ++pieces_win[WHITE];
        } else {
            ++pieces_win[BLACK];
            piece[pcs].draw(gc, SQ_SIZE * 9 - pieces_win[BLACK] * (SQ_SIZE / 4) - (SQ_SIZE / 8),
                    SQ_SIZE * 9 + SQ_SIZE / 3, SQ_SIZE / 2, SQ_SIZE / 2, WHITE);
        }
    }
}