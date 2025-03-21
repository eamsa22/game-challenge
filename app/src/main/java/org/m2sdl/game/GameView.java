package org.m2sdl.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread thread;
    private ArrayList<Note> notes; // Liste des notes à afficher
    private int[] lanes; // Positions des pistes
    private Paint paint;
    private static float lightLevel = 0.5f; // Niveau de luminosité initial

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);

        notes = new ArrayList<>();
        lanes = new int[]{200, 400, 600}; // 5 pistes
        paint = new Paint();
        paint.setColor(Color.RED);

        thread = new GameThread(getHolder(), this);
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
        lightLevel = level; // Mettre à jour la luminosité
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(Color.BLACK); // Fond noir

            // Dessiner les notes
            for (Note note : notes) {
                paint.setColor(note.getColor()); // Définir la couleur de la note
                canvas.drawCircle(note.getX(), note.getY(), 25, paint); // Dessiner un cercle (note)
            }

            // Afficher les lignes de guitare
            paint.setColor(Color.WHITE);
            for (int lane : lanes) {
                canvas.drawLine(lane, 0, lane, getHeight(), paint); // Dessiner les lignes
            }
        }
    }

    public void update(float deltaTime) {
        // Mettre à jour les positions des notes en fonction du deltaTime
        for (Note note : notes) {
            note.update(deltaTime, lightLevel); // Passer le deltaTime à la méthode update de Note
        }

        // Générer de nouvelles notes avec une probabilité de 1% à chaque mise à jour
        if (Math.random() < 0.01) {
            int lane = (int) (Math.random() * lanes.length);
            notes.add(new Note(lanes[lane], 0, lane));
        }

        // Enlever les notes qui sont sorties de l'écran
        ArrayList<Note> toRemove = new ArrayList<>();
        for (Note note : notes) {
            if (note.getY() > getHeight()) {
                toRemove.add(note);
            }
        }
        notes.removeAll(toRemove);
    }
}
