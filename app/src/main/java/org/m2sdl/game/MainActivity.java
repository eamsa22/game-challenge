package org.m2sdl.game;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private Sensor accelerometer;
    private GameView gameView;
    private float lastX, lastY, lastZ;
    private long lastTime;
    private static final int SHAKE_THRESHOLD = 300;

    private MediaPlayer mediaPlayer;
    private float timeInSong = 0;  // Temps écoulé dans la chanson (en secondes)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        gameView = new GameView(this);
        setContentView(gameView); // Afficher GameView

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            GameView.setLightLevel(event.values[0]);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastTime) > 100) {
                long diffTime = currentTime - lastTime;
                lastTime = currentTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

                Log.d("Accéléromètre", "X: " + x + " Y: " + y + " Z: " + z + " Speed: " + speed);

                if (speed > SHAKE_THRESHOLD) {
                    Log.d("Accéléromètre", "🟢 Secousse détectée ! Effet spécial déclenché.");

                    // ✅ Utilisation de gameView pour appeler la méthode
                    if (gameView != null) {
                        gameView.triggerSpecialEffect();
                    }
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            if (lightSensor != null) {
                sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI);
            }
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}
