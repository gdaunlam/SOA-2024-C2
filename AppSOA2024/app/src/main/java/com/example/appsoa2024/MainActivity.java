package com.example.appsoa2024;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
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
    private CheckBox mqttCheckBox;
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

        mqttCheckBox = (CheckBox)findViewById(R.id.mqttCheckBox);

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
        mqttCheckBox.setChecked(true);
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
            mqttCheckBox.setChecked(false);
            connect();
        }
    }

    private class ReceptorEventos extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            for (String key : intent.getExtras().keySet()) {
                System.out.println(key);
                String value = intent.getStringExtra(key);
                assert value != null;
                value = value.substring(value.indexOf("=") + 1);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String currentDateTime = sdf.format(new Date());
                addMessageToRecyclerView(value + " " + currentDateTime);
                actualizarFechaYHora();
                if (key.equals("STATE")) {
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

                    // Build the notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                            "states_notification")
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle("App Notification")
                            .setContentText(value)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(value))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    // Send the notification
                    NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0, builder.build());
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