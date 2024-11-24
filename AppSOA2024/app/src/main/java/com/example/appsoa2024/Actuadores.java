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
    private boolean boolRelayOn = false;
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
                // Lógica para encender el buzzer
                publishMessage(mqttHandler.TOPIC_BUZZER_MUTE,"FALSE");
                btnBuzzerOn.setEnabled(false);
                btnBuzzerOff.setEnabled(true);
            }
        });

        btnBuzzerOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para apagar el buzzer
                publishMessage(mqttHandler.TOPIC_BUZZER_MUTE,"TRUE");
                btnBuzzerOn.setEnabled(false);
                btnBuzzerOff.setEnabled(false);
            }
        });

        // Configurar los botones del Relé
        btnReleOn = findViewById(R.id.btnReleOn);
        btnReleOff = findViewById(R.id.btnReleOff);

        btnReleOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para encender el relé
                publishMessage(mqttHandler.TOPIC_RELAY_MUTE,"FALSE");
                btnReleOn.setEnabled(false);
                btnReleOff.setEnabled(true);
            }
        });

        btnReleOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para apagar el relé
                publishMessage(mqttHandler.TOPIC_RELAY_MUTE,"TRUE");
                btnReleOn.setEnabled(true);
                btnReleOff.setEnabled(false);
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
    private void publishMessage(String topic, String message){
        //Toast.makeText(this, "Publishing message: " + message + "; Topico: " + topic, Toast.LENGTH_SHORT).show();
        mqttHandler.publish(topic,message);
    }
    private void subscribeToTopic(String topic){
        mqttHandler.subscribe(topic);
    }

    private void configureBroadcastReceiver() {
        filterReceive = new IntentFilter(MqttHandler.ACTION_EVENTS_ACTUATOR_STATUS);
        filterReceive.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiverEventoActuador, filterReceive);
    }

    private void cambiarEstadoBuzzer(String value){
        if(value.equals("TRUE")) {
            if(!boolBuzzerOn) { //Si llegó un evento diciendo que esta prendido, y en la app no lo esta, cambiarlo
                cbBuzzerStatus.setChecked(true);
                boolBuzzerOn = true;
                btnBuzzerOff.setEnabled(true);
                btnBuzzerOn.setEnabled(false);
            }
        } else {
            if(boolBuzzerOn) { //Si llegó un evento diciendo que esta apagado, y en la app esta prendido, cambiarlo
                cbBuzzerStatus.setChecked(false);
                boolBuzzerOn = false;
                btnBuzzerOff.setEnabled(false);
                btnBuzzerOn.setEnabled(true);
            }
        }
    }

    private void cambiarEstadoRelay(String value){
        if(value.equals("TRUE")) {
            if(!boolRelayOn) { //Si llegó un evento diciendo que esta prendido, y en la app no lo esta, cambiarlo
                cbRelayStatus.setChecked(true);
                boolRelayOn = true;
                btnReleOff.setEnabled(true);
                btnReleOn.setEnabled(false);
            }
        } else {
            if(boolRelayOn) { //Si llegó un evento diciendo que esta apagado, y en la app esta prendido, cambiarlo
                cbRelayStatus.setChecked(false);
                boolRelayOn = false;
                btnReleOff.setEnabled(false);
                btnReleOn.setEnabled(true);
            }
        }
    }

    private class ReceptorEventoActuador extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String actuatorName = intent.getExtras().keySet().toString();
            String value = intent.getStringExtra(actuatorName);
            switch(actuatorName){
                case "BUZZER":
                    cambiarEstadoBuzzer(value);
                    break;
                case "RELAY":
                    cambiarEstadoRelay(value);
                    break;
                default:
                    //do nothing...
            }
        }
    }
}
