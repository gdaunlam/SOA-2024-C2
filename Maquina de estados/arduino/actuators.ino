#define PIN_BUZZER 26
#define PIN_LED_RED 23
#define PIN_LED_BLUE 18
#define PIN_LED_GREEN 22
#define PIN_RELAY 14

void turnOnBuzzer() {
  int value = LOW;
  if(getMuteBuzzer()) value = HIGH;
  digitalWrite(PIN_BUZZER, value);
}
void turnOffBuzzer() {
  digitalWrite(PIN_BUZZER, HIGH);
}

void turnOnRelay(){
  int value = HIGH;
  if(getMuteRelay()) value = LOW;
  digitalWrite(PIN_RELAY, value);
}
void turnOffRelay(){
  digitalWrite(PIN_RELAY, LOW);
}

void setColorLed(int red, int green, int blue) {
  analogWrite(PIN_LED_RED, red);
  analogWrite(PIN_LED_GREEN, green);
  analogWrite(PIN_LED_BLUE, blue);
}

void setColorLedRed() {
  setColorLed(255,0,0);
}
void setColorLedOrange() {
  setColorLed(255,165,0);
}
void setColorLedYellow() {
  setColorLed(255,255,0);
}
void setColorLedGreen() {
  setColorLed(0,255,0);
}

void initActuators() {
  pinMode(PIN_BUZZER, OUTPUT);
  pinMode(PIN_LED_RED, OUTPUT);
  pinMode(PIN_LED_BLUE, OUTPUT);
  pinMode(PIN_LED_GREEN, OUTPUT);
  pinMode(PIN_RELAY, OUTPUT);
}
