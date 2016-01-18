package com.dision.android.hudrecyclebin.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.dision.android.hudrecyclebin.R;

public class ShowHudService extends Service {

    // constants
    public static final String BASIC_TAG = ShowHudService.class.getName();

    // variables
    private WindowManager mWindowManager;
    private Vibrator mVibrator;
    private WindowManager.LayoutParams mPaperParams;
    private WindowManager.LayoutParams mRecycleBinParams;
    private int windowHeight;
    private int windowWidth;

    // UI
    private ImageView ivCrumpledPaper;
    private ImageView ivRecycleBin;

    // get intent methods
    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, ShowHudService.class);

        return intent;
    }

    // methods
    @Override
    public void onCreate() {
        super.onCreate();

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showHud();

        return START_STICKY;
    }

    private void showHud() {
        if (ivCrumpledPaper != null) {
            mWindowManager.removeView(ivCrumpledPaper);
            ivCrumpledPaper = null;
        }

        mPaperParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displaymetrics);
        windowHeight = displaymetrics.heightPixels;
        windowWidth = displaymetrics.widthPixels;

        mPaperParams.gravity = Gravity.TOP | Gravity.RIGHT;

        ivCrumpledPaper = new ImageView(this);
        ivCrumpledPaper.setImageResource(R.drawable.ic_crumpled_paper);

        mPaperParams.x = 0;
        mPaperParams.y = 100;

        mWindowManager.addView(ivCrumpledPaper, mPaperParams);
        addCrumpledPaperOnTouchListener();
    }

    private void addCrumpledPaperOnTouchListener() {
        ivCrumpledPaper.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = mPaperParams.x;
                        initialY = mPaperParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        // add recycle bin when moving crumpled paper
                        addRecycleBinView();

                        return true;
                    case MotionEvent.ACTION_UP:

                        int centerOfScreenByX = windowWidth / 2;

                        // remove crumpled paper when the it is in the recycle bin's area
                        if ((mPaperParams.y > windowHeight - ivRecycleBin.getHeight() - ivCrumpledPaper.getHeight()) &&
                                ((mPaperParams.x > centerOfScreenByX - ivRecycleBin.getWidth() - ivCrumpledPaper.getWidth() / 2) && (mPaperParams.x < centerOfScreenByX + ivRecycleBin.getWidth() / 2))) {
                            mWindowManager.removeView(ivCrumpledPaper);
                            ivCrumpledPaper = null;
                            mVibrator.vibrate(100);
                        }

                        // always remove recycle bin ImageView when paper is dropped
                        mWindowManager.removeView(ivRecycleBin);
                        ivRecycleBin = null;

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        // move paper ImageView
                        mPaperParams.x = initialX + (int) (initialTouchX - event.getRawX());
                        mPaperParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(ivCrumpledPaper, mPaperParams);
                        return true;
                }
                return false;
            }
        });
    }

    private void addRecycleBinView() {
        // add recycle bin ImageView centered on the bottom of the screen
        mRecycleBinParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        mRecycleBinParams.gravity = Gravity.BOTTOM | Gravity.CENTER;

        ivRecycleBin = new ImageView(this);
        ivRecycleBin.setImageResource(R.drawable.ic_recycle_bin);

        mRecycleBinParams.x = 0;
        mRecycleBinParams.y = 0;

        mWindowManager.addView(ivRecycleBin, mRecycleBinParams);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // remove views on destroy!
        if (ivCrumpledPaper != null) {
            mWindowManager.removeView(ivCrumpledPaper);
            ivCrumpledPaper = null;
        }

        if (ivRecycleBin != null) {
            mWindowManager.removeView(ivRecycleBin);
            ivRecycleBin = null;
        }
    }
}
