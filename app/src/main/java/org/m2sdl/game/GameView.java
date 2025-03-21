package org.m2sdl.game;

import android.content.Context;
import android.graphics.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import java.util.*;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread thread;
    private ArrayList<Note> notes;
    private int[] lanes = {225, 400, 585};
    private Button[] buttons;
    private Paint paint, scorePaint;
    private Bitmap background;
    private int score = 0;
    private static final int HIT_WINDOW = 200;
    private static final int NOTE_START_Y = 700;

    private float ambientLight = 50f;
    private boolean isIncreasing = true;
    public static float noteSpawnRate = 0.01f;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);

        background = BitmapFactory.decodeResource(getResources(), R.drawable.img);

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        int screenHeight = metrics.heightPixels;

        notes = new ArrayList<>();
        buttons = new Button[lanes.length];
        int[] colors = {Color.GREEN, Color.RED, Color.YELLOW};

        for (int i = 0; i < lanes.length; i++) {
            buttons[i] = new Button(lanes[i], screenHeight - 120, colors[i]);
        }

        paint = new Paint();
        scorePaint = new Paint();
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextSize(30);
        scorePaint.setFakeBoldText(true);

        thread = new GameThread(getHolder(), this);
    }

    public void setLightLevelExternally(float level) {
        this.ambientLight = level;
    }

    public void triggerSpecialEffect() {
        noteSpawnRate = isIncreasing ? 0.1f : 0.01f;
        isIncreasing = !isIncreasing;
        flashScreen();
    }

    private void flashScreen() {
        new Thread(() -> {
            Paint flashPaint = new Paint();
            flashPaint.setColor(Color.WHITE);
            flashPaint.setAlpha(150);

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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    @Override public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) { e.printStackTrace(); }
            retry = false;
        }
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

            canvas.drawText("Score: " + score, 50, 80, scorePaint);
        }
    }

    public void update(float deltaTime) {
        float speedMultiplier = Math.min(3.0f, Math.max(0.5f, 0.5f + ambientLight / 5f));
        Log.d("Light", "ambientLight: " + ambientLight + " | speed: " + speedMultiplier);

        for (Note note : notes) {
            note.update(deltaTime, speedMultiplier);
        }

        if (Math.random() < noteSpawnRate) {
            int lane = (int) (Math.random() * lanes.length);
            notes.add(new Note(lanes[lane], NOTE_START_Y, lane));
        }

        notes.removeIf(note -> note.getY() > getHeight());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            checkClick((int) event.getX(), (int) event.getY());
        }
        return true;
    }

    private void checkClick(int x, int y) {
        for (int i = 0; i < buttons.length; i++) {
            Button button = buttons[i];
            if (isClickOnButton(x, y, button)) {
                boolean hit = false;
                Note toRemove = null;

                for (Note note : notes) {
                    if (note.getLane() == i && Math.abs(note.getY() - button.getY()) <= HIT_WINDOW) {
                        score += 100;
                        hit = true;
                        toRemove = note;
                        break;
                    }
                }

                if (!hit) score -= 25;
                if (toRemove != null) notes.remove(toRemove);
            }
        }
    }

    private boolean isClickOnButton(int x, int y, Button button) {
        int dx = x - button.getX();
        int dy = y - button.getY();
        return dx * dx + dy * dy <= button.getRadius() * button.getRadius();
    }

    public class Button {
        private int x, y, radius = 55, color;
        public Button(int x, int y, int color) {
            this.x = x; this.y = y; this.color = color;
        }
        public void draw(Canvas canvas, Paint paint) {
            paint.setColor(color);
            canvas.drawCircle(x, y, radius, paint);
        }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getRadius() { return radius; }
    }
}
