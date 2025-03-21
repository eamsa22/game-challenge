package org.m2sdl.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread thread;
    private int x = 0;
    private int y;
    private Bitmap background;

    private TextView scoreText;
    private ConstraintLayout parentLayout;

    private Button[] buttons;
    private int[] lanes = {225, 400, 585}; // X positions
    private int[] laneColors = {Color.GREEN, Color.RED, Color.YELLOW};

    public GameView(Context context, ConstraintLayout parentLayout) {
        super(context);
        getHolder().addCallback(this);
        setZOrderOnTop(true);
        setFocusable(true);

        this.parentLayout = parentLayout;

        background = BitmapFactory.decodeResource(getResources(), R.drawable.guitarhero);

        SharedPreferences sharedPref = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
        y = sharedPref.getInt("valeur_y", 0);
        y = (y + 100) % 400;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("valeur_y", y);
        editor.apply();

        setupUI(context);
        thread = new GameThread(getHolder(), this);
    }

    private void setupUI(Context context) {
        BitmapDrawable drawable = new BitmapDrawable(getResources(), background);
        parentLayout.setBackground(drawable);

        scoreText = new TextView(context);
        scoreText.setText("Score: 0");
        scoreText.setTextSize(24);
        scoreText.setTextColor(Color.WHITE);
        scoreText.setId(View.generateViewId());
        scoreText.setElevation(10f);

        LayoutParams scoreParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        scoreParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        scoreParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        scoreParams.setMargins(40, 32, 0, 0);
        scoreText.setLayoutParams(scoreParams);
        parentLayout.addView(scoreText);

        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        buttons = new Button[lanes.length];
        for (int i = 0; i < lanes.length; i++) {
            buttons[i] = new Button(lanes[i], screenHeight - 150, laneColors[i]);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawBitmap(
                    Bitmap.createScaledBitmap(background, getWidth(), getHeight(), false),
                    0, 0, null
            );

            Paint paint = new Paint();

            // Moving red square
            paint.setColor(Color.RED);
            canvas.drawRect(x, y, x + 100, y + 100, paint);

            // Draw buttons
            for (Button b : buttons) {
                b.draw(canvas, paint);
            }
        }
    }

    public void update() {
        x = (x + 5) % getWidth();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                for (Button b : buttons) {
                    b.setPressed(b.contains(touchX, touchY));
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                for (Button b : buttons) {
                    b.setPressed(false);
                }
                break;
        }

        return true;
    }

    // Custom circle button
    private class Button {
        private int x, y;
        private int radius = 60;
        private int activeColor;
        private boolean isPressed = false;

        public Button(int x, int y, int activeColor) {
            this.x = x;
            this.y = y;
            this.activeColor = activeColor;
        }

        public void draw(Canvas canvas, Paint paint) {
            paint.setColor(isPressed ? activeColor : Color.argb(0, 0, 0, 0));
            canvas.drawCircle(x, y, radius, paint);
        }

        public void setPressed(boolean pressed) {
            isPressed = pressed;
        }

        public boolean contains(float touchX, float touchY) {
            float dx = touchX - x;
            float dy = touchY - y;
            return dx * dx + dy * dy <= radius * radius;
        }
    }
}
