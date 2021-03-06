package Game;

import static Piece.Piece.*;

import Main.Chess;
import javafx.application.Platform;
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
    private int game_end_state;
    private boolean state;
    private boolean animation_on;
    private Computer ai;

    public enum GameType {
        PLAY,
        REPLAY,
        HUMAN,
        COMPUTER
    }

    private GameType game_state;
    private GameType[] player;
    private final Vector<Move> moves;
    private TextArea infoText;
    private Timer timer;
    private Chess parent;
    private Animation animation;

    private class Animation {
        private final float[] x;
        private final float[] x2;
        private float y;
        private float y2;
        private int pcs_x;
        private int pcs_y;
        private int cast_x;
        private int pcs;
        private int color;
        private int castle;
        private boolean passant;

        private final float[] x_add;
        private float y_add;

        private boolean running;
        private final int STEP = 20;
        private int steps;
        private final Timer timer;

        Animation() {
            x = new float[2];
            x2 = new float[2];
            x_add = new float[2];
            timer = new Timer();

            this.running = false;
        }

        public void init(int x, int y, int x2, int y2, int piece, int color, int castling, boolean passant) {
            this.pcs_x = x2;
            this.pcs_y = y2;
            this.x[0] = x * SQ_SIZE + SQ_SIZE;
            this.x2[0] = x2 * SQ_SIZE + SQ_SIZE;
            this.y = y * SQ_SIZE + SQ_SIZE;
            this.y2 = y2 * SQ_SIZE + SQ_SIZE;
            this.pcs = piece;
            this.color = color;
            this.running = false;
            this.castle = castling;
            this.passant = passant;

            if (castling == 1) {
                this.x[1] = SQ_SIZE;
                this.x2[1] = 4 * SQ_SIZE;
                cast_x = 3;
                board_table[cast_x][pcs_y].piece = NONE;
                board_table[cast_x][pcs_y].piece_color = NONE;
            }
            else if (castling == 2) {
                this.x[1] = 8 * SQ_SIZE;
                this.x2[1] = 6 * SQ_SIZE;
                cast_x = 5;
                board_table[cast_x][pcs_y].piece = NONE;
                board_table[cast_x][pcs_y].piece_color = NONE;
            }

            if (passant) {
                int y_add = 1;
                if (color == BLACK) y_add = - 1;

                board_table[pcs_x][pcs_y + y_add].piece = PAWN;
                board_table[pcs_x][pcs_y + y_add].piece_color = color ^ 1;
            }

            calculateStep();
        }

        public void calculateStep() {
            x_add[0] = (x2[0] - x[0]) / STEP;
            y_add = (y2 - y) / STEP;
            steps = STEP;

            if (castle > 0) {
                x_add[1] = (x2[1] - x[1]) / STEP;
            }
        }

        public boolean isRunning() {
            return running;
        }

        public void cancel() {
            timer.cancel();
            timer.purge();
        }

        public void runAnimation() {
            running = true;

            if (steps-- > 0)
            {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            drawBoard(board_table);

                            x[0] += x_add[0];
                            y += y_add;

                            piece[pcs].draw(gc, (int)x[0], (int)y, SQ_SIZE, SQ_SIZE, color);
                            if (castle > 0) {
                                x[1] += x_add[1];
                                piece[ROOK].draw(gc, (int)x[1], (int)y, SQ_SIZE, SQ_SIZE, color);
                            }

                            runAnimation();
                        });
                    }
                }, 20);
            }
            else {
                running = false;

                if (board_table[pcs_x][pcs_y].piece > NONE) {
                    addPiece(board_table[pcs_x][pcs_y].piece, board_table[pcs_x][pcs_y].piece_color);
                }
                else if (passant) {
                    int y_add = 1;
                    if (color == BLACK) y_add = - 1;

                    board_table[pcs_x][pcs_y + y_add].piece = NONE;
                    board_table[pcs_x][pcs_y + y_add].piece_color = NONE;
                    addPiece(PAWN, color ^ 1);
                    moves.get(replay_count - 1).piece = KING + 3 + color;
                }

                board_table[pcs_x][pcs_y].piece = pcs;
                board_table[pcs_x][pcs_y].piece_color = color;

                if (castle > 0) {
                    board_table[cast_x][pcs_y].piece = ROOK;
                    board_table[cast_x][pcs_y].piece_color = color;
                }

                updateBoard();
            }
        }
    }

    public Game(Chess parent) {

        this.parent = parent;
        this.pieces_win = new int[2];
        this.moves = new Vector<>(64);
        this.ai = new Computer(getLogic());
        this.animation = new Animation();
        this.player = new GameType[2];

        infoText = new TextArea();
        infoText.setFont(Font.font("Consolas", SQ_SIZE / 4));
        infoText.setMaxSize(SQ_SIZE * 3 + (SQ_SIZE / 4), SQ_SIZE * 8);
        infoText.setMinHeight(SQ_SIZE * 8);
        infoText.setEditable(false);
        infoText.setWrapText(true);

        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (rules.checkmate || game_state == GameType.REPLAY || animation.isRunning()) return;
            mouseClick((int)(event.getSceneX() - canvas.getLayoutX()),
                           (int)(event.getSceneY() - canvas.getLayoutY()));
        });

        timer = new Timer();
    }

    public void initGame(int computer, boolean animation_on, GameType type) {

        this.x = 0;
        this.y = 0;
        this.state = false;
        this.pieces_win[WHITE] = 0;
        this.pieces_win[BLACK] = 0;

        this.game_state = type;
        this.animation_on = animation_on;
        this.replay_count = 0;
        this.infoText.clear();
        player[WHITE] = GameType.HUMAN;

        if (computer == 0) player[BLACK] = GameType.HUMAN;
            else player[BLACK] = GameType.COMPUTER;

        if (game_state == GameType.PLAY) {
            this.moves.clear();
            this.game_end_state = 0;
        }

        initLogic();
        initBoard();
        drawBoard(board_table);
        drawPositionText();

        if (player[WHITE] == GameType.COMPUTER && game_state == GameType.PLAY)
        {
            drawText("Computer Turn");
            setComputerTimer();
        }
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

                if (player[rules.cur_player] == GameType.HUMAN) setXY(mx, my);
                //if (rules.choose_piece) drawChooseBox(rules.cur_player);
            }
        }
        else choosePiece(mx, my);
    }

    public void startReplay() {
        initGame(0, false, GameType.REPLAY);
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
        drawBoard(board_table);

        if (replay_count == moves.size()) {
            game_state = GameType.PLAY;

            if (game_end_state == 0) drawText("Draw");
                else drawText("Checkmate");

            parent.gameEnded("Replay Again");
        }
    }

    private void choosePiece(int mx, int my) {
        if (mx < box_x || mx > box_x + BOX_W || my < box_y || my > box_y + SQ_SIZE) return;

        mx = (mx - box_x) / SQ_SIZE;

        board_table[x][y].piece = KNIGHT + mx;
        moves.get(replay_count - 1).piece = KNIGHT + mx;
        rules.choose_piece = false;
        lookPlayerCheck(board_table, rules);
        clearBoard(board_table);

        updateBoard();
        /*rules.cur_player ^= 1;

        if (!rules.checkmate && player[rules.cur_player] == GameType.COMPUTER) {

            drawText("Computer Turn");
            setComputerTimer();
        }*/
    }

    private void setCheckTimer() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                drawBoard(board_table);
            }
        }, 3000);
    }

    private void setComputerTimer() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Move ai_move = ai.makeMove();
                if (ai_move.x == -1) {
                    rules.checkmate = true;
                    drawText("Draw");
                }
                else makeMove(ai_move.x, ai_move.y, ai_move.x2, ai_move.y2);
            }
        }, 50);
    }

    private void addMove(int x, int y, int x2, int y2) {
        String text = Character.toString('A' + x);
        text += (y + 1) + Character.toString('A' + x2) + (y2 + 1);
        if (replay_count % 3 != 2) text += " : ";
            else text += " ";

        infoText.appendText(text);

        if (game_state == GameType.PLAY){
            Move mov = new Move(x, y, x2, y2, board_table[x][y].piece);
            moves.add(mov);
        }

        ++replay_count;
    }

    private void setXY(int x, int y) {

        if (!state && board_table[x][y].piece == -1) return;

        if (board_table[x][y].square_check < 2 && state) {
            clearBoard(board_table);
            drawBoard(board_table);
            this.state = false;
        }

        if (state) {
            makeMove(this.x, this.y, x, y);

            this.x = x;
            this.y = y;
        }
        else if (board_table[x][y].piece_color == rules.cur_player) {

            board_table[x][y].square_check = 1;
            this.x = x;
            this.y = y;
            this.state = true;

            lookMoves(board_table, rules, x, y);
            drawBoard(board_table);
        }
    }

    private void updateBoard() {

        int draw = lookPlayerDraw(board_table, rules);

        drawBoard(board_table);

        if (draw == 1) {
            drawText("Draw");
            parent.gameEnded("Replay Game");
            return;
        }
        else if (draw == 2) {
            if (rules.cur_player == WHITE) drawText("White Wins");
                else drawText("Black Wins");
            parent.gameEnded("Replay Game");
            return;
        }

        if (rules.check) {
            if (!rules.checkmate) {
                drawText("Check");
                setCheckTimer();
            }
            else {
                drawText("Checkmate");
                game_end_state = 1;
                parent.gameEnded("Replay Game");
                return;
            }
        }

        if (game_state == GameType.REPLAY) return;

        if (!rules.choose_piece) {

            rules.cur_player ^= 1;
            if (player[rules.cur_player] == GameType.COMPUTER) {
                drawText("Computer Turn");

                setComputerTimer();
            }
        }
        else drawChooseBox(rules.cur_player);
    }

    private void makeMove(int x, int y, int x2, int y2) {

        int old_piece = board_table[x2][y2].piece;

        removePiece(x2, y2);
        addMove(x, y, x2, y2);

        board_table[x2][y2].piece = board_table[x][y].piece;
        board_table[x2][y2].piece_color = rules.cur_player;
        board_table[x][y].piece = NONE;
        board_table[x][y].piece_color = NONE;

        lookSpecialMoves(board_table, rules, x2, y2);
        boolean passant = lookPawnPassant(board_table, rules, x2, y2);

        if (passant && !animation_on) {
            addPiece(PAWN, rules.cur_player ^ 1);
            moves.get(replay_count - 1).piece = KING + 3 + rules.cur_player;
        }

        if (rules.choose_piece && player[rules.cur_player] == GameType.COMPUTER)
        {
            board_table[x2][y2].piece = QUEEN;
            moves.get(replay_count - 1).piece = QUEEN;
            rules.choose_piece = false;
        }

        lookPlayerCheck(board_table, rules);

        int castling = movePossibleCastling(board_table, rules, x2, y2);
        if (castling > 0) moves.get(replay_count - 1).piece = KING + castling;

        clearBoard(board_table);

        if (animation_on) {
            animation.init(x, y, x2, y2, board_table[x2][y2].piece, rules.cur_player, castling, passant);

            if (old_piece > NONE) {
                board_table[x2][y2].piece = old_piece;
                board_table[x2][y2].piece_color = rules.cur_player ^ 1;
            }
            else {
                board_table[x2][y2].piece = NONE;
                board_table[x2][y2].piece_color = NONE;
            }

            animation.runAnimation();
        }
        else updateBoard();

        this.state = false;
    }

    private void removePiece(int x, int y) {

        if (board_table[x][y].piece > NONE) {

            if (animation_on == false) addPiece(board_table[x][y].piece, board_table[x][y].piece_color);
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