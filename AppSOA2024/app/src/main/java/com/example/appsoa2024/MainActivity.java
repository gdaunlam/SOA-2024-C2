package com.example.appsoa2024;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;
import android.content.IntentFilter;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private MqttHandler mqttHandler;
    public IntentFilter filterReceive;
    public IntentFilter filterConncetionLost;
    private ReceptorOperacion receiver =new ReceptorOperacion();
    private ConnectionLost connectionLost =new ConnectionLost();
    private TextView txtJson;
    private TextView txtTemp;
    private TextView txtUltimaActualizacion;
    private TextView txtTemperatura;
    private TextView txtHumedad;
    private TextView txtCO2;
    private TextView txtEstadoPuerta;

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
                Intent intent = new Intent(MainActivity.this, Actuadores.class);
                startActivity(intent);
            }
        });

        //Inicializo resto de los textviews
        txtUltimaActualizacion = findViewById(R.id.tvUltimaActualizacion);
        txtTemperatura = findViewById(R.id.tvTemperaturaValor);
        txtCO2 = findViewById(R.id.tvCO2Valor);
        txtHumedad = findViewById(R.id.tvHumedadValor);
        txtEstadoPuerta = findViewById(R.id.tvPuertaValor);

        //Crear instancia MQTT
        mqttHandler = new MqttHandler(getApplicationContext());
        connect();
        configurarBroadcastReciever();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        mqttHandler.disconnect();
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    //Funciones para la comunicacion via MQTT
    private void connect() {
        mqttHandler.connect();
        try {
            Thread.sleep(1000);
            //Suscripcion a los topicos de la app
            subscribeToTopic(MqttHandler.TOPIC_BUZZER_MUTE);
            subscribeToTopic(MqttHandler.TOPIC_RELAY_MUTE);
            subscribeToTopic(MqttHandler.TOPIC_SENSORS_EVENTS);
            subscribeToTopic(MqttHandler.TOPIC_SENSORS_VALUES);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //Metodo que crea y configurar un broadcast receiver para comunicar el servicio que recibe los mensaje del servidor
    //con la activity principal
    private void configurarBroadcastReciever() {
        //se asocia(registra) la  accion RESPUESTA_OPERACION, para que cuando el Servicio de recepcion la ejecute
        //se invoque automaticamente el OnRecive del objeto receiver
        filterReceive = new IntentFilter(MqttHandler.ACTION_DATA_RECEIVE);
        filterConncetionLost = new IntentFilter(MqttHandler.ACTION_CONNECTION_LOST);

        filterReceive.addCategory(Intent.CATEGORY_DEFAULT);
        filterConncetionLost.addCategory(Intent.CATEGORY_DEFAULT);

        registerReceiver(receiver, filterReceive);
        registerReceiver(connectionLost,filterConncetionLost);
    }

    private void publishMessage(String topic, String message){
        Toast.makeText(this, "Publishing message: " + message, Toast.LENGTH_SHORT).show();
        mqttHandler.publish(topic,message);
    }
    private void subscribeToTopic(String topic){
        Toast.makeText(this, "Subscribing to topic "+ topic, Toast.LENGTH_SHORT).show();
        mqttHandler.subscribe(topic);
    }

    //Clases
    public class ConnectionLost extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(),"Conexion Perdida",Toast.LENGTH_SHORT).show();
            connect();
        }
    }

    public class ReceptorOperacion extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            //Se obtiene los valores que envio el servicio atraves de un intent
            //Se actualiza fecha de actualizacion
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String currentDateTime = sdf.format(new Date());
            txtUltimaActualizacion.setText("Ultima actualizacion: " + currentDateTime);

            //Ahora se pregunta el contexto

            String msgJson = intent.getStringExtra("msgJson");
            txtJson.setText(msgJson);

            try {
                JSONObject jsonObject = new JSONObject(msgJson);
                String value = jsonObject.getString("value");
                txtTemp.setText(value + "°");
                System.out.println("JSON value is: " + value);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

    }


}