package com.hernandovela.reto6;

import java.util.Arrays;
import java.util.Random;

/** Pure game rules, independent of the Android UI. */
public final class TicTacToeGame {
    public static final int BOARD_SIZE = 9;
    public static final char HUMAN_PLAYER = 'X', COMPUTER_PLAYER = 'O', EMPTY = ' ';
    public static final int PLAYING = 0, HUMAN_WINS = 1, COMPUTER_WINS = 2, DRAW = 3;
    private static final int[][] LINES = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
    private final char[] board = new char[BOARD_SIZE];
    public enum Difficulty { EASY, NORMAL, EXPERT }
    private Difficulty difficulty = Difficulty.EXPERT;
    public void setDifficulty(Difficulty value) { difficulty=value; }
    public Difficulty getDifficulty() { return difficulty; }
    public char[] getBoardState() { return board.clone(); }
    public void setBoardState(char[] state) { if(state!=null) restore(new String(state)); }
    private final Random random = new Random();
    public TicTacToeGame() { clearBoard(); }
    public void clearBoard() { Arrays.fill(board, EMPTY); }
    public char getBoardOccupant(int i) { return board[i]; }
    public boolean setMove(char player, int i) {
        if (i < 0 || i >= BOARD_SIZE || board[i] != EMPTY || result() != PLAYING
                || (player != HUMAN_PLAYER && player != COMPUTER_PLAYER)) return false;
        board[i] = player;
        return true;
    }
    public int[] winningLine() {
        for (int[] line : LINES)
            if (board[line[0]] != EMPTY && board[line[0]] == board[line[1]] && board[line[1]] == board[line[2]])
                return line.clone();
        return null;
    }
    public int result() {
        int[] line = winningLine();
        if (line != null) return board[line[0]] == HUMAN_PLAYER ? HUMAN_WINS : COMPUTER_WINS;
        for (char c : board) if (c == EMPTY) return PLAYING;
        return DRAW;
    }
    /** Win, block, then choose a free square: a beatable, tactical opponent. */
    public int getComputerMove() {
        if (result() != PLAYING) return -1;
        if(difficulty==Difficulty.EXPERT) {
            int best=-100, move=-1;
            for(int i=0;i<9;i++) if(board[i]==EMPTY) { board[i]=COMPUTER_PLAYER; int score=minimax(false,0); board[i]=EMPTY; if(score>best) {best=score;move=i;} }
            return move;
        }
        if(difficulty==Difficulty.NORMAL) for (char player : new char[]{COMPUTER_PLAYER, HUMAN_PLAYER}) {
            for (int i = 0; i < BOARD_SIZE; i++) if (board[i] == EMPTY) {
                board[i] = player;
                boolean wins = winningLine() != null;
                board[i] = EMPTY;
                if (wins) return i;
            }
        }
        int[] empty = new int[9]; int count = 0;
        for (int i = 0; i < BOARD_SIZE; i++) if (board[i] == EMPTY) empty[count++] = i;
        return empty[random.nextInt(count)];
    }
    private int minimax(boolean computer, int depth) {
        int r=result();
        if(r==COMPUTER_WINS) return 10-depth;
        if(r==HUMAN_WINS) return depth-10;
        if(r==DRAW) return 0;
        int best=computer ? -100 : 100;
        for(int i=0;i<9;i++) if(board[i]==EMPTY) {
            board[i]=computer ? COMPUTER_PLAYER : HUMAN_PLAYER;
            int score=minimax(!computer,depth+1); board[i]=EMPTY;
            best=computer ? Math.max(best,score) : Math.min(best,score);
        }
        return best;
    }
    public String save() { return new String(board); }
    public void restore(String state) {
        if (state == null || state.length() != BOARD_SIZE) return;
        for (int i = 0; i < BOARD_SIZE; i++) {
            char c = state.charAt(i);
            if (c != EMPTY && c != HUMAN_PLAYER && c != COMPUTER_PLAYER) return;
        }
        state.getChars(0, BOARD_SIZE, board, 0);
    }
}
