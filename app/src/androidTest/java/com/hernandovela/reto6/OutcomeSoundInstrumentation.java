package com.hernandovela.reto6;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.widget.TextView;
import android.graphics.Bitmap;
import java.io.File;
import java.io.FileOutputStream;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/** Device test: verifies real MediaPlayer playback, mute and Activity recreation. */
public final class OutcomeSoundInstrumentation extends Instrumentation {
    private Activity activity;
    private static Object field(Object target,String name) throws Exception {
        Field f=target.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(target);
    }
    private static void set(Object target,String name,Object value) throws Exception {
        Field f=target.getClass().getDeclaredField(name); f.setAccessible(true); f.set(target,value);
    }
    private static void call(Object target,String name) throws Exception {
        Method m=target.getClass().getDeclaredMethod(name); m.setAccessible(true); m.invoke(target);
    }
    private void main(CheckedAction action) {
        final Throwable[] failure=new Throwable[1];
        runOnMainSync(()-> { try {action.run();} catch(Throwable e) {failure[0]=e;} });
        if(failure[0]!=null) throw new AssertionError(failure[0]);
    }
    private interface CheckedAction { void run() throws Exception; }
    private static void require(boolean condition,String message) {
        if(!condition) throw new AssertionError(message);
    }
    private MediaPlayer player(String name) throws Exception {
        return (MediaPlayer)field(field(activity,"audio"),name);
    }
    private void board(String cells) throws Exception {
        TicTacToeGame game=(TicTacToeGame)field(activity,"game");
        game.setBoardState(cells.toCharArray()); set(activity,"gameOver",false);
        set(activity,"computerTurn",false);
    }
    private void move(int cell) throws Exception {
        Method m=activity.getClass().getDeclaredMethod("humanMove",int.class);
        m.setAccessible(true);m.invoke(activity,cell);
    }
    private AlertDialog popup() throws Exception { return (AlertDialog)field(activity,"resultDialog"); }
    private void capture(String name) throws Exception {
        waitForIdleSync(); android.os.SystemClock.sleep(350);
        Bitmap screenshot=getUiAutomation().takeScreenshot();
        require(screenshot!=null,"Evidence screenshot must exist");
        try(FileOutputStream out=new FileOutputStream(new File(getTargetContext().getFilesDir(),name))) {
            screenshot.compress(Bitmap.CompressFormat.PNG,100,out);
        }
        screenshot.recycle();
    }
    private void recreateActivity(boolean rotate) {
        ActivityMonitor monitor=addMonitor(MainActivity.class.getName(),null,false);
        main(()->{
            if(rotate) activity.setRequestedOrientation(activity.getResources().getConfiguration().orientation==2 ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            else activity.recreate();
        });
        Activity replacement=monitor.waitForActivityWithTimeout(5000);
        require(replacement!=null,"Activity must recreate");activity=replacement;removeMonitor(monitor);waitForIdleSync();
    }
    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }
    @Override public void onStart() {
        Bundle result=new Bundle();
        SharedPreferences prefs=getTargetContext().getSharedPreferences("ttt_prefs",0);
        Map<String,?> original=prefs.getAll();
        try {
            Intent intent=new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity=startActivitySync(intent); waitForIdleSync();
            main(()->{
                set(activity,"soundEnabled",true); board("XX OO    "); move(2);
                require(player("victory").isPlaying(),"Victory cue must play after winning move");
                require(!player("defeat").isPlaying(),"Defeat cue must not play after victory");
                require(popup()!=null && popup().isShowing(),"Victory popup must open");
                require(((TextView)popup().findViewById(R.id.result_title)).getText().toString().equals("¡Ganaste!"),"Victory popup title");
            });
            capture("popup-victoria.png");
            // Recreate during the cue: retain the exact player and its playback position.
            final MediaPlayer[] retained=new MediaPlayer[1];
            main(()->retained[0]=player("victory"));
            int winsBefore=(Integer)field(activity,"wins");
            recreateActivity(true);
            main(()->{
                require(player("victory")==retained[0],"Rotation must retain the playing audio instance");
                require((Boolean)field(activity,"soundEnabled"),"Rotation must keep sound enabled");
                require((Boolean)field(activity,"gameOver"),"Rotation must retain the completed match");
                require(popup()!=null && popup().isShowing(),"Rotate must restore one visible popup");
                require((Integer)field(activity,"wins")==winsBefore,"Rotate must not count the win again");
                popup().getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
            });
            waitForIdleSync();
            recreateActivity(false);
            main(()->{
                require(popup()==null && !(Boolean)field(activity,"resultPopupVisible"),"Dismissed popup must stay dismissed after recreation");
                board("OO XXOXOX");set(activity,"computerTurn",true);
                ((Runnable)field(activity,"computerMove")).run();
                require(player("defeat").isPlaying(),"Defeat cue must play when Android wins");
                require(!player("victory").isPlaying(),"Victory cue must stop before defeat cue");
                require(popup()!=null && popup().isShowing(),"Defeat popup must open");
                require(((TextView)popup().findViewById(R.id.result_title)).getText().toString().equals("Perdiste"),"Defeat popup title");
            });
            capture("popup-derrota.png");
            main(()->{
                popup().getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            });
            waitForIdleSync();
            main(()->{
                require(!(Boolean)field(activity,"gameOver") && popup()==null,"Play again must close popup and start a new match");
                call(activity,"newGame");set(activity,"soundEnabled",false);
                board("XX OO    ");move(2);
                require(!player("victory").isPlaying()&&!player("defeat").isPlaying(),"Mute must suppress outcome cues");
                call(activity,"newGame");set(activity,"soundEnabled",true);
                board("XOXXOOOX ");move(8);
                require(!player("victory").isPlaying()&&!player("defeat").isPlaying(),"Draw must not use win/loss cue");
                require(popup()==null,"Draw must not show a victory or defeat popup");
            });
            result.putString("stream","PASS: victory, defeat, retained audio during recreation, sound preference, mute and draw; victory/defeat popups, rotation, dismissal and play again.\n");
            result.putInt("passed",13);
        } catch(Throwable e) {
            result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(e));
            result.putInt("failed",1);
        } finally {
            if(activity!=null) main(()->activity.finish());waitForIdleSync();
            SharedPreferences.Editor edit=prefs.edit().clear();
            for(Map.Entry<String,?> entry:original.entrySet()) {
                Object value=entry.getValue();String key=entry.getKey();
                if(value instanceof Integer)edit.putInt(key,(Integer)value);
                else if(value instanceof Boolean)edit.putBoolean(key,(Boolean)value);
            }
            edit.commit();
        }
        finish(result.containsKey("failed")?Activity.RESULT_CANCELED:Activity.RESULT_OK,result);
    }
}
