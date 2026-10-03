package com.hernandovela.reto6;
import org.junit.Test;
import static org.junit.Assert.*;
public class TicTacToeGameTest {
    @Test public void boardStateUsesDefensiveCopies() {
        TicTacToeGame g=new TicTacToeGame(); g.setMove('X',4);
        char[] state=g.getBoardState(); state[4]='O'; assertEquals('X',g.getBoardOccupant(4));
        g.setBoardState(state); state[4]='X'; assertEquals('O',g.getBoardOccupant(4));
    }
    @Test public void expertNeverLosesAgainstAnyHumanContinuation() { explore(new TicTacToeGame()); }
    private void explore(TicTacToeGame g) {
        assertNotEquals(TicTacToeGame.HUMAN_WINS,g.result());
        if(g.result()!=TicTacToeGame.PLAYING) return;
        String before=g.save();
        for(int i=0;i<9;i++) if(g.getBoardOccupant(i)==' ') {
            g.setMove('X',i);
            assertNotEquals(TicTacToeGame.HUMAN_WINS,g.result());
            if(g.result()==TicTacToeGame.PLAYING) { g.setMove('O',g.getComputerMove()); explore(g); }
            g.restore(before);
        }
    }
    @Test public void rejectsIllegalMoves() {
        TicTacToeGame g=new TicTacToeGame();
        assertFalse(g.setMove('X',-1)); assertFalse(g.setMove('O',9)); assertFalse(g.setMove('Z',0));
        assertTrue(g.setMove('X',0)); assertFalse(g.setMove('O',0));
    }
    @Test public void detectsEveryWinningLineForBothPlayers() {
        int[][] lines={{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for(char p:new char[]{'X','O'}) for(int[] line:lines) {
            TicTacToeGame g=new TicTacToeGame();
            for(int i:line) assertTrue(g.setMove(p,i));
            assertEquals(p=='X'?1:2,g.result()); assertArrayEquals(line,g.winningLine()); assertFalse(g.setMove('X',8));
        }
    }
    @Test public void drawAndReset() {
        TicTacToeGame g=new TicTacToeGame(); g.restore("XOXXOOOXX");
        assertEquals(TicTacToeGame.DRAW,g.result()); assertEquals(-1,g.getComputerMove());
        g.clearBoard(); assertEquals(0,g.result()); assertEquals("         ",g.save());
    }
    @Test public void computerWinsBeforeBlocking() {
        TicTacToeGame g=new TicTacToeGame(); g.setDifficulty(TicTacToeGame.Difficulty.NORMAL); g.restore("OO XX    "); assertEquals(2,g.getComputerMove());
        assertEquals("OO XX    ",g.save());
    }
    @Test public void computerBlocksAndOnlyChoosesEmptyCells() {
        TicTacToeGame g=new TicTacToeGame(); g.setDifficulty(TicTacToeGame.Difficulty.NORMAL); g.restore("XX  O    "); assertEquals(2,g.getComputerMove());
        g.restore("X O      "); for(int i=0;i<100;i++) assertEquals(' ',g.getBoardOccupant(g.getComputerMove()));
    }
    @Test public void roundTripAndInvalidRestore() {
        TicTacToeGame g=new TicTacToeGame(); g.setMove('X',4); String s=g.save();
        TicTacToeGame copy=new TicTacToeGame(); copy.restore(s); assertEquals(s,copy.save());
        copy.restore("invalid"); assertEquals(s,copy.save());
    }
}
