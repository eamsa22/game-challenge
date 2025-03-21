package org.m2sdl.game;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread {
    private SurfaceHolder surfaceHolder;
    private GameView gameView;
    private boolean running;
    private long lastUpdateTime; // Dernière mise à jour en nanosecondes

    public GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
        this.lastUpdateTime = System.nanoTime(); // Initialiser avec le temps actuel en nanosecondes
    }

    public void setRunning(boolean isRunning) {
        running = isRunning;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public void run() {
        while (running) {
            Canvas canvas = null;

            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    // Calcul du deltaTime (temps écoulé entre chaque frame)
                    long currentTime = System.nanoTime();
                    float deltaTime = (currentTime - lastUpdateTime) / 1000000.0f; // Convertir en millisecondes

                    // Mettre à jour le temps de jeu avec le deltaTime
                    gameView.update(deltaTime);

                    // Mettre à jour le temps de la dernière mise à jour
                    setLastUpdateTime(currentTime);

                    // Dessiner la vue du jeu
                    gameView.draw(canvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Ajout d'un délai pour stabiliser la vitesse du jeu (60 FPS)
            try {
                Thread.sleep(16); // Viser 60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
