#define PIN_TRIGGER_ULTRASONIDO 13
#define PIN_ECHO_ULTRASONIDO 12
#define PIN_DHT 32
#define POT_PIN 25

DHTesp dht;

TempAndHumidity readDHT() {
  return dht.getTempAndHumidity();
}

long readUltrasonicDistance()
{
  pinMode(PIN_TRIGGER_ULTRASONIDO, OUTPUT);
  digitalWrite(PIN_TRIGGER_ULTRASONIDO, LOW);
  //delayMicroseconds(2);
  digitalWrite(PIN_TRIGGER_ULTRASONIDO, HIGH);
  //delayMicroseconds(10);
  digitalWrite(PIN_TRIGGER_ULTRASONIDO, LOW);
  pinMode(PIN_ECHO_ULTRASONIDO, INPUT);
  long distance = pulseIn(PIN_ECHO_ULTRASONIDO, HIGH);
  return distance * 0.017;
}

int readPotentiometerData() {
  int auxPpmValue = analogRead(POT_PIN);
  return map(auxPpmValue, 0, 4095, 0, 100);
}

void initSensors() {
  pinMode(PIN_TRIGGER_ULTRASONIDO, OUTPUT);
  pinMode(PIN_ECHO_ULTRASONIDO, INPUT);
  pinMode(POT_PIN, INPUT);
  dht.setup(PIN_DHT, DHTesp::DHT22);
}