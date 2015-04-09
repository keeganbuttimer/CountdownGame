package edu.augustana.csc490.countdown;

/**
 * Created by keeganbuttimer11 on 4/8/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class startGameView extends SurfaceView implements SurfaceHolder.Callback
{
    private static final String TAG = "GameStarter"; // for Log.w(TAG, ...)

    private GameThread gameThread; // runs the main game loop
    private Activity mainActivity; // keep a reference to the main Activity

    private boolean isGameOver = true;

    private int centerX;
    private int centerY;
    private double circle1x;
    private double circle1y;
    private int circle1radius;
    private double circle2x;
    private double circle2y;
    private int circle2radius;
    private double circle1degree;
    private double circle2degree;
    private int screenWidth;
    private int screenHeight;
    private Paint circleBackgroundPaint;
    private Paint backgroundPaint;
    private Paint circlePaint;

    public startGameView(Context context, AttributeSet atts)
    {
        super(context, atts);
        mainActivity = (Activity) context;

        getHolder().addCallback(this);

        circleBackgroundPaint = new Paint();
        circleBackgroundPaint.setColor(Color.parseColor("#516D6D"));
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#BEC0C0"));
        circlePaint = new Paint();
        circlePaint.setColor(Color.DKGRAY);
    }

    // called when the size changes (and first time, when view is created)
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        screenWidth = w;
        screenHeight = h;
        circle1degree=0;
        circle2degree=0;
        circle1radius=10;
        circle2radius=10;
        startNewGame();
    }

    public void startNewGame()
    {
        centerX = screenWidth/2;
        centerY = screenHeight/2;
        circle1x= centerX+200;
        circle1y= centerY;
        if (isGameOver)
        {
            isGameOver = false;
            gameThread = new GameThread(getHolder());
            gameThread.start(); // start the main game loop going
        }
    }


    private void gameStep()
    {
        moveCircleOne();
    }
    public void moveCircleOne(){
        circle1degree+=Math.PI/180;
        circle2degree-=2*Math.PI/180;
        circle1x= (centerX+(200*Math.cos(circle1degree)));
        circle1y= (centerY+(200*Math.sin(circle1degree)));
        circle2x= (centerX+(200*Math.cos(circle2degree)));
        circle2y= (centerY+(200*Math.sin(circle2degree)));
    }
    public void updateView(Canvas canvas)
    {

        if (canvas != null) {
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
            canvas.drawCircle(centerX, centerY, 200, circleBackgroundPaint);
            canvas.drawCircle((int)circle1x,(int)circle1y,circle1radius, circlePaint);
            canvas.drawCircle((int)circle2x,(int)circle2y,circle2radius, circlePaint);

           if(isGameOver) {
               canvas.drawText(displayResult(), 200, 200, circlePaint);
           }



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
        if (e.getAction() == MotionEvent.ACTION_DOWN)
        {
            isGameOver=true;
        }

        return true;
    }

    public String displayResult(){
        if(Math.abs(circle1x - circle2x)<circle1radius && Math.abs(circle1y-circle2y)<circle1radius){
            return "Victory!";
        }
        return "Failure!";
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
                   Thread.sleep(10); // if you want to slow down the action...
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