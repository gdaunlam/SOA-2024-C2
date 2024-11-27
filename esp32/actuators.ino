#include <actuators_consts.h>

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
  if (servoPos > CERO_DEG && servoOn) servoPos--;
  if (servoPos < ONE_HUNDRED_EIGHTY_DEG && !servoOn) servoPos++;
}

bool buzzerOn = false;
void turnOnBuzzer() {
  buzzerOn = true;
}
void turnOffBuzzer() {
  buzzerOn = false;
}
bool isBuzzerOn() {
  return !getMuteBuzzer() && buzzerOn;
}

bool relayOn = false;
void turnOnRelay() {
  relayOn = true;
}
void turnOffRelay() {
  relayOn = false;
}
bool isRelayOn() {
  return !getMuteRelay() && relayOn;
}

void setColorLed(int red, int green, int blue) {
  analogWrite(PIN_LED_RED, red);
  analogWrite(PIN_LED_GREEN, green);
  analogWrite(PIN_LED_BLUE, blue);
}
void setColorLedRed() {
  setColorLed(RED_RED_COLOR_VALUE, RED_GREEN_COLOR_VALUE, RED_BLUE_COLOR_VALUE);
}
void setColorLedOrange() {
  setColorLed(ORANGE_RED_COLOR_VALUE, ORANGE_GREEN_COLOR_VALUE, ORANGE_BLUE_COLOR_VALUE);
}
void setColorLedYellow() {
  setColorLed(YELLOW_RED_COLOR_VALUE, YELLOW_GREEN_COLOR_VALUE, YELLOW_BLUE_COLOR_VALUE);
}
void setColorLedGreen() {
  setColorLed(GREEN_RED_COLOR_VALUE, GREEN_GREEN_COLOR_VALUE, GREEN_BLUE_COLOR_VALUE);
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
  digitalWrite(PIN_BUZZER, mustOnBuzzer ? LOW : HIGH);
  digitalWrite(PIN_RELAY, mustOnRelay ? LOW : HIGH);
  setServoPos();
  servo.write(servoPos);
}
