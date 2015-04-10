package edu.augustana.csc490.countdown;

/**
 * Created by keeganbuttimer11 on 4/8/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class startGameView extends SurfaceView implements SurfaceHolder.Callback
{
    private static final String TAG = "GameStarter"; // for Log.w(TAG, ...)

    private GameThread gameThread; // runs the main game loop
    private Activity mainActivity; // keep a reference to the main Activity

    private boolean isGameOver = true;

    private int centerX;
    private int centerY;
    private int trackRadius;

    private double circle1x;
    private double circle1y;
    private int circle1radius;
    private double circle1degree;
    private int circle1speed;

    private double circle2x;
    private double circle2y;
    private int circle2radius;
    private double circle2degree;
    private int circle2speed;

    private int screenWidth;
    private int screenHeight;
    private Paint circleBackgroundPaint;
    private Paint backgroundPaint;
    private Paint circlePaint;
    private Paint scorePaint;
    private Random r;
    private SoundPool mySound;
    private int beepId;
    private int buzzId;
    private int score;
    private int positiveMultiplier;
    private int negativeMultiplier;
    private int round;
    private int lives;
    private int bonusLife;

    private boolean levelsGame;
    private boolean randomGame;


    public startGameView(Context context, AttributeSet atts)
    {
        super(context, atts);
        mySound= new SoundPool(1, AudioManager.STREAM_MUSIC,0);
        buzzId=mySound.load(context,R.raw.buzz,1);
        beepId=mySound.load(context,R.raw.beep,1);
        mainActivity = (Activity) context;
        getHolder().addCallback(this);
        circleBackgroundPaint = new Paint();
        circleBackgroundPaint.setColor(Color.parseColor("#516D6D"));
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#B4B6B6"));
        circlePaint = new Paint();
        circlePaint.setColor(Color.DKGRAY);
        scorePaint= new Paint();
        scorePaint.setColor(Color.parseColor("#B4B6B6"));
        scorePaint.setTextSize(72);
        scorePaint.setTextAlign(Paint.Align.CENTER);
    }

    // called when the size changes (and first time, when view is created)
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;
        if(levelsGame) {
            trackRadius = 100;
        }
        else{
            trackRadius = 350;
        }
        circle1degree=0;
        circle2degree=0;
        circle1radius=40;
        circle2radius=40;
        circle1speed=2;
        circle2speed=2;
        lives=3;
        score=0;
        positiveMultiplier =1;
        negativeMultiplier =-1;
        round=1;
        bonusLife=0;
        randomGame=true;
        levelsGame=false;
        startNewGame();
    }

    public void startNewGame()
    {
        centerX = screenWidth/2;
        centerY = screenHeight/2;
        circle1x= centerX+trackRadius;
        circle1y= centerY;
        r= new Random();
        if (isGameOver)
        {
            isGameOver = false;
            gameThread = new GameThread(getHolder());
            gameThread.start(); // start the main game loop going
        }
    }


    private void gameStep()
    {
        moveCircleOne(circle1speed,circle2speed);
    }
    public void moveCircleOne(int speed1,int speed2){
        circle1degree+=speed1*Math.PI/180;
        circle2degree-=speed2*Math.PI/180;
        circle1x= (centerX+(trackRadius*Math.cos(circle1degree)));
        circle1y= (centerY+(trackRadius*Math.sin(circle1degree)));
        circle2x= (centerX+(trackRadius*Math.cos(circle2degree)));
        circle2y= (centerY+(trackRadius*Math.sin(circle2degree)));
    }
    public void updateView(Canvas canvas)
    {

        if (canvas != null) {
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
            canvas.drawCircle(centerX, centerY, trackRadius, circleBackgroundPaint);
            canvas.drawCircle((int) circle1x, (int) circle1y, circle1radius, circlePaint);
            canvas.drawCircle((int) circle2x, (int) circle2y, circle2radius, circlePaint);
            canvas.drawText(""+score, centerX, centerY-30, scorePaint);
            canvas.drawText("Lives: "+lives, centerX, centerY+60, scorePaint);

        }
    }

    // stop the game; may be called by the MainGameFragment onPause
    public void stopGame()
    {
        if (gameThread != null)
            gameThread.setRunning(false);
    }

    // release resources; may be called by MainGameFragment onDestroy
    public void releaseResources()
    {
        // release any resources (e.g. SoundPool stuff)
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    // called when the surface is destroyed
    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // ensure that thread terminates properly
        boolean retry = true;
        gameThread.setRunning(false); // terminate gameThread

        while (retry)
        {
            try
            {
                gameThread.join(); // wait for gameThread to finish
                retry = false;
            }
            catch (InterruptedException e)
            {
                Log.e(TAG, "Thread interrupted", e);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isGameOver) {
                if (levelsGame) {
                    if (displayResult() == 0) {
                        mySound.play(buzzId, 1, 1, 1, 0, 1);
                        positiveMultiplier = 1;
                        score -= 10 * round * negativeMultiplier;
                        negativeMultiplier++;
                    } else {
                        negativeMultiplier = 1;
                        score += round * 10 * positiveMultiplier;
                        positiveMultiplier++;
                        mySound.play(beepId, 1, 1, 1, 0, 1);
                        trackRadius += 50;
                        if (trackRadius > 400) {
                            trackRadius = 100;
                            speedUp();
                            if (circle1speed > 5 || circle2speed > 5) {
                                circle1speed = 2;
                                circle2speed = 2;
                                shrink();
                                round++;
                                if (circle1radius < 20 || circle2radius < 20) {
                                    isGameOver = true;
                                }
                            }
                        }
                    }
                }
                if (randomGame) {
                    if (displayResult() == 0) {
                        mySound.play(buzzId, 1, 1, 1, 0, 1);
                        positiveMultiplier = 1;
                        lives--;
                        if (lives <= 0) {
                            isGameOver = true;
                        }
                        trackRadius = 350;
                        circle1speed = 1 + r.nextInt(7);
                        if(circle1speed>=4){
                            circle2speed=1+r.nextInt(8-circle1speed);
                        }
                        else {
                            circle2speed = 1 + r.nextInt(7);
                        }
                        circle1radius = 20 + r.nextInt(41);
                        circle2radius = 20 + r.nextInt(41);
                    } else {
                        score += 10 * positiveMultiplier;
                        positiveMultiplier++;
                        bonusLife++;
                        if (bonusLife == 5) {
                            lives++;
                            bonusLife = 0;
                        }
                        mySound.play(beepId, 1, 1, 1, 0, 1);

                        trackRadius = 350;
                        circle1speed = 1 + r.nextInt(7);
                        if(circle1speed>=4){
                            circle2speed=1+r.nextInt(8-circle1speed);
                        }
                        else {
                            circle2speed = 1 + r.nextInt(7);
                        }
                        circle1radius = 20 + r.nextInt(21);
                        circle2radius = 20 + r.nextInt(21);
                    }
                }

            }
        }
        return true;
    }
    public void shrink(){
        circle1radius-=5;
        circle2radius-=5;
    }
    public void speedUp(){
        circle1speed++;
        circle2speed++;
    }
    public int displayResult(){
        Paint result= new Paint();
        result.setColor(Color.RED);
        if(circle1radius>circle2radius) {
            if (Math.abs(circle1x - circle2x) < circle1radius * 2 && Math.abs(circle1y - circle2y) < circle1radius * 2) {
                result.setColor(Color.GREEN);
                return 1;
            }
        }
        else{
            if (Math.abs(circle1x - circle2x) < circle2radius * 2 && Math.abs(circle1y - circle2y) < circle2radius * 2) {
                result.setColor(Color.GREEN);
                return 1;
            }
        }
        return 0;
    }



    // Thread subclass to run the main game loop
    private class GameThread extends Thread
    {
        private SurfaceHolder surfaceHolder; // for manipulating canvas
        private boolean threadIsRunning = true; // running by default

        // initializes the surface holder
        public GameThread(SurfaceHolder holder)
        {
            surfaceHolder = holder;
            setName("GameThread");
        }

        // changes running state
        public void setRunning(boolean running)
        {
            threadIsRunning = running;
        }

        @Override
        public void run()
        {
            Canvas canvas = null;

            while (threadIsRunning)
            {
                try
                {
                    // get Canvas for exclusive drawing from this thread
                    canvas = surfaceHolder.lockCanvas(null);

                    // lock the surfaceHolder for drawing
                    synchronized(surfaceHolder)
                    {
                        if(!isGameOver) {
                            gameStep();         // update game state
                        }
                        updateView(canvas); // draw using the canvas
                    }
                   Thread.sleep(1); // if you want to slow down the action...
                } catch (InterruptedException ex) {
                    Log.e(TAG,ex.toString());
                }
                finally  // regardless if any errors happen...
                {
                    // make sure we unlock canvas so other threads can use it
                    if (canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}