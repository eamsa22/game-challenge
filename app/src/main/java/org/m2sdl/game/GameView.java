package org.m2sdl.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread thread;
    private ArrayList<Note> notes;
    private int[] lanes;
    private Paint paint;
    private Button[] buttons;

    private Bitmap background;
    private int score = 0;
    private static float lightLevel = 0.5f;
    public static float noteSpawnRate = 0.01f;
    private boolean isIncreasing = true;


    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);

        background = BitmapFactory.decodeResource(getResources(), R.drawable.img);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        Log.d("GameView", "Screen Width: " + screenWidth);
        Log.d("GameView", "Screen Height: " + screenHeight);

        notes = new ArrayList<>();
        lanes = new int[]{225, 400, 585};
        buttons = new Button[lanes.length];
        int[] laneColors = {
                Color.GREEN,
                Color.RED,
                Color.YELLOW
        };

        for (int i = 0; i < lanes.length; i++) {
            buttons[i] = new Button(lanes[i], screenHeight - 120, laneColors[i]);
        }

        paint = new Paint();
        paint.setColor(Color.RED);

        thread = new GameThread(getHolder(), this);
    }

    public void triggerSpecialEffect() {
        if (isIncreasing) {
            noteSpawnRate = 0.1f;
        } else {
            noteSpawnRate = 0.01f;
        }


        isIncreasing = !isIncreasing;
        flashScreen();
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

    public static void setLightLevel(float level) {
        lightLevel = level;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawBitmap(Bitmap.createScaledBitmap(background, getWidth(), getHeight(), false), 0, 0, null);

            for (Note note : notes) {
                paint.setColor(note.getColor());
                canvas.drawCircle(note.getX(), note.getY(), 25, paint);
            }

            for (Button button : buttons) {
                button.draw(canvas, paint);
            }
        }
    }

    public void update(float deltaTime) {
        for (Note note : notes) {
            note.update(deltaTime, lightLevel);
        }


        if (Math.random() < noteSpawnRate) {
            int lane = (int) (Math.random() * lanes.length);
            notes.add(new Note(lanes[lane], 0, lane));
        }

        ArrayList<Note> toRemove = new ArrayList<>();
        for (Note note : notes) {
            if (note.getY() > getHeight()) {
                toRemove.add(note);
            }
        }
        notes.removeAll(toRemove);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int touchX = (int) event.getX();
            int touchY = (int) event.getY();
            Log.d("GameView", "checkClick called at (" + touchX + ", " + touchY + ")");
            checkClick(touchX, touchY);
        }
        return true;
    }

    public void checkClick(int x, int y) {
        for (int i = 0; i < buttons.length; i++) {
            Button button = buttons[i];
            if (isClickOnButton(x, y, button)) {
                for (Note note : notes) {
                    if (note.getLane() == i && note.getY() <= button.getY()) {
                        if (note.getY() >= button.getY() - 100 && note.getY() <= button.getY() + 100) {
                            this.score += 100; // Bonus
                        }
                    }
                }
            }
        }
    }

    private boolean isClickOnButton(int x, int y, Button button) {
        int dx = x - button.getX();
        int dy = y - button.getY();
        return (dx * dx + dy * dy <= button.getRadius() * button.getRadius());
    }

    private void flashScreen() {
        new Thread(() -> {
            Paint flashPaint = new Paint();
            flashPaint.setColor(Color.WHITE);
            flashPaint.setAlpha(150); // Semi-transparent

            Canvas canvas = getHolder().lockCanvas();
            if (canvas != null) {
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), flashPaint);
                getHolder().unlockCanvasAndPost(canvas);
            }

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}

            Canvas normalCanvas = getHolder().lockCanvas();
            if (normalCanvas != null) {
                draw(normalCanvas);
                getHolder().unlockCanvasAndPost(normalCanvas);
            }
        }).start();
    }

    public class Button {
        private int x, y;
        private int radius = 55;
        private int color;

        public Button(int x, int y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public void draw(Canvas canvas, Paint paint) {
            paint.setColor(color);
            canvas.drawCircle(x, y, radius, paint);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }
    }
}
