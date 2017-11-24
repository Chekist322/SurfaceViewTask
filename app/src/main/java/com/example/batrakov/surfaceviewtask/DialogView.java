package com.example.batrakov.surfaceviewtask;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by batrakov on 24.11.17.
 */

public class DialogView extends DialogFragment implements SurfaceHolder.Callback {

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
        surfaceView.getHolder().addCallback(this);
        getDialog().setTitle("Test dialog.");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        DisplayMetrics display = getResources().getDisplayMetrics();
        getDialog().getWindow().setLayout((int)(display.widthPixels * 0.9), WindowManager.LayoutParams.MATCH_PARENT);

    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        mDrawThread = new Thread() {
            @Override
            public void run() {
                long movieStart = 0;
                InputStream is = getResources().openRawResource(R.raw.gif);
                Surface surface = holder.getSurface();
                Movie movie;
                byte[] array = streamToBytes(is);
                movie = Movie.decodeByteArray(array, 0, array.length);
                long now = android.os.SystemClock.uptimeMillis();
                try {
                    while (!isInterrupted()) {
                        if (movieStart == 0) {   // first time
                            movieStart = now;
                        }

                        int dur = movie.duration();

                        if (dur == 0)
                        {
                            dur = 1000;
                        }
                        int relTime = (int)((now - movieStart) % dur);
                        movie.setTime(relTime);
                        Canvas canvas = surface.lockCanvas(holder.getSurfaceFrame());
                        movie.draw(canvas,120,100);
                        surface.unlockCanvasAndPost(canvas);
                        sleep(50);
                    }
                } catch (InterruptedException ignored) {
                }
            }
        };
        mDrawThread.start();
    }


    private static byte[] streamToBytes(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = is.read(buffer)) >= 0) {
                os.write(buffer, 0, len);
            }
        } catch (java.io.IOException e) {
        }
        return os.toByteArray();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mDrawThread != null) {
            mDrawThread.interrupt();
            mDrawThread = null;
        }

    }
}
