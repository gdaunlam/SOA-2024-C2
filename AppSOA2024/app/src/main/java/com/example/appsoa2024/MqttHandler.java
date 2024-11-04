package com.example.appsoa2024;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

//Para agregar estas bibliotecas, se actualizaron los archivos build.gradle.kts y libs.versions.toml
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

public class MqttHandler implements MqttCallback {
    private static final String BROKER_URL = "ssl://broker.emqx.io:8883"; // port: 8883 URL: broker.emqx.io ; tcp://broker.emqx.io:8883
    private static final String CLIENT_ID = "mqttx_f9bfd3ww";
    private static final String USER="emqx";
    private static final String PASS="public";

    public static final String TOPIC_RELAY_MUTE = "/abscgwrrrt22/actuators/mute/relay";
    public static final String TOPIC_BUZZER_MUTE = "/abscgwrrrt22/actuators/mute/buzzer";
    public static final String TOPIC_SENSORS_EVENTS = "/abscgwrrrt22/sensors/events";
    public static final String TOPIC_SENSORS_VALUES = "/abscgwrrrt22/sensors/values";
    public static final String ACTION_DATA_RECEIVE ="com.example.intentservice.intent.action.DATA_RECEIVE";
    public static final String ACTION_CONNECTION_LOST ="com.example.intentservice.intent.action.CONNECTION_LOST";
    private MqttClient client;
    private Context mContext;

    public MqttHandler(Context mContext){
        this.mContext = mContext;
    }

    public void connect() {
        try {
            //Se configura las opciones de conexion
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(USER);
            options.setPassword(PASS.toCharArray());
            // Persistencia en memoria
            MemoryPersistence persistence = new MemoryPersistence();
            // Inicializar el cliente
            client = new MqttClient(BROKER_URL, CLIENT_ID, persistence);
            client.connect(options);

            client.setCallback(this);
            //client.subscribe("#");
        } catch (MqttException e) {
            Log.d("Aplicacion",e.getMessage()+ "  "+e.getCause());
        }
    }

    public void disconnect() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(2);
            client.publish(topic, mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic) {
        try {
            client.subscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d("MAIN ACTIVITY","conexion perdida"+ cause.getMessage().toString());

        Intent i = new Intent(ACTION_CONNECTION_LOST);
        mContext.sendBroadcast(i);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        String msgJson=message.toString();

        JSONObject json = new JSONObject(message.toString());
        Float valorPote = Float.parseFloat(json.getString("value"));

        //Se envian los valores sensados por el potenciometro, al bradcast reciever de la activity principal
        Intent i = new Intent(ACTION_DATA_RECEIVE);
        i.putExtra("msgJson", msgJson);

        mContext.sendBroadcast(i);



    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }


}
