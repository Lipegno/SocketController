package com.example.filipe.socketcontroller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Filipe on 01/08/2017.
 */
public class SimulationView extends SurfaceView implements SurfaceHolder.Callback{

    int height;
    int width;

    float x;
    float y;

    float x2;
    float y2;

    Paint p;

    public SimulationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onDraw(Canvas c){
        if(c!=null){
            c.drawColor(Color.parseColor("#E2E0DB"));
            drawCircles(c);
            }
        }

    public void requestRender(){
        System.gc(); // ainda n‹o se se Ž necess‡rio mas pronto s— para ter certeza chamamos o GC agora.. aqui Ž um lugar seguro
        Canvas c = null;
        SurfaceHolder sh = getHolder();
        try {
            c = sh.lockCanvas(null);
            synchronized (sh) {
                onDraw(c);
            }
        } finally {
            // do this in a finally so that if an exception is thrown
            // during the above, we don't leave the Surface in an
            // inconsistent state
            if (c != null) {
                sh.unlockCanvasAndPost(c);
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        //	requestRender();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.e("COMP","COMP WIDGET CREATED");
        height = getHeight();
        width  = getWidth();
        p = new Paint();
        p.setColor(Color.parseColor("#FF0000"));
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

    private void drawCircles(Canvas c){


        c.drawCircle((x+2)*120, ((y*-1)+2)*120, 20f, p);

        c.drawCircle((x2+2), ((y2*-1)+2), 20f, p);

    }

    public void setCoords(float x, float y,float x2, float y2){
        this.x = x;
        this.y = y;
        this.x2 = x2;
        this.y2 = y2;
        requestRender();
    }


}
