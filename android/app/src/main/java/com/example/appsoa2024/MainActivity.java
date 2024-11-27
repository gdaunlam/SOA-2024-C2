package com.example.appsoa2024;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.content.IntentFilter;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private final static float ACELEROMETER_MAX_VALUE_TO_SONG = 30;
    private SensorManager sensor;
    private MediaPlayer mplayer;
    private MqttHandler mqttHandler;
    public IntentFilter filterReceive;
    public IntentFilter filterConnectionLost;
    public IntentFilter filterSensorValues;
    public IntentFilter filterSmartphoneEvent;
    private EventsReceiver receiverEvents = new EventsReceiver();
    private ConnectionLost connectionLost = new ConnectionLost();
    private SensorsValuesReceiver receiverSensors = new SensorsValuesReceiver();
    private AlarmReceiver receiverAlarm = new AlarmReceiver();
    private TextView txtLastUpdate;
    private TextView txtTemperature;
    private TextView txtHumidity;
    private TextView txtCO2;
    private TextView txtStateDoor;
    private TextView txtStateEmbed;
    private CheckBox mqttCheckBox;
    private RecyclerView rvMessages;
    private MessageAdapter messageAdapter;
    private List<String> messages = new ArrayList<>();
    private final static String ORANGE = "#FF9800";
    private final Hashtable<String, Integer> statesColors = new Hashtable<String, Integer>() {{
        put("LOW", Color.GREEN);
        put("MEDIUM", Color.YELLOW);
        put("HIGH", Color.parseColor(ORANGE));
        put("CRITICAL", Color.RED);
    }};

    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Logica para ir a la pantalla de actuadores
        Button btnIrActuadores = findViewById(R.id.btnIrActuadores);

        btnIrActuadores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Actuators.class);
                startActivity(intent);
            }
        });

        //Configurar el sensor
        sensor = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerSensor();
        mplayer = MediaPlayer.create(this, R.raw.audio_alarma);
        mplayer.setOnPreparedListener(
                new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer arg0) {
                        double VOLUME = 1.0;
                        mplayer.setVolume((float) VOLUME, (float) VOLUME);
                    }
                }
        );

        // Inicializar RecyclerView
        rvMessages = findViewById(R.id.rvMessages);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));

        // Configurar adaptador
        messageAdapter = new MessageAdapter(messages);
        rvMessages.setAdapter(messageAdapter);
        adjustRecyclerViewHeight();

        //Inicializo resto de los textviews
        txtLastUpdate = (TextView) findViewById(R.id.tvUltimaActualizacion);
        txtTemperature = (TextView) findViewById(R.id.tvTemperaturaValor);
        txtCO2 = (TextView) findViewById(R.id.tvCO2Valor);
        txtHumidity = (TextView) findViewById(R.id.tvHumedadValor);
        txtStateDoor = (TextView) findViewById(R.id.tvPuertaValor);
        txtStateEmbed = (TextView) findViewById(R.id.tvEstado);
        mqttCheckBox = (CheckBox) findViewById(R.id.mqttCheckBox);

        //Crear instancia MQTT
        mqttHandler = new MqttHandler(getApplicationContext());
        connect();
        configureBroadcastReceiver();
        updateDateAndHour();
    }

    @Override
    protected void onDestroy() {
        mqttHandler.disconnect();
        super.onDestroy();
        sensor.unregisterListener(this);
        unregisterReceiver(receiverEvents);
        unregisterReceiver(receiverSensors);
        unregisterReceiver(receiverAlarm);
        unregisterReceiver(connectionLost);
    }

    @Override
    protected void onResume() {
        registerSensor();
        super.onResume();
        mplayer = MediaPlayer.create(this, R.raw.audio_alarma);
        if (mplayer != null) {
            double VOLUME = 1.0;
            mplayer.setVolume((float) VOLUME, (float) VOLUME);
        }
    }

    private void connect() {
        try {
            mqttHandler.connect();
            Toast.makeText(getApplicationContext(), "Conexion establecida", Toast.LENGTH_SHORT).show();
        } catch (MqttException e) {
            long DELAY_MILLIS = 500;
            Log.d("Aplicacion", e.getMessage() + "  " + e.getCause());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    connect();
                }
            }, DELAY_MILLIS);
        }
        mqttHandler.subscribe(MqttHandler.TOPIC_SMARTPHONES);
        mqttHandler.subscribe(MqttHandler.TOPIC_SENSORS_EVENTS);
        mqttHandler.subscribe(MqttHandler.TOPIC_SENSORS_VALUES);
        mqttCheckBox.setChecked(true);
    }

    private void configureBroadcastReceiver() {
        filterReceive = new IntentFilter(MqttHandler.ACTION_EVENTS_RECEIVE);
        filterConnectionLost = new IntentFilter(MqttHandler.ACTION_CONNECTION_LOST);
        filterSensorValues = new IntentFilter(MqttHandler.ACTION_VALUES_RECEIVE);
        filterSmartphoneEvent = new IntentFilter(MqttHandler.ACTION_EVENTS_SMARTPHONES);

        filterSmartphoneEvent.addCategory(Intent.CATEGORY_DEFAULT);
        filterReceive.addCategory(Intent.CATEGORY_DEFAULT);
        filterConnectionLost.addCategory(Intent.CATEGORY_DEFAULT);
        filterSensorValues.addCategory(Intent.CATEGORY_DEFAULT);

        registerReceiver(receiverAlarm, filterSmartphoneEvent);
        registerReceiver(receiverEvents, filterReceive);
        registerReceiver(connectionLost, filterConnectionLost);
        registerReceiver(receiverSensors, filterSensorValues);
    }

    private void updateDateAndHour() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String currentDateTime = sdf.format(new Date());
        txtLastUpdate.setText("Ultima actualizacion: " + currentDateTime);
    }

    //Clases
    public class ConnectionLost extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), "Conexion Perdida", Toast.LENGTH_SHORT).show();
            mqttCheckBox.setChecked(false);
            connect();
        }
    }

    private class AlarmReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (!mplayer.isPlaying()) {
                mplayer.start();
            }
        }
    }

    private class EventsReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            for (String key : intent.getExtras().keySet()) {
                String message = intent.getStringExtra(key).split("=")[1];
                String value = intent.getStringExtra(key).split("=")[0];

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String currentDateTime = sdf.format(new Date());
                addMessageToRecyclerView(message + " " + currentDateTime);
                updateDateAndHour();

                if (key.equals("STATE") && Arrays.asList("critical", "high").contains(value)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        String channelId = "states_notification";
                        CharSequence channelName = "My App Notifications";
                        int importance = NotificationManager.IMPORTANCE_DEFAULT;
                        NotificationChannel channel = new NotificationChannel(channelId,
                                channelName, importance);
                        NotificationManager notificationManager = (NotificationManager)
                                context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.createNotificationChannel(channel);
                    }

                    int notificationId = (int) System.currentTimeMillis();

                    // Build the notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                            "states_notification")
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle("App Notification")
                            .setContentText(message)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    // Send the notification
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(notificationId, builder.build());
                }
            }
        }
    }

    private void addMessageToRecyclerView(String message) {
        int POSITION = 0;
        messages.add(POSITION, message);
        messageAdapter.notifyItemInserted(POSITION);
        rvMessages.scrollToPosition(POSITION);
        adjustRecyclerViewHeight();
    }

    private void adjustRecyclerViewHeight() {
        rvMessages.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int CHILD_COUNT = 0;
                if (rvMessages.getChildCount() > CHILD_COUNT) {
                    int INDEX = 0;
                    int ROWS = 5;
                    View listItem = rvMessages.getChildAt(INDEX);
                    int itemHeight = listItem.getHeight();
                    int totalHeight = itemHeight * ROWS;

                    // Ajustar la altura del RecyclerView
                    ViewGroup.LayoutParams params = rvMessages.getLayoutParams();
                    params.height = totalHeight;
                    rvMessages.setLayoutParams(params);

                    // Elimina el listener después de ajustar la altura
                    rvMessages.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    private class SensorsValuesReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            runOnUiThread(() -> {
                for (String extraName : intent.getExtras().keySet()) {
                    String sensorValue = intent.getStringExtra(extraName);
                    switch (extraName) {
                        case "CO2":
                            txtCO2.setText(sensorValue + " ppm");
                            break;
                        case "DIST":
                            double DISTANCE = 5;
                            if (Float.parseFloat(sensorValue) > (float) DISTANCE) {
                                txtStateDoor.setText("ABIERTA");
                            } else {
                                txtStateDoor.setText("CERRADA");
                            }
                            break;
                        case "HUM":
                            txtHumidity.setText(sensorValue + " %");
                            break;
                        case "TEMP":
                            txtTemperature.setText(sensorValue + " °C");
                            break;
                        case "STATE":
                            txtStateEmbed.setText(sensorValue);
                            Integer color = statesColors.get(sensorValue);
                            if (color != null) {
                                txtStateEmbed.setTextColor(color);
                            }
                            break;
                        default:
                            break;
                    }
                }
                updateDateAndHour();
            });
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        float[] values = event.values;
        boolean accelerometerActivated = sensorType == Sensor.TYPE_ACCELEROMETER && (Math.abs(values[0]) > ACELEROMETER_MAX_VALUE_TO_SONG || Math.abs(values[1]) > ACELEROMETER_MAX_VALUE_TO_SONG || Math.abs(values[2]) > ACELEROMETER_MAX_VALUE_TO_SONG);
        if (accelerometerActivated) {
            mqttHandler.publish(MqttHandler.TOPIC_SMARTPHONES, "ALARMA");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void registerSensor() {
        sensor.registerListener(this, sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }
}