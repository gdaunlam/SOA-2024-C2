#define PIN_BUZZER 26
#define PIN_LED_RED 23
#define PIN_LED_BLUE 18
#define PIN_LED_GREEN 22
#define PIN_RELAY 14

bool buzzerOn = false;
void turnOnBuzzer() {
  buzzerOn = true;
}
void turnOffBuzzer() {
  buzzerOn = false;
}

bool relayOn = false;
void turnOnRelay(){
  relayOn = true;
}
void turnOffRelay(){
  relayOn = false;
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

void loopActuators() {
  bool mustOnBuzzer = !getMuteBuzzer() && buzzerOn;
  bool mustOnRelay = !getMuteRelay() && relayOn;
  digitalWrite(PIN_BUZZER, mustOnBuzzer ? LOW: HIGH);
  digitalWrite(PIN_RELAY, mustOnRelay ? LOW: HIGH);
}

bool isBuzzerOn(){
  return digitalRead(PIN_BUZZER) == 1;
}

bool isRelayOn(){
  return digitalRead(PIN_RELAY) == 1;
}