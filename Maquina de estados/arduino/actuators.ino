#define PIN_BUZZER 26
#define PIN_LED_RED 23
#define PIN_LED_BLUE 18
#define PIN_LED_GREEN 22
#define PIN_RELAY 14
#define PIN_SERVO 5
  
Servo servo;
bool servoOn = false;
int servoPos = 0;
void turnServoOn() {
  servoOn = true;
  servo.writeMicroseconds(2000);
}
void turnServoOff() {
  servoOn = false;
  servo.writeMicroseconds(1000);  // 500us es 0 grados
}
void setServoPos() {
  if(servoPos > 0 && servoOn) servoPos --;
  if(servoPos < 180 && !servoOn) servoPos ++;
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
  
  servo.setPeriodHertz(50);           // standard 50 hz servo
	servo.attach(PIN_SERVO, 1000, 2000); // attaches the servo on pin 18 to the servo object
}

void loopActuators() {
  bool mustOnBuzzer = isBuzzerOn();
  bool mustOnRelay = isRelayOn();
  digitalWrite(PIN_BUZZER, mustOnBuzzer ? LOW: HIGH);
  digitalWrite(PIN_RELAY, mustOnRelay ? LOW: HIGH);
  //setServoPos();
  //servo.write(servoPos);
}
