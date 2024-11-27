#include <sensors_consts.h>
DHTesp dht;

TempAndHumidity readDHT() {
  return dht.getTempAndHumidity();
}

float readUltrasonicDistance() {
  pulseIn(PIN_ECHO_ULTRASONIDO, LOW);
  digitalWrite(PIN_TRIGGER_ULTRASONIDO, LOW);
  digitalWrite(PIN_TRIGGER_ULTRASONIDO, HIGH);
  digitalWrite(PIN_TRIGGER_ULTRASONIDO, LOW);
  float distance = pulseIn(PIN_ECHO_ULTRASONIDO, HIGH);
  return distance * SPEED_SOUND_CONVERTION;
}

float readC02Data() {
  int auxPpmValue = analogRead(MQ135_PIN);
  return float(auxPpmValue);
}

void initSensors() {
  pinMode(PIN_TRIGGER_ULTRASONIDO, OUTPUT);
  pinMode(PIN_ECHO_ULTRASONIDO, INPUT);
  pinMode(MQ135_PIN, INPUT);
  dht.setup(PIN_DHT, DHTesp::DHT11);
}