package com.hernandovela.reto6;

import android.app.Activity;
import android.app.Instrumentation;
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
        runOnMainSync(()-> { try {action.run();} catch(Exception e) {throw new AssertionError(e);} });
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
            });
            // Recreate during the cue: retain the exact player and its playback position.
            final MediaPlayer[] retained=new MediaPlayer[1];
            main(()->retained[0]=player("victory"));
            ActivityMonitor monitor=addMonitor(MainActivity.class.getName(),null,false);
            main(()->activity.recreate());
            Activity replacement=monitor.waitForActivityWithTimeout(5000);
            require(replacement!=null,"Activity must recreate");activity=replacement;removeMonitor(monitor);waitForIdleSync();
            main(()->{
                require(player("victory")==retained[0],"Rotation must retain the playing audio instance");
                require((Boolean)field(activity,"soundEnabled"),"Rotation must keep sound enabled");
                require((Boolean)field(activity,"gameOver"),"Rotation must retain the completed match");
                board("OO XXOXOX");set(activity,"computerTurn",true);
                ((Runnable)field(activity,"computerMove")).run();
                require(player("defeat").isPlaying(),"Defeat cue must play when Android wins");
                require(!player("victory").isPlaying(),"Victory cue must stop before defeat cue");
                call(activity,"newGame");set(activity,"soundEnabled",false);
                board("XX OO    ");move(2);
                require(!player("victory").isPlaying()&&!player("defeat").isPlaying(),"Mute must suppress outcome cues");
                call(activity,"newGame");set(activity,"soundEnabled",true);
                board("XOXXOOOX ");move(8);
                require(!player("victory").isPlaying()&&!player("defeat").isPlaying(),"Draw must not use win/loss cue");
            });
            result.putString("stream","PASS: victory, defeat, retained audio during recreation, sound preference, mute and draw.\n");
            result.putInt("passed",6);
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
