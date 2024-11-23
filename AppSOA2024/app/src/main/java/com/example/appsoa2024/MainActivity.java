package com.example.appsoa2024;

import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;
import android.content.IntentFilter;

import org.eclipse.paho.client.mqttv3.MqttException;
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

    private RecyclerView rvMessages;
    private MessageAdapter messageAdapter;
    private List<String> messages = new ArrayList<>();


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
                Intent intent = new Intent(MainActivity.this, Actuadores.class);
                startActivity(intent);

            }
        });

        // Inicializar RecyclerView
        rvMessages = findViewById(R.id.rvMessages);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));

        // Configurar adaptador
        messageAdapter = new MessageAdapter(messages);
        rvMessages.setAdapter(messageAdapter);

        //Inicializo resto de los textviews
        txtUltimaActualizacion = (TextView)findViewById(R.id.tvUltimaActualizacion);
        txtTemperatura = (TextView)findViewById(R.id.tvTemperaturaValor);
        txtCO2 = (TextView)findViewById(R.id.tvCO2Valor);
        txtHumedad = (TextView)findViewById(R.id.tvHumedadValor);
        txtEstadoPuerta = (TextView)findViewById(R.id.tvPuertaValor);
        txtEstadoEmbebido = (TextView)findViewById(R.id.tvEstado);

        //Crear instancia MQTT
        mqttHandler = new MqttHandler(getApplicationContext());
        connect();
        configurarBroadcastReciever();
        actualizarFechaYHora();
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
        try {
            mqttHandler.connect();
            Toast.makeText(getApplicationContext(),"Conexion establecida",Toast.LENGTH_SHORT).show();
        } catch (MqttException e) {
            Log.d("Aplicacion",e.getMessage()+ "  "+e.getCause());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    connect();
                }
            }, 500);
        }
        subscribeToTopic(MqttHandler.TOPIC_SENSORS_EVENTS);
        subscribeToTopic(MqttHandler.TOPIC_SENSORS_VALUES);
    }

    //Metodo que crea y conrefigurar un broadcast receiver para comunicar el servicio que recibe los mensaje del servidor
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

    private class ReceptorEventos extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            for (String key : intent.getExtras().keySet()) {
                String value = intent.getStringExtra(key);
                value = value.substring(value.indexOf("=") + 1);
                if (value != null) {
                    addMessageToRecyclerView(value);
                    actualizarFechaYHora();
                }
            }
        }
    }

    private void addMessageToRecyclerView(String message) {
        messages.add(0, message);
        messageAdapter.notifyItemInserted(0);
        rvMessages.scrollToPosition(0);
    }

    private class ReceptorValoresSensores extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            //Leo todos los extras del intent en un for each
            runOnUiThread(() -> {
                for(String extraName: intent.getExtras().keySet()){
                    String sensorValue = intent.getStringExtra(extraName);
                    switch(extraName){
                        case "CO2":
                            txtCO2.setText(sensorValue + " ppm");
                            break;
                        case "DIST":
                            if(Float.parseFloat(sensorValue) > 5) {
                                txtEstadoPuerta.setText("ABIERTA");
                            } else {
                                txtEstadoPuerta.setText("CERRADA");
                            }
                            break;
                        case "HUM":
                            txtHumedad.setText(sensorValue + " %");
                            break;
                        case "TEMP":
                            txtTemperatura.setText(sensorValue + " °C");
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

    private void publishMessage(String topic, String message){
        //Toast.makeText(this, "Publishing message: " + message + "; Topico: " + topic, Toast.LENGTH_SHORT).show();
        mqttHandler.publish(topic,message);
    }
}