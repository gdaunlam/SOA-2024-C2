package com.example.appsoa2024;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;

public class Actuadores extends AppCompatActivity implements SensorEventListener {

    private final static float ACC = 30;
    private MediaPlayer mplayer;
    private SensorManager sensor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actuadores);

        //Configurar el sensor
        sensor = (SensorManager) getSystemService(SENSOR_SERVICE);

        mplayer = MediaPlayer.create(this, R.raw.audio_alarma);
        mplayer.setOnPreparedListener (
                new OnPreparedListener()
                {
                    public void onPrepared(MediaPlayer arg0)
                    {
                        Log.e("ready!","ready!");
                        mplayer.setVolume(1.0f, 1.0f);
                    }
                }
        );

        // Configurar el botón Atrás
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Esto cerrará la actividad actual y volverá a la anterior
            }
        });

        // Configurar los botones del Buzzer
        Button btnBuzzerOn = findViewById(R.id.btnBuzzerOn);
        btnBuzzerOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para encender el buzzer
            }
        });

        Button btnBuzzerOff = findViewById(R.id.btnBuzzerOff);
        btnBuzzerOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para apagar el buzzer
            }
        });

        // Configurar los botones del Relé
        Button btnReleOn = findViewById(R.id.btnReleOn);
        btnReleOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para encender el relé
            }
        });

        Button btnReleOff = findViewById(R.id.btnReleOff);
        btnReleOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para apagar el relé
            }
        });

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerSenser();

        // Inicializar el MediaPlayer y establecer el volumen
        mplayer = MediaPlayer.create(this, R.raw.audio_alarma);
        if (mplayer != null) {
            mplayer.setVolume(1.0f, 1.0f);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        unregisterSenser();
    }

    @Override
    protected void onPause()
    {
        unregisterSenser();
        // Liberar el MediaPlayer
        if (mplayer != null) {
            mplayer.release();
            mplayer = null;
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mplayer != null) {
            mplayer.release();
            mplayer = null;
        }
        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {

        int sensorType = event.sensor.getType();

        float[] values = event.values;

        if (sensorType == Sensor.TYPE_ACCELEROMETER)
        {
            if ((Math.abs(values[0]) > ACC || Math.abs(values[1]) > ACC || Math.abs(values[2]) > ACC))
            {
                Log.i("sensor", "running");
                //Chequeo para que se reproduzca solamente si no esta activado
                if (!mplayer.isPlaying()) {
                    mplayer.start();
                }
            }
        }
    }

    private void registerSenser()
    {
        boolean done;
        done = sensor.registerListener(this, sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        /*if (!done)
        {
            Toast.makeText(this, getResources().getString(R.string.sensor_unsupported), Toast.LENGTH_SHORT).show();
            switchButton.setChecked(false);
        }*/

        Log.i("sensor", "register");
    }

    private void unregisterSenser()
    {
        sensor.unregisterListener(this);
        Log.i("sensor", "unregister");
    }

}
