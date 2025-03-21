package org.m2sdl.game;

public class Note {

    private int x, y, lane;
    private int color;
    private static final int BASE_SPEED = 3;
    private static final float MAX_SPEED = 20.0f;
    private static final float MIN_SPEED = 3.0f;
    private static float gameTime = 0;

    public Note(int x, int y, int lane) {
        this.x = x;
        this.y = y;
        this.lane = lane;
        this.color = getColorForLane(lane);
    }

    public void update(float deltaTime, float speedMultiplier) {
        y += 5.0f * speedMultiplier;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getLane() { return lane; }
    public int getColor() { return color; }

    private int getColorForLane(int lane) {
        switch (lane) {
            case 0: return 0xFF00FF00;
            case 1: return 0xFFFF0000;
            case 2: return 0xFFFFFF00;
            default: return 0xFFFFFFFF;
        }
    }
}

