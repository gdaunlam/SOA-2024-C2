package com.example.appsoa2024;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class Actuators extends AppCompatActivity {
    private MqttHandler mqttHandler;
    private ActuatorEventReceiver actuatorEventReceiver = new ActuatorEventReceiver();
    private final static String ON = "1";
    private CheckBox cbBuzzerStatus;
    private CheckBox cbRelayStatus;
    private Button buttonReleOn;
    private Button buttonReleOff;
    private Button buttonBuzzerOn;
    private Button buttonBuzzerOff;
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
                finish();
            }
        });

        // Configurar los botones del Buzzer
        buttonBuzzerOn = findViewById(R.id.btnBuzzerOn);
        buttonBuzzerOff = findViewById(R.id.btnBuzzerOff);

        buttonBuzzerOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttHandler.publish(mqttHandler.TOPIC_BUZZER_MUTE, "FALSE");
            }
        });

        buttonBuzzerOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttHandler.publish(mqttHandler.TOPIC_BUZZER_MUTE, "TRUE");
            }
        });

        // Configurar los botones del Relé
        buttonReleOn = findViewById(R.id.btnReleOn);
        buttonReleOff = findViewById(R.id.btnReleOff);

        buttonReleOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttHandler.publish(mqttHandler.TOPIC_RELAY_MUTE, "FALSE");
            }
        });

        buttonReleOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttHandler.publish(mqttHandler.TOPIC_RELAY_MUTE, "TRUE");
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
        unregisterReceiver(actuatorEventReceiver);
        super.onDestroy();
    }

    private void connect() {
        subscribeToTopic(MqttHandler.TOPIC_RELAY_MUTE);
        subscribeToTopic(MqttHandler.TOPIC_BUZZER_MUTE);
        subscribeToTopic(MqttHandler.TOPIC_ACTUATOR_BUZZER_STATE);
        subscribeToTopic(MqttHandler.TOPIC_ACTUATOR_RELAY_STATE);
        subscribeToTopic(MqttHandler.TOPIC_SMARTPHONES);
    }

    private void subscribeToTopic(String topic) {
        mqttHandler.subscribe(topic);
    }

    private void configureBroadcastReceiver() {
        filterReceive = new IntentFilter(MqttHandler.ACTION_EVENTS_ACTUATOR_STATUS);
        filterReceive.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(actuatorEventReceiver, filterReceive);
    }

    private class ActuatorEventReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            for (String actuatorName : intent.getExtras().keySet()) {
                String value = intent.getStringExtra(actuatorName);
                switch (actuatorName) {
                    case "BUZZER":
                        changeStateBuzzer(value);
                        break;
                    case "RELAY":
                        changeStateRelay(value);
                        break;
                }
            }
        }
    }

    private void changeStateBuzzer(String value) {
        boolean buzzerON = value.equals(ON);
        cbBuzzerStatus.setChecked(buzzerON);
    }

    private void changeStateRelay(String value) {
        boolean relayON = value.equals(ON);
        cbRelayStatus.setChecked(relayON);
    }
}
