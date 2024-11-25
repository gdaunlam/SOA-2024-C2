package com.example.appsoa2024;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class Actuadores extends AppCompatActivity{
    private MqttHandler mqttHandler;
    private boolean boolBuzzerOn = false;
    private ReceptorEventoActuador receiverEventoActuador = new ReceptorEventoActuador();
    private CheckBox cbBuzzerStatus;
    private CheckBox cbRelayStatus;
    private Button btnReleOn;
    private Button btnReleOff;
    private Button btnBuzzerOn;
    private Button btnBuzzerOff;
    public IntentFilter filterReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actuadores);

        // Configurar el botón Atrás
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Esto cerrará la actividad actual y volverá a la anterior
            }
        });

        // Configurar los botones del Buzzer
        btnBuzzerOn = findViewById(R.id.btnBuzzerOn);
        btnBuzzerOff = findViewById(R.id.btnBuzzerOff);
        btnBuzzerOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttHandler.publish(mqttHandler.TOPIC_BUZZER_MUTE,"FALSE");
            }
        });
        btnBuzzerOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttHandler.publish(mqttHandler.TOPIC_BUZZER_MUTE,"TRUE");
            }
        });

        // Configurar los botones del Relé
        btnReleOn = findViewById(R.id.btnReleOn);
        btnReleOff = findViewById(R.id.btnReleOff);
        btnReleOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttHandler.publish(mqttHandler.TOPIC_RELAY_MUTE,"FALSE");
            }
        });
        btnReleOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttHandler.publish(mqttHandler.TOPIC_RELAY_MUTE,"TRUE");
            }
        });

        cbBuzzerStatus = (CheckBox) findViewById(R.id.cbBuzzerState);
        cbRelayStatus = (CheckBox) findViewById(R.id.cbReleState);

        //Crear instancia MQTT y suscripcion de los topicos
        mqttHandler = new MqttHandler(getApplicationContext());
        connect();
        configureBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiverEventoActuador);
        super.onDestroy();
    }
    private void connect()
    {
        subscribeToTopic(MqttHandler.TOPIC_RELAY_MUTE);
        subscribeToTopic(MqttHandler.TOPIC_BUZZER_MUTE);
        subscribeToTopic(MqttHandler.TOPIC_ACTUATOR_BUZZER_STATE);
        subscribeToTopic(MqttHandler.TOPIC_ACTUATOR_RELAY_STATE);
        subscribeToTopic(MqttHandler.TOPIC_SMARTPHONES);
    }
    private void subscribeToTopic(String topic){
        mqttHandler.subscribe(topic);
    }
    private void configureBroadcastReceiver() {
        filterReceive = new IntentFilter(MqttHandler.ACTION_EVENTS_ACTUATOR_STATUS);
        filterReceive.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiverEventoActuador, filterReceive);
    }

    private class ReceptorEventoActuador extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            for(String actuatorName : intent.getExtras().keySet()) {
                String value = intent.getStringExtra(actuatorName);
                switch(actuatorName){
                    case "BUZZER":
                        cambiarEstadoBuzzer(value);
                        break;
                    case "RELAY":
                        cambiarEstadoRelay(value);
                        break;
                }
            }
        }
    }
    private void cambiarEstadoBuzzer(String value){
        boolean buzeerON = value.equals("1") && !boolBuzzerOn;
        cbBuzzerStatus.setChecked(buzeerON);
        boolBuzzerOn = buzeerON;
    }
    private void cambiarEstadoRelay(String value){
        boolean relatON = value.equals("1") && !boolBuzzerOn;
        cbRelayStatus.setChecked(relatON);
    }
}
