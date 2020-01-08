package com.example.runnertracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.net.Uri;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class GifPlayer extends View {
    private Movie animation;
    private int w, height;
    private Context content;
    private boolean paused;
    private int time;
    private InputStream stream;
    private long begin;

    public GifPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.content = context;
        if (attrs.getAttributeName(1).equals("background")) {
            int id = Integer.parseInt(attrs.getAttributeValue(1).substring(1));
            setGifImageResource(id);
        }
        paused = false;
        time = 0;
    }

    public GifPlayer(Context context) {
        super(context);
        this.content = context;
    }

    public GifPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public void pause() {
        paused = true;
    }

    public void play() {
        paused = false;
    }

    private void init() {
        setFocusable(true);
        animation = Movie.decodeStream(stream);
        w = animation.width();
        height = animation.height();

        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(w, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // play the animation
        long now = SystemClock.uptimeMillis();

        if (begin == 0) {
            begin = now;
        }

        if (animation != null) {

            // if paused then dont update the time
            if(!paused) {
                int duration = animation.duration();
                if (duration == 0) {
                    duration = 1000;
                }

                time = (int) ((now - begin) % duration);
            }

            animation.setTime(time);

            animation.draw(canvas, 0, 0);
            invalidate();
        }
    }

    public void setGifImageResource(int id) {
        stream = content.getResources().openRawResource(id);
        init();
    }

    public void setGifImageUri(Uri uri) {
        try {
            stream = content.getContentResolver().openInputStream(uri);
            init();
        } catch (FileNotFoundException e) {
            Log.e("mdp", "Couldn't find gif file");
        }
    }
}
