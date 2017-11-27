package com.example.batrakov.surfaceviewtask;

import android.app.DialogFragment;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.InputStream;

/**
 * Dialog include SurfaceView and Button to self dismiss.
 */
public class DialogView extends DialogFragment implements SurfaceHolder.Callback {

    private static final String TAG = DialogView.class.getSimpleName();
    private Thread mDrawThread;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_layout, container);
        view.findViewById(R.id.mainButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        SurfaceView surfaceView = view.findViewById(R.id.surfaceView);
        surfaceView.setZOrderOnTop(true);

        surfaceView.getHolder().addCallback(this);
        getDialog().setTitle("Gif dialog.");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        mDrawThread = new Thread() {
            @Override
            public void run() {
                int startX = 0;
                int startY = 0;

                InputStream is = getResources().openRawResource(R.raw.gif);
                Surface surface = holder.getSurface();
                Movie movie;
                movie = Movie.decodeStream(is);
                int movieDuration = movie.duration();
                int gifTime = 0;
                try {
                    while (surface.isValid()) {
                        if (gifTime >= movieDuration) {
                            gifTime = 0;
                        }
                        movie.setTime(gifTime += 50);

                        Canvas canvas = surface.lockCanvas(holder.getSurfaceFrame());

                        if (startX == 0) {
                            startX = (canvas.getWidth() - movie.width()) / 2;
                            startY = (canvas.getHeight() - movie.height()) / 2;
                        }

                        movie.draw(canvas, startX, startY);
                        surface.unlockCanvasAndPost(canvas);

                        sleep(40);
                    }
                } catch (InterruptedException aE) {
                    aE.printStackTrace();
                    Log.i(TAG, "DrawThread: interrupted");
                }
            }
        };
        mDrawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mDrawThread != null) {
            try {
                mDrawThread.interrupt();
                mDrawThread.join();
                mDrawThread = null;
            } catch (InterruptedException aE) {
                aE.printStackTrace();
                Log.i(TAG, "DrawThread: interruption failed");
            }
        }
    }
}
