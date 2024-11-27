#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>
#include <connections_consts.h>

WiFiClientSecure wifiClient;
PubSubClient mqttClient(wifiClient);


bool MUTE_BUZZER = false;
bool MUTE_RELAY = false;
void mqttCallback(char* topic, byte* payload, unsigned int length) {
  String message;
  for (unsigned int i = 0; i < length; i++) {
    message += (char)payload[i];
  }
  if (strcmp(topic, MQTT_TOPIC_MUTE_BUZZER) == 0) {
    MUTE_BUZZER = (strcmp(message.c_str(), "TRUE") == 0);
  }
  if (strcmp(topic, MQTT_TOPIC_MUTE_RELAY) == 0) {
    MUTE_RELAY = (strcmp(message.c_str(), "TRUE") == 0);
  }
}
bool getMuteBuzzer() {
  return MUTE_BUZZER;
}
bool getMuteRelay() {
  return MUTE_RELAY;
}

void sendValuesMqtt(String CO2_VALUE, String DIST_VALUE, String HUM_VALUE, String TEMP_VALUE, String nextState) {
  String message =
    String(CO2_KEY) + "=" + CO2_VALUE + "|" + String(DIST_KEY) + "=" + DIST_VALUE + "|" + String(HUM_KEY) + "=" + HUM_VALUE + "|" + String(TEMP_KEY) + "=" + TEMP_VALUE + "|" + String(STATE_KEY) + "=" + nextState;
  mqttClient.publish(MQTT_TOPIC_SEND_VALUES, message.c_str());
}
void sendEventsMqtt(String values, String sensor, String message) {
  String msg = sensor + "=" + values + "=" + message;
  mqttClient.publish(MQTT_TOPIC_SEND_EVENTS, msg.c_str());
}
void sendEventsActuatorsMqtt(String actuator, String value) {
  if (strcmp(actuator.c_str(), BUZZER_KEY) == 0) {
    mqttClient.publish(MQTT_TOPIC_SEND_BUZZER_STATUS, value.c_str());
  }
  if (strcmp(actuator.c_str(), RELAY_KEY) == 0) {
    mqttClient.publish(MQTT_TOPIC_SEND_RELAY_STATUS, value.c_str());
  }
}
bool checkWifiConnection() {
  if (WiFi.isConnected()) return true;
  bool success = WiFi.reconnect();
  if (!success) {
    return false;
  }
  return false;
}

void checkMqttConnection() {
  if (mqttClient.connected()) return;
  bool success = mqttClient.connect("esp32_sensors_esn332prls", MQTT_USER, MQTT_PASSWORD);
  if (!success) {
    return;
  }
  mqttClient.subscribe(MQTT_TOPIC_MUTE_BUZZER);
  mqttClient.subscribe(MQTT_TOPIC_MUTE_RELAY);
}

void initConnections() {
  WiFi.begin(WIFI_SSID, WIFI_PASS);
  wifiClient.setCACert(ca_cert);
  mqttClient.setServer(MQTT_SERVER, MQTT_PORT);
  mqttClient.setCallback(mqttCallback);
}

void loopConnections() {
  bool successWifi = checkWifiConnection();
  if (successWifi) checkMqttConnection();
  mqttClient.loop();
}