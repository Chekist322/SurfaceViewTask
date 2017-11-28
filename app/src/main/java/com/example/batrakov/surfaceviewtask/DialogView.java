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
    private Surface mSurface;
    private SurfaceHolder mSurfaceHolder;
    private Thread mTopDrawThread;
    private Thread mBottomDrawThread;

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
        mSurfaceHolder = holder;
        mSurface = holder.getSurface();
        mTopDrawThread = new Thread() {
            @Override
            public void run() {
                int xPosition = -1;
                int yPosition = 0;

                InputStream is = getResources().openRawResource(R.raw.gif);
                Movie movie;
                movie = Movie.decodeStream(is);

                int movieDurationMs = movie.duration();

                int gifTimeMs = 0;

                try {
                    while (!isInterrupted()) {
                        if (gifTimeMs >= movieDurationMs) {
                            gifTimeMs = 0;
                        }
                        movie.setTime(gifTimeMs += 20);
                        drawGif(xPosition, yPosition, movie);
                        sleep(1);
                    }
                } catch (InterruptedException aE) {
                    aE.printStackTrace();
                    Log.i(TAG, "TopDrawThread: interrupted");
                }
            }
        };
        mTopDrawThread.start();

        mBottomDrawThread = new Thread() {
            @Override
            public void run() {
                int xPosition = -1;
                int yPosition = -1;

                InputStream is = getResources().openRawResource(R.raw.gif1);
                Movie movie;
                movie = Movie.decodeStream(is);

                int movieDurationMs = movie.duration();

                int gifTimeMs = 0;

                try {
                    while (!isInterrupted()) {
                        if (gifTimeMs >= movieDurationMs) {
                            gifTimeMs = 0;
                        }
                        movie.setTime(gifTimeMs += 20);
                        drawGif(xPosition, yPosition, movie);
                        sleep(1);
                    }
                } catch (InterruptedException aE) {
                    aE.printStackTrace();
                    Log.i(TAG, "BottomDrawThread: interrupted");
                }
            }
        };
        mBottomDrawThread.start();
    }

    private void drawGif(float aXPosition, float aYPosition, Movie aMovie) {
        synchronized (getActivity()) {
            Canvas canvas = mSurface.lockCanvas(mSurfaceHolder.getSurfaceFrame());
            if ((aXPosition == -1)) {
                aXPosition = (canvas.getWidth() - aMovie.width()) / 2;
            }
            if (aYPosition == -1) {
                aYPosition = (canvas.getHeight() - aMovie.height());
            }

            aMovie.draw(canvas, aXPosition, aYPosition);
            mSurface.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mTopDrawThread != null) {
            try {
                mTopDrawThread.interrupt();
                mTopDrawThread.join();
                mTopDrawThread = null;
            } catch (InterruptedException aE) {
                aE.printStackTrace();
                Log.i(TAG, "TopDrawThread: interruption failed");
            }
        }

        if (mBottomDrawThread != null) {
            try {
                mBottomDrawThread.interrupt();
                mBottomDrawThread.join();
                mBottomDrawThread = null;
            } catch (InterruptedException aE) {
                aE.printStackTrace();
                Log.i(TAG, "BottomDrawThread: interruption failed");
            }
        }
    }
}
