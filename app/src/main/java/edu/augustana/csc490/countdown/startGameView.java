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
    private Paint pass;
    private Paint fail;
    private Random r;

    private boolean changeColor;

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
        pass=new Paint();
        pass.setColor(Color.GREEN);
        fail=new Paint();
        fail.setColor(Color.RED);
        circlePaint.setColor(Color.DKGRAY);
    }

    // called when the size changes (and first time, when view is created)
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        screenWidth = w;
        screenHeight = h;
        trackRadius=100;
        circle1degree=0;
        circle2degree=0;
        circle1radius=40;
        circle2radius=40;
        circle1speed=2;
        circle2speed=2;
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
            if(changeColor){
                if(displayResult()==1) {
                    canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), pass);
                }
                else{
                    canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), fail);
                }
            }
            canvas.drawCircle(centerX, centerY, trackRadius, circleBackgroundPaint);
            canvas.drawCircle((int)circle1x,(int)circle1y,circle1radius, circlePaint);
            canvas.drawCircle((int)circle2x,(int)circle2y,circle2radius, circlePaint);
            canvas.drawText("Circle 1 Speed: " + circle1speed, 200, 140, circlePaint);
            canvas.drawText("Circle 2 Speed: "+circle2speed,200,170,circlePaint);
            canvas.drawText("Circle 1 Radius: "+circle1radius,200,200,circlePaint);
            canvas.drawText("Circle 2 Radius: "+circle2radius,200,230,circlePaint);
            canvas.drawText("Track radius: "+trackRadius,200,260,circlePaint);
            changeColor=false;



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
            changeColor=true;
            if(displayResult()==0) {
                //isGameOver = true;
            }
            else{
                trackRadius+=50;
                if(trackRadius>400){
                    trackRadius=100;
                    speedUp();
                    if(circle1speed>5 || circle2speed>5) {
                        circle1speed=2;
                        circle2speed=2;
                        shrink();
                        if(circle1radius<20 || circle2radius<20){
                            isGameOver=true;
                        }
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
        if(Math.abs(circle1x - circle2x)<circle1radius*2 && Math.abs(circle1y-circle2y)<circle1radius*2){
           result.setColor(Color.GREEN);
            return 1;
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