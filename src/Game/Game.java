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
    private Computer ai;

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
            if (rules.checkmate || game_state == GameType.REPLAY) return;
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
        this.ai = new Computer(getLogic());

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

        if (!rules.choose_piece) {
            if (mx > SQ_SIZE && mx < SQ_SIZE + 8 * SQ_SIZE && my > SQ_SIZE && my < SQ_SIZE + 8 * SQ_SIZE) {
                mx = (mx - SQ_SIZE) / SQ_SIZE;
                my = (my - SQ_SIZE) / SQ_SIZE;

                setXY(mx, my);
                if (rules.choose_piece) drawChooseBox(rules.cur_player);
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

        board_table[x][y].piece = NONE;

        switch(pcs) {

            case (KING + 1):
                board_table[0][y].piece = NONE;
                board_table[0][y].piece = NONE;
                board_table[3][y].piece = ROOK;
                board_table[3][y].piece_color = (replay_count + 1) % 2;
                pcs = KING;
                break;

            case (KING + 2):
                board_table[7][y].piece = NONE;
                board_table[7][y].piece = NONE;
                board_table[5][y].piece = ROOK;
                board_table[5][y].piece_color = (replay_count + 1) % 2;
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

        board_table[x2][y2].piece = pcs;
        board_table[x2][y2].piece_color = (replay_count + 1) % 2;

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

        board_table[x][y].piece = KNIGHT + mx;
        moves.get(replay_count - 1).piece = KNIGHT + mx;
        rules.choose_piece = false;
        lookPlayerCheck(board_table, rules, x, y);
        clearBoard(board_table);

        rules.cur_player ^= 1;
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

    private void setComputerTimer() {
        checkTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Move ai_move = ai.makeMove(rules.cur_player);
                makeMove(ai_move.x, ai_move.y, ai_move.x2, ai_move.y2);
            }
        }, 50);
    }

    private void addMove(int x, int y, int x2, int y2) {
        String text = Character.toString('A' + x);
        text += (y + 1) + ":";
        text += Character.toString('A' + x2);
        text += (y2 + 1) + " ";

        infoText.appendText(text);

        if (game_state == GameType.PLAY){
            Move mov = new Move(x, y, x2, y2, board_table[this.x][this.y].piece);
            moves.add(mov);
        }

        ++replay_count;
    }

    private void setXY(int x, int y) {

        if (!state && board_table[x][y].piece == -1) return;

        if (board_table[x][y].square_check < 2 && state) {
            clearBoard(board_table);
            drawBoard();
            this.state = false;
        }

        if (state) {

            makeMove(this.x, this.y, x, y);
            this.x = x;
            this.y = y;

            if (rules.cur_player == BLACK && !rules.checkmate) {
                drawBoard();
                drawText("Computer Turn");

                setComputerTimer();
            }
        }
        else if (board_table[x][y].piece_color == rules.cur_player) {

            board_table[x][y].square_check = 1;
            this.x = x;
            this.y = y;
            this.state = true;

            lookMoves(board_table, rules, x, y);
            drawBoard();
        }
    }

    private void makeMove(int x, int y, int x2, int y2) {

        removePiece(x2, y2);
        addMove(x, y, x2, y2);

        board_table[x2][y2].piece = board_table[x][y].piece;
        board_table[x2][y2].piece_color = rules.cur_player;
        board_table[x][y].piece = NONE;
        board_table[x][y].piece_color = NONE;

        lookSpecialMoves(board_table, rules, x2, y2);
        if (lookPawnPassant(board_table, rules, x2, y2)) {
            addPiece(PAWN, rules.cur_player ^ 1);
            moves.get(replay_count - 1).piece = KING + 3 + rules.cur_player;
        }

        lookPlayerCheck(board_table, rules, x2, y2);

        int castling = movePossibleCastling(board_table, rules, x2, y2);
        if (castling > 0) moves.get(replay_count - 1).piece = castling + 1;

        clearBoard(board_table);
        drawBoard();

        this.state = false;

        if (!rules.choose_piece) rules.cur_player ^= 1;
        if (rules.check) {
            if (!rules.checkmate) {
                drawText("Check");
                setTimer();
            }
            else {
                drawText("Checkmate");
                parent.gameEnded("Replay Game");
            }
        }
    }

    private void removePiece(int x, int y) {

        if (board_table[x][y].piece > NONE) {

            addPiece(board_table[x][y].piece, board_table[x][y].piece_color);
            board_table[x][y].piece_color = NONE;
            board_table[x][y].piece = NONE;
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