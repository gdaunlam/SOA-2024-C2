#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>

const char* ca_cert= \
"-----BEGIN CERTIFICATE-----\n" \
"MIIDrzCCApegAwIBAgIQCDvgVpBCRrGhdWrJWZHHSjANBgkqhkiG9w0BAQUFADBh\n" \
"MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3\n" \
"d3cuZGlnaWNlcnQuY29tMSAwHgYDVQQDExdEaWdpQ2VydCBHbG9iYWwgUm9vdCBD\n" \
"QTAeFw0wNjExMTAwMDAwMDBaFw0zMTExMTAwMDAwMDBaMGExCzAJBgNVBAYTAlVT\n" \
"MRUwEwYDVQQKEwxEaWdpQ2VydCBJbmMxGTAXBgNVBAsTEHd3dy5kaWdpY2VydC5j\n" \
"b20xIDAeBgNVBAMTF0RpZ2lDZXJ0IEdsb2JhbCBSb290IENBMIIBIjANBgkqhkiG\n" \
"9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4jvhEXLeqKTTo1eqUKKPC3eQyaKl7hLOllsB\n" \
"CSDMAZOnTjC3U/dDxGkAV53ijSLdhwZAAIEJzs4bg7/fzTtxRuLWZscFs3YnFo97\n" \
"nh6Vfe63SKMI2tavegw5BmV/Sl0fvBf4q77uKNd0f3p4mVmFaG5cIzJLv07A6Fpt\n" \
"43C/dxC//AH2hdmoRBBYMql1GNXRor5H4idq9Joz+EkIYIvUX7Q6hL+hqkpMfT7P\n" \
"T19sdl6gSzeRntwi5m3OFBqOasv+zbMUZBfHWymeMr/y7vrTC0LUq7dBMtoM1O/4\n" \
"gdW7jVg/tRvoSSiicNoxBN33shbyTApOB6jtSj1etX+jkMOvJwIDAQABo2MwYTAO\n" \
"BgNVHQ8BAf8EBAMCAYYwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUA95QNVbR\n" \
"TLtm8KPiGxvDl7I90VUwHwYDVR0jBBgwFoAUA95QNVbRTLtm8KPiGxvDl7I90VUw\n" \
"DQYJKoZIhvcNAQEFBQADggEBAMucN6pIExIK+t1EnE9SsPTfrgT1eXkIoyQY/Esr\n" \
"hMAtudXH/vTBH1jLuG2cenTnmCmrEbXjcKChzUyImZOMkXDiqw8cvpOp/2PV5Adg\n" \
"06O/nVsJ8dWO41P0jmP6P6fbtGbfYmbW0W5BjfIttep3Sp+dWOIrWcBAI+0tKIJF\n" \
"PnlUkiaY4IBIqDfv8NZ5YBberOgOzW6sRBc4L0na4UU+Krk2U886UAb3LujEV0ls\n" \
"YSEY1QSteDwsOoBrp+uvFRTp2InBuThs4pFsiv9kuXclVzDAGySj4dzp30d8tbQk\n" \
"CAUw7C29C79Fv1C5qfPrmAESrciIxpg0X40KPMbp1ZWVbd4=" \
"-----END CERTIFICATE-----\n";
#define MQTT_USER "emqx"
#define MQTT_PASSWORD "public"
#define MQTT_PORT 8883
#define MQTT_SERVER "broker.emqx.io" 
#define MQTT_TOPIC_SEND_EVENTS "/abscgwrrrt22/sensors/events"
#define MQTT_TOPIC_SEND_VALUES "/abscgwrrrt22/sensors/values"
#define MQTT_TOPIC_MUTE_BUZZER "/abscgwrrrt22/actuators/mute/buzzer"
#define MQTT_TOPIC_MUTE_RELAY "/abscgwrrrt22/actuators/mute/relay"
#define MQTT_TOPIC_SEND_BUZZER_STATUS = "/abscgwrrrt22/actuators/status/buzzer"
#define MQTT_TOPIC_SEND_RELAY_STATUS = "/abscgwrrrt22/actuators/status/relay"

