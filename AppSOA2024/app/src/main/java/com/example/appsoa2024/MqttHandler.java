package com.example.appsoa2024;

import android.content.Context;
import android.content.Intent;

//Para agregar estas bibliotecas, se actualizaron los archivos build.gradle.kts y libs.versions.toml
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

public class MqttHandler implements MqttCallback
{
    private static final String BROKER_URL = "ssl://broker.emqx.io:8883"; // port: 8883 URL: broker.emqx.io ;
    private static final String CLIENT_ID = UUID.randomUUID().toString().replaceAll("-", "");
    private static final String USER="emqx";
    private static final String PASS="public";
    public static final String TOPIC_RELAY_MUTE = "/abscgwrrrt22/actuators/mute/relay";
    public static final String TOPIC_BUZZER_MUTE = "/abscgwrrrt22/actuators/mute/buzzer";
    public static final String TOPIC_SENSORS_EVENTS = "/abscgwrrrt22/sensors/events";
    public static final String TOPIC_SENSORS_VALUES = "/abscgwrrrt22/sensors/values";
    public static final String TOPIC_ACTUATOR_RELAY_STATE = "/abscgwrrrt22/actuators/status/relay";
    public static final String TOPIC_ACTUATOR_BUZZER_STATE = "/abscgwrrrt22/actuators/status/buzzer";
    public static final String TOPIC_SMARTPHONES = "/abscgwrrrt22/smartphones";
    public static final String ACTION_EVENTS_RECEIVE ="com.example.intentservice.intent.action.EVENTS_RECEIVE";
    public static final String ACTION_CONNECTION_LOST ="com.example.intentservice.intent.action.CONNECTION_LOST";
    public static final String ACTION_VALUES_RECEIVE = "com.example.intentservice.intent.action.VALUES_RECEIVE";
    public static final String ACTION_EVENTS_ACTUATOR_STATUS = "com.example.intentservice.intent.action.ACTUATOR.STATUS";
    public static final String ACTION_EVENTS_SMARTPHONES = "com.example.intentservice.intent.action.SMARTPHONES";
    private static final Hashtable<String, String> embedStates = new Hashtable<String, String>()
    {{
        put("1", "LOW");
        put("2", "MEDIUM");
        put("3", "HIGH");
        put("4", "CRITICAL");
    }};
    private static MqttClient client;
    private Context mContext;
    public MqttHandler(Context mContext)
    {
        this.mContext = mContext;
    }

    public void connect() throws MqttException
    {
        boolean clientExists = client != null;
        if(clientExists && client.isConnected())
        {
            return;
        }
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
    }

    public void disconnect()
    {
        try
        {
            client.disconnect();
        } catch (MqttException e)
        {
            e.printStackTrace();
        }
    }

    public void publish(String topic, String message)
    {
        try
        {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            int qualityOfService = 2;
            mqttMessage.setQos(qualityOfService);
            client.publish(topic, mqttMessage);
        } catch (MqttException e)
        {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic)
    {
        try
        {
            client.subscribe(topic);
        } catch (MqttException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause)
    {
        Intent i = new Intent(ACTION_CONNECTION_LOST);
        mContext.sendBroadcast(i);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception
    {
        String messageMqtt=message.toString();
        //Se declara el intent que se va a enviar en el broadcast. Se inicializa segun el topico
        Intent i = null;

        switch(topic)
        {
            case TOPIC_SENSORS_VALUES:
                //Guardar los valores en las variables de la clase MQTTHandler
                i = new Intent(ACTION_VALUES_RECEIVE);
                List<String> listValues = new ArrayList<String>(Arrays.asList(messageMqtt.split("\\|")));
                for(String value: listValues){
                    int beginIndex = 0;
                    String key = value.substring(beginIndex,value.indexOf("="));
                    value = value.substring(value.indexOf("=") + 1);
                    if(key.equals("STATE"))
                    {
                        value = embedStates.get(value); //Reemplazo el numero por el string
                    }

                    i.putExtra(key,value);  //En el intent, se crea un item por cada valor de sensor recibido
                }
                break;
            case TOPIC_SENSORS_EVENTS:
                i = new Intent(ACTION_EVENTS_RECEIVE);
                int beginIndex = 0;
                String key = messageMqtt.substring(beginIndex,messageMqtt.indexOf("="));
                String eventMessage = messageMqtt.substring(messageMqtt.indexOf("=") + 1);
                i.putExtra(key, eventMessage);
                break;
            case TOPIC_ACTUATOR_BUZZER_STATE:
                i = new Intent(ACTION_EVENTS_ACTUATOR_STATUS);
                i.putExtra("BUZZER",messageMqtt);
                break;
            case TOPIC_ACTUATOR_RELAY_STATE:
                i = new Intent(ACTION_EVENTS_ACTUATOR_STATUS);
                i.putExtra("RELAY",messageMqtt);
                break;
            case TOPIC_SMARTPHONES:
                if(messageMqtt.equals("ALARMA"))
                {
                    i = new Intent(ACTION_EVENTS_SMARTPHONES);
                    i.putExtra("type","ALARMA");
                }
            default:
                System.out.println("ERROR: Unknown topic.");
                break;

        }

        if(i != null)
        {
            mContext.sendBroadcast(i);  //Envio el intent al broadcast
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token)
    {

    }
}
