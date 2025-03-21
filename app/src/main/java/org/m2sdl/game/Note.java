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

    public static void updateGameTime(float deltaTime) {
        gameTime += deltaTime;
    }

    public void update(float lightLevel, float level) {
        float speedMultiplier = MIN_SPEED + ((gameTime / 1000) * (MAX_SPEED - MIN_SPEED));

        speedMultiplier += lightLevel * 0.5f;

        this.y += BASE_SPEED + speedMultiplier;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getColor() {
        return color;
    }

    private int getColorForLane(int lane) {
        switch (lane) {
            case 0: return 0xFF00FF00;
            case 1: return 0xFFFF0000;
            case 2: return 0xFFFFFF00;
            default: return 0xFFFFFFFF;
        }
    }

    public int getLane() {
        return lane;
    }
}