// WIFI
#define WIFI_SSID "moto g52_5630"
#define WIFI_PASS "12345678"
WiFiClientSecure wifiClient;
PubSubClient mqttClient(wifiClient);


bool MUTE_BUZZER = false;
bool MUTE_RELAY = false;
void mqttCallback(char* topic, byte* payload, unsigned int length)
{
  String message;
  for (unsigned int i = 0; i < length; i++) 
  {
    message += (char)payload[i];
  }
  if (strcmp(topic, MQTT_TOPIC_MUTE_BUZZER) == 0) 
  {
    MUTE_BUZZER = (strcmp(message.c_str(), "TRUE") == 0);
    Serial.println("BUZZER STATUS TO: " + String(MUTE_BUZZER));
  }
  if (strcmp(topic, MQTT_TOPIC_MUTE_RELAY) == 0) 
  {
    MUTE_RELAY = (strcmp(message.c_str(), "TRUE") == 0);
    Serial.println("RELAY STATUS TO: " + String(MUTE_RELAY));
  }
}
bool getMuteBuzzer(){
  return MUTE_BUZZER;
}
bool getMuteRelay(){
  return MUTE_RELAY;
}

void sendValuesMqtt(String CO2_VALUE, String DIST_VALUE, String HUM_VALUE, String TEMP_VALUE, String nextState)
{
  String message =
    String(CO2_KEY) + "=" + CO2_VALUE + "|" +
    String(DIST_KEY) + "=" + DIST_VALUE + "|" +
    String(HUM_KEY) + "=" + HUM_VALUE + "|" +
    String(TEMP_KEY) + "=" + TEMP_VALUE + "|" +
    String(STATE_KEY) + "=" +  nextState;
  mqttClient.publish(MQTT_TOPIC_SEND_VALUES, message.c_str());
}
void sendEventsMqtt(String values, String sensor)
{
  String message = sensor + "=" + values;
  mqttClient.publish(MQTT_TOPIC_SEND_EVENTS, message.c_str());
}
void sendEventsActuatorsMqtt(String actuator, String value){
  if(strcmp(actuator,BUZZER_KEY) == 0){//Send buzzer status (If
    mqttClient.publish(MQTT_TOPIC_SEND_BUZZER_STATUS, value.c_str());
  }

  if(strcmp(actuator,RELAY_KEY) == 0 ) {
    mqttClient.publish(MQTT_TOPIC_SEND_RELAY_STATUS, value.c_str());
  }
  
}
bool checkWifiConnection() 
{
  if (WiFi.isConnected()) return true; 
  Serial.println("Conectando wifi...");
  bool success = WiFi.reconnect();
  if (!success)
  {
    Serial.println("Fallo en la conexión al servidor wifi: " + String(WiFi.status()));
    return false;
  }
  Serial.println("WIFI OK");
  return false;
}

void checkMqttConnection() 
{
  if (mqttClient.connected()) return;
  Serial.println("Conectando al servidor MQTT...");
  bool success = mqttClient.connect("esp32_sensors_esn332prls",MQTT_USER,MQTT_PASSWORD);
  if (!success)
  {
    Serial.println("Fallo en la conexión al servidor MQTT: " + String(mqttClient.state()));
    return;
  }
  mqttClient.subscribe(MQTT_TOPIC_SEND_EVENTS);
  mqttClient.subscribe(MQTT_TOPIC_SEND_VALUES);
  mqttClient.subscribe(MQTT_TOPIC_MUTE_BUZZER);
  mqttClient.subscribe(MQTT_TOPIC_MUTE_RELAY);
  Serial.println("MQTT OK");
}

void initConnections()
{
  WiFi.begin(WIFI_SSID, WIFI_PASS);
  wifiClient.setCACert(ca_cert);
  mqttClient.setServer(MQTT_SERVER, MQTT_PORT);
  mqttClient.setCallback(mqttCallback);
}

void loopConnections()
{
  bool successWifi = checkWifiConnection();
  if (successWifi) checkMqttConnection();
  mqttClient.loop();
}