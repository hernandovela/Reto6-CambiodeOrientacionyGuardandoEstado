package com.hernandovela.reto6;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/** A real custom Canvas board: the nine cells are not Buttons. */
public final class BoardView extends View {
    public static final int GRID_WIDTH = 6;
    private Bitmap mHumanBitmap, mComputerBitmap;
    private Paint mPaint;
    private TicTacToeGame mGame;
    private MoveListener listener;
    private int downCell = -1;
    public interface MoveListener { void onMove(int position); }
    public BoardView(Context c) { super(c); initialize(); }
    public BoardView(Context c, AttributeSet a) { super(c,a); initialize(); }
    public BoardView(Context c, AttributeSet a, int style) { super(c,a,style); initialize(); }
    private void initialize() {
        mHumanBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.x_img);
        mComputerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.o_img);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        setClickable(true);
        setContentDescription("Tablero de tres en raya, tres filas y tres columnas");
    }
    public void setGame(TicTacToeGame game) { mGame = game; invalidate(); }
    public void setMoveListener(MoveListener value) { listener = value; }
    public int getBoardCellWidth() { return getWidth()/3; }
    public int getBoardCellHeight() { return getHeight()/3; }
    @Override protected void onMeasure(int w, int h) {
        int size = Math.min(MeasureSpec.getSize(w), MeasureSpec.getSize(h));
        if (MeasureSpec.getMode(h) == MeasureSpec.UNSPECIFIED) size = MeasureSpec.getSize(w);
        setMeasuredDimension(size, size);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cw = getWidth()/3f, ch = getHeight()/3f;
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(0,0,getWidth(),getHeight(),24,24,mPaint);
        int[] line = mGame == null ? null : mGame.winningLine();
        if (line != null) {
            mPaint.setColor(Color.rgb(220,242,232));
            for (int i : line) canvas.drawRoundRect(i%3*cw+7,i/3*ch+7,(i%3+1)*cw-7,(i/3+1)*ch-7,18,18,mPaint);
        }
        mPaint.setColor(Color.rgb(221,230,226));
        mPaint.setStrokeWidth(GRID_WIDTH * getResources().getDisplayMetrics().density / 2);
        for (int i=1;i<3;i++) {
            canvas.drawLine(i*cw,12,i*cw,getHeight()-12,mPaint);
            canvas.drawLine(12,i*ch,getWidth()-12,i*ch,mPaint);
        }
        if (mGame == null) return;
        float inset = Math.min(cw,ch)*0.23f;
        for (int i=0;i<9;i++) {
            char c = mGame.getBoardOccupant(i);
            if (c == TicTacToeGame.EMPTY) continue;
            RectF destination = new RectF(i%3*cw+inset,i/3*ch+inset,(i%3+1)*cw-inset,(i/3+1)*ch-inset);
            canvas.drawBitmap(c == 'X' ? mHumanBitmap : mComputerBitmap,null,destination,mPaint);
        }
    }
    private int cell(float x, float y) {
        if (x<0 || y<0 || x>=getWidth() || y>=getHeight() || getWidth()==0 || getHeight()==0) return -1;
        return (int)(y*3/getHeight())*3+(int)(x*3/getWidth());
    }
    @Override public boolean onTouchEvent(MotionEvent e) {
        if (!isEnabled()) return false;
        if (e.getActionMasked()==MotionEvent.ACTION_DOWN) { downCell=cell(e.getX(),e.getY()); return true; }
        if (e.getActionMasked()==MotionEvent.ACTION_UP) {
            int up=cell(e.getX(),e.getY());
            if (up>=0 && up==downCell) { performClick(); if(listener!=null) listener.onMove(up); }
            downCell=-1; return true;
        }
        if (e.getActionMasked()==MotionEvent.ACTION_CANCEL) downCell=-1;
        return true;
    }
    @Override public boolean performClick() { super.performClick(); return true; }
}
