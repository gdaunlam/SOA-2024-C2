package com.example.appsoa2024;

import android.os.Bundle;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MqttHandler mqttHandler;
    public IntentFilter filterReceive;
    public IntentFilter filterConncetionLost;
    public IntentFilter filterSensorValues;
    private ReceptorEventos receiverEventos = new ReceptorEventos();
    private ConnectionLost connectionLost =new ConnectionLost();
    private ReceptorValoresSensores receiverSensores = new ReceptorValoresSensores();
    private TextView txtUltimaActualizacion;
    private TextView txtTemperatura;
    private TextView txtHumedad;
    private TextView txtCO2;
    private TextView txtEstadoPuerta;
    private TextView txtEstadoEmbebido;

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
        txtUltimaActualizacion = (TextView)findViewById(R.id.tvUltimaActualizacion);
        txtTemperatura = (TextView)findViewById(R.id.tvTemperaturaValor);
        txtCO2 = (TextView)findViewById(R.id.tvCO2Valor);
        txtHumedad = (TextView)findViewById(R.id.tvHumedadValor);
        txtEstadoPuerta = (TextView)findViewById(R.id.tvPuertaValor);
        txtEstadoEmbebido = (TextView)findViewById(R.id.tvEstado);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //Crear instancia MQTT
        mqttHandler = new MqttHandler(getApplicationContext());
        connect();
        configurarBroadcastReciever();
        actualizarFechaYHora();
    }

    @Override
    protected void onResume() {
        Log.d("Aplicacion","ASDASDASD");
        mqttHandler.connect();
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("Aplicacion","ASDASDASD");
        mqttHandler.disconnect();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mqttHandler.disconnect();
        unregisterReceiver(receiverEventos);
        unregisterReceiver(receiverSensores);
        unregisterReceiver(connectionLost);
    }

    @Override
    protected void onDestroy() {
        mqttHandler.disconnect();
        super.onDestroy();
        unregisterReceiver(receiverEventos);
        unregisterReceiver(receiverSensores);
        unregisterReceiver(connectionLost);
    }

    //Funciones para la comunicacion via MQTT
    private void connect() {
        mqttHandler.connect();
        try {
            Thread.sleep(1000);
            //Suscripcion a los topicos de la app
            //subscribeToTopic(MqttHandler.TOPIC_BUZZER_MUTE);
            //subscribeToTopic(MqttHandler.TOPIC_RELAY_MUTE);
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
        filterReceive = new IntentFilter(MqttHandler.ACTION_EVENTS_RECEIVE);
        filterConncetionLost = new IntentFilter(MqttHandler.ACTION_CONNECTION_LOST);
        filterSensorValues = new IntentFilter(MqttHandler.ACTION_VALUES_RECEIVE);

        filterReceive.addCategory(Intent.CATEGORY_DEFAULT);
        filterConncetionLost.addCategory(Intent.CATEGORY_DEFAULT);
        filterSensorValues.addCategory(Intent.CATEGORY_DEFAULT);

        registerReceiver(receiverEventos, filterReceive);
        registerReceiver(connectionLost,filterConncetionLost);
        registerReceiver(receiverSensores,filterSensorValues);
    }

    private void subscribeToTopic(String topic){
        Toast.makeText(this, "Subscribing to topic "+ topic, Toast.LENGTH_SHORT).show();
        mqttHandler.subscribe(topic);
    }

    private void actualizarFechaYHora(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String currentDateTime = sdf.format(new Date());
        txtUltimaActualizacion.setText("Ultima actualizacion: " + currentDateTime);
    }

    //Clases
    public class ConnectionLost extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(),"Conexion Perdida",Toast.LENGTH_SHORT).show();
            connect();
        }
    }

    public class ReceptorEventos extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            //Solamente me interesa reportar el cambio de estado.
            if(intent.hasExtra("STATE")){
                String sensorValue = intent.getStringExtra("STATE");
                Toast.makeText(getApplicationContext(),"Cambio de estado a " + sensorValue,Toast.LENGTH_LONG).show();
                actualizarFechaYHora();
            }

        }

    }

    public class ReceptorValoresSensores extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            boolean isOnUiThread = Looper.myLooper() == Looper.getMainLooper();

            //Leo todos los extras del intent en un for each
            runOnUiThread(() -> {
                for(String extraName: intent.getExtras().keySet()){
                    String sensorValue = intent.getStringExtra(extraName);
                    switch(extraName){
                        case "CO2":
                            txtCO2.setText(sensorValue);
                            break;
                        case "DIST":
                            if(sensorValue.equals("0")){
                                txtEstadoPuerta.setText("CERRADA");
                            } else {
                                txtEstadoPuerta.setText("ABIERTA");
                            }
                            break;
                        case "HUM":
                            txtHumedad.setText(sensorValue);
                            break;
                        case "TEMP":
                            txtTemperatura.setText(sensorValue);
                            break;
                        case "STATE":
                            txtEstadoEmbebido.setText(sensorValue);
                            break;
                        default:
                            System.out.println("error Extra del Intent desconocido: " + extraName);
                            break;
                    }
                }

                actualizarFechaYHora();
            });

        }

    }

}