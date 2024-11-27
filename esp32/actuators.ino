#define PIN_BUZZER 26
#define PIN_LED_RED 23
#define PIN_LED_BLUE 18
#define PIN_LED_GREEN 22
#define PIN_RELAY 14
#define PIN_SERVO 5

#define CERO_DEG_VALUE 1000
#define ONE_HUNDRED_EIGHTY_DEG_VALUE 2000
#define STANDARD_PERIOD_HERTZ 50

#define ONE_HUNDRED_EIGHTY_DEG 180
#define CERO_DEG 0

Servo servo;
bool servoOn = false;
int servoPos = 0;
void turnServoOn() {
  servoOn = true;
  servo.writeMicroseconds(ONE_HUNDRED_EIGHTY_DEG_VALUE);
}
void turnServoOff() {
  servoOn = false;
  servo.writeMicroseconds(CERO_DEG_VALUE);
}
void setServoPos() {
  if(servoPos > CERO_DEG && servoOn) servoPos --;
  if(servoPos < ONE_HUNDRED_EIGHTY_DEG && !servoOn) servoPos ++;
}

bool buzzerOn = false;
void turnOnBuzzer() {
  buzzerOn = true;
}
void turnOffBuzzer() {
  buzzerOn = false;
}
bool isBuzzerOn(){
  return !getMuteBuzzer() && buzzerOn;
}

bool relayOn = false;
void turnOnRelay(){
  relayOn = true;
}
void turnOffRelay(){
  relayOn = false;
}
bool isRelayOn(){
  return !getMuteRelay() && relayOn;
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
  
  servo.setPeriodHertz(STANDARD_PERIOD_HERTZ);
	servo.attach(PIN_SERVO, CERO_DEG_VALUE, ONE_HUNDRED_EIGHTY_DEG_VALUE);
}

void loopActuators() {
  bool mustOnBuzzer = isBuzzerOn();
  bool mustOnRelay = isRelayOn();
  digitalWrite(PIN_BUZZER, mustOnBuzzer ? LOW: HIGH);
  digitalWrite(PIN_RELAY, mustOnRelay ? LOW: HIGH);
  setServoPos();
  servo.write(servoPos);
}
