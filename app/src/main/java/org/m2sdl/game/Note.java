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
        gameTime += deltaTime; // Augmenter le temps de jeu pour suivre la progression
    }

    public void update(float lightLevel, float level) {
        // La vitesse des notes augmente progressivement en fonction du temps de jeu
        float speedMultiplier = MIN_SPEED + ((gameTime / 1000) * (MAX_SPEED - MIN_SPEED)); // Augmentation progressive de la vitesse

        // Appliquer un ajustement très léger en fonction de la luminosité
        speedMultiplier += lightLevel * 0.5f; // Facteur de luminosité faible pour ajuster la vitesse

        // Calculer la nouvelle position
        this.y += BASE_SPEED + speedMultiplier; // Ajouter la vitesse ajustée
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
            case 0: return 0xFFFF0000; // Rouge
            case 1: return 0xFF00FF00; // Vert
            case 2: return 0xFF0000FF; // Bleu
            case 3: return 0xFFFFFF00; // Jaune
            case 4: return 0xFF00FFFF; // Cyan
            default: return 0xFFFFFFFF; // Blanc par défaut
        }
    }
}
