package com.hernandovela.reto6;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.*;
import android.media.MediaPlayer;
import android.view.*;
import android.widget.*;

/** Bundle: transient match. SharedPreferences: scores and settings across launches. */
public final class MainActivity extends Activity {
    private final TicTacToeGame game=new TicTacToeGame();
    private final Handler handler=new Handler(Looper.getMainLooper());
    private BoardView board;
    private TextView status, detail, score;
    private SharedPreferences prefs;
    private boolean computerTurn, gameOver, soundEnabled, resumed;
    private char goFirst='X';
    private int wins, draws, losses;
    private MediaPlayer humanSound, computerSound;
    private final Runnable computerMove=()->{
        if(!resumed || !computerTurn || gameOver) return;
        if(game.setMove('O',game.getComputerMove())) play(computerSound);
        computerTurn=false; finishMove();
    };
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        // Root Back must finish this match, including Android's predictive back path.
        if(Build.VERSION.SDK_INT>=33) getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
            android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::finish);
        prefs=getSharedPreferences("ttt_prefs",MODE_PRIVATE);
        wins=prefs.getInt("mHumanWins",0); losses=prefs.getInt("mComputerWins",0); draws=prefs.getInt("mTies",0);
        soundEnabled=prefs.getBoolean("sound",true);
        int level=prefs.getInt("difficulty",2);
        game.setDifficulty(TicTacToeGame.Difficulty.values()[Math.max(0,Math.min(2,level))]);
        if(state!=null) {
            game.setBoardState(state.getCharArray("board"));
            computerTurn=state.getBoolean("computerTurn");
            gameOver=state.getBoolean("mGameOver");
            goFirst=state.getChar("mGoFirst",'X');
        }
        setContentView(R.layout.main);
        board=findViewById(R.id.board); board.setGame(game); board.setMoveListener(this::humanMove);
        status=findViewById(R.id.status); detail=findViewById(R.id.detail); score=findViewById(R.id.score);
        findViewById(R.id.restart).setOnClickListener(v->newGame());
        findViewById(R.id.options).setOnClickListener(v->{
            PopupMenu popup=new PopupMenu(this,v); popup.inflate(R.menu.options_menu);
            popup.getMenu().findItem(R.id.sound).setChecked(soundEnabled);
            popup.setOnMenuItemClickListener(this::onOptionsItemSelected); popup.show();
        });
        View root=findViewById(R.id.root);
        final int padding=Math.round((getResources().getConfiguration().orientation==2?12:0)*getResources().getDisplayMetrics().density);
        root.setOnApplyWindowInsetsListener((v,insets)->{
            if(Build.VERSION.SDK_INT>=30) {
                android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
                v.setPadding(bars.left+padding,bars.top+padding,bars.right+padding,bars.bottom+padding);
            } else v.setPadding(insets.getSystemWindowInsetLeft()+padding,insets.getSystemWindowInsetTop()+padding,insets.getSystemWindowInsetRight()+padding,insets.getSystemWindowInsetBottom()+padding);
            return insets;
        });
        root.requestApplyInsets(); render();
        if(state!=null && state.getCharSequence("info")!=null) status.setText(state.getCharSequence("info"));
    }
    private void humanMove(int position) {
        if(computerTurn || gameOver || !game.setMove('X',position)) return;
        play(humanSound);
        computerTurn=game.result()==TicTacToeGame.PLAYING;
        finishMove(); scheduleComputer();
    }
    private void scheduleComputer() {
        handler.removeCallbacks(computerMove);
        if(resumed && computerTurn && !gameOver) handler.postDelayed(computerMove,1000);
    }
    private void finishMove() {
        int result=game.result();
        if(!gameOver && result!=TicTacToeGame.PLAYING) {
            gameOver=true; computerTurn=false;
            if(result==TicTacToeGame.HUMAN_WINS) wins++; else if(result==TicTacToeGame.COMPUTER_WINS) losses++; else draws++;
            savePreferences();
        }
        render();
    }
    private void render() {
        int result=game.result(); displayScores();
        if(result==TicTacToeGame.HUMAN_WINS) status.setText("¡Ganaste!");
        else if(result==TicTacToeGame.COMPUTER_WINS) status.setText("Android gana");
        else if(result==TicTacToeGame.DRAW) status.setText("¡Empate!");
        else status.setText(computerTurn ? "Android está pensando…" : "Tu turno");
        String[] levels={"Fácil","Normal","Experto"};
        detail.setText("Tú × · Android ○ · "+levels[game.getDifficulty().ordinal()]);
        StringBuilder description=new StringBuilder("Tablero de tres en raya. ");
        for(int row=0;row<3;row++) {
            description.append("Fila ").append(row+1).append(": ");
            for(int col=0;col<3;col++) {
                char occupant=game.getBoardOccupant(row*3+col);
                description.append(occupant==' ' ? "vacía" : String.valueOf(occupant)).append(col==2 ? ". " : ", ");
            }
        }
        board.setContentDescription(description.toString());
        board.setEnabled(!computerTurn && !gameOver); board.invalidate();
    }
    private void displayScores() { score.setText("Tú  "+wins+"   ·   Empates  "+draws+"   ·   Android  "+losses); }
    private void newGame() {
        handler.removeCallbacks(computerMove); game.clearBoard(); gameOver=false;
        goFirst=goFirst=='X'?'O':'X'; computerTurn=goFirst=='O'; render(); scheduleComputer();
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) { getMenuInflater().inflate(R.menu.options_menu,menu); return true; }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.new_game) newGame();
        else if(id==R.id.reset_scores) { wins=draws=losses=0; savePreferences(); displayScores(); }
        else if(id==R.id.sound) { soundEnabled=!soundEnabled; savePreferences(); item.setChecked(soundEnabled); }
        else if(id==R.id.difficulty) new AlertDialog.Builder(this).setTitle("Dificultad").setSingleChoiceItems(new String[]{"Fácil","Normal","Experto"},game.getDifficulty().ordinal(),(dialog,which)->{
            game.setDifficulty(TicTacToeGame.Difficulty.values()[which]); savePreferences(); render(); dialog.dismiss();
        }).setNegativeButton("Cancelar",null).show();
        else if(id==R.id.about) new AlertDialog.Builder(this).setTitle("Reto 6").setMessage("Cambio de orientación y guardado de estado.\nLa partida y el turno se conservan al girar. Los marcadores y la dificultad se guardan entre aperturas.\nBasado en el reto de Frank McCown, Harding University (CC BY 3.0).").setPositiveButton("Aceptar",null).show();
        else return super.onOptionsItemSelected(item);
        return true;
    }
    private void savePreferences() {
        prefs.edit().putInt("mHumanWins",wins).putInt("mComputerWins",losses).putInt("mTies",draws).putInt("difficulty",game.getDifficulty().ordinal()).putBoolean("sound",soundEnabled).commit();
    }
    private void play(MediaPlayer player) { if(soundEnabled && player!=null) { player.seekTo(0); player.start(); } }
    @Override protected void onResume() {
        super.onResume(); resumed=true;
        humanSound=MediaPlayer.create(this,R.raw.human_move); computerSound=MediaPlayer.create(this,R.raw.computer_move);
        scheduleComputer();
    }
    @Override protected void onPause() {
        resumed=false; handler.removeCallbacks(computerMove);
        if(humanSound!=null) {humanSound.release();humanSound=null;}
        if(computerSound!=null) {computerSound.release();computerSound=null;}
        super.onPause();
    }
    // Legacy fallback only: API 33+ uses the OnBackInvokedDispatcher registered above.
    @android.annotation.SuppressLint("GestureBackNavigation")
    @Override public void onBackPressed() { finish(); }
    @Override protected void onStop() { savePreferences(); super.onStop(); }
    @Override protected void onSaveInstanceState(Bundle out) {
        out.putCharArray("board",game.getBoardState()); out.putBoolean("computerTurn",computerTurn);
        out.putBoolean("mGameOver",gameOver); out.putChar("mGoFirst",goFirst); out.putCharSequence("info",status.getText());
        super.onSaveInstanceState(out);
    }
}
