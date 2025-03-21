package org.m2sdl.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread thread;
    private ArrayList<Note> notes; // Liste des notes à afficher
    private int[] lanes; // Positions des pistes
    private Paint paint;
    private Button[] buttons; // Liste des boutons en bas de l'écran


    private static float lightLevel = 0.5f; // Niveau de luminosité initial

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
        // Obtenir les dimensions de l'écran
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        int screenWidth = metrics.widthPixels;  // Largeur de l'écran
        int screenHeight = metrics.heightPixels; // Hauteur de l'écran

        // Afficher les dimensions de l'écran
        Log.d("GameView", "Screen Width: " + screenWidth);
        Log.d("GameView", "Screen Height: " + screenHeight);
        notes = new ArrayList<>();
        lanes = new int[]{200, 400, 600}; // 5 pistes
        buttons = new Button[lanes.length];
       int[] laneColors = {
                Color.RED,    // Couleur pour la lane 1
                Color.GREEN,  // Couleur pour la lane 2
                Color.BLUE    // Couleur pour la lane 3
        };
        for (int i = 0; i < lanes.length; i++) {
            buttons[i] = new Button(lanes[i], screenHeight-30, laneColors[i]);
        }
        Log.d("GameView", "Hauteur de l'écran : " + getHeight());

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
            Log.d("GameView", "Dessin en cours...");

            canvas.drawColor(Color.BLACK); // Fond noir

            // Dessiner les notes
            for (Note note : notes) {
                paint.setColor(note.getColor()); // Définir la couleur de la note
                canvas.drawCircle(note.getX(), note.getY(), 25, paint); // Dessiner un cercle (note)
            }

            // Dessiner les boutons en bas
            for (Button button : buttons) {
                button.draw(canvas, paint); // Dessiner chaque bouton
                Log.d("GameView", "Button drawn at: (" + button.getX() + ", " + button.getY() + ")");
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
    public class Button {
        private int x, y; // Position du bouton
        private int width = 100, height = 50; // Taille du bouton
        private int color; // Couleur du bouton

        public Button(int x, int y, int color) {
            this.x = x;
            this.y = y;
            this.color = color; // Affecter la couleur
        }

        public void draw(Canvas canvas, Paint paint) {
            paint.setColor(color); // Appliquer la couleur du bouton
            canvas.drawRect(x - width / 2, y - height / 2, x + width / 2, y + height / 2, paint); // Dessiner un rectangle pour le bouton
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

}
