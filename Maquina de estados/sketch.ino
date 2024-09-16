#include "DHTesp.h"

const int TEMPERATURE_HIGH = 35;
const int CO2_HIGH = 5;
const int MIN_HUMIDITY_HIGH = 20;
const int MAX_HUMIDITY_HIGH = 70;

const int TEMPERATURE_MID = 27;
const int CO2_MID = 2;
const int MIN_HUMIDITY_MID = 30;
const int MAX_HUMIDITY_MID = 60;

const int TEMPERATURE_LOW = 24;
const int CO2_LOW = 1;
const int MIN_HUMIDITY_LOW = 35;
const int MAX_HUMIDITY_LOW = 50;
const int DISTANCE_LOW = 5;

typedef int STATE;
const STATE INIT_STATE = 0;
const STATE LOW_STATE = 1;
const STATE MEDIUM_STATE = 2;
const STATE HIGH_STATE = 3;
const STATE CRITICAL_STATE = 4;
const int MAX_STATES = 5;

typedef int EVENT;
const EVENT INIT_CONDITION_EVENT = 0;
const EVENT LOW_CONDITION_EVENT = 1;
const EVENT MID_CONDITION_EVENT = 2;
const EVENT HIGH_CONDITION_EVENT = 3;
const EVENT CRITICAL_CONDITION_EVENT = 4;
const int MAX_EVENTS = 5;

typedef int SENSOR;
const SENSOR CO2_SENSOR = 0;
const SENSOR DISTANCE_SENSOR = 1;
const SENSOR TEMP_HUMIDITY_SENSOR = 2;
const int SENSORS_COUNT = 3;

STATE currentState = INIT_STATE;
STATE nextState = INIT_STATE;
SENSOR currentSensor = CO2_SENSOR;
long DISTANCE_VALUE = 0;
float TEMPERATURE_VALUE = 0;
float HUMIDITY_VALUE = 0;
long CO2_VALUE = 0;

typedef void (*transition)();
void iToL() {
  setColorLedGreen();
  notify();
  return;
}
void lToM() {
  setColorLedYellow();
  notify();
  return;
}
void mToL() {
  setColorLedGreen();
  notify();
  return;
}
void mToH() {
  turnOnRelay();
  setColorLedOrange();
  notify();
  return;
}
void hToM() {
  turnOffRelay();
  setColorLedYellow();
  notify();
  return;
}
void hToC() {
  turnOnBuzzer();
  setColorLedRed();
  notify();
  return;
}
void cToH() {
  turnOffBuzzer();
  setColorLedOrange();
  notify();
  return;
}
void none() {
  return;
}

STATE state_table_next_state[MAX_STATES][MAX_EVENTS] =
{
  {LOW_STATE, LOW_STATE, LOW_STATE, LOW_STATE, LOW_STATE }, //INIT_STATE
  {LOW_STATE, LOW_STATE, MEDIUM_STATE, MEDIUM_STATE, MEDIUM_STATE }, //LOW_STATE
  {MEDIUM_STATE, LOW_STATE, MEDIUM_STATE, HIGH_STATE, HIGH_STATE}, //MEDIUM_STATE
  {HIGH_STATE, MEDIUM_STATE, MEDIUM_STATE, HIGH_STATE, CRITICAL_STATE}, //HIGH_STATE
  {CRITICAL_STATE, HIGH_STATE, HIGH_STATE, HIGH_STATE, CRITICAL_STATE }, //CRITICAL_STATE
  //INIT_CONDITION_EVENT, LOW_CONDITION_EVENT, MID_CONDITION_EVENT, HIGH_CONDITION_EVENT, CRITICAL_CONDITION_EVENT
};
transition state_table_actions[MAX_STATES][MAX_EVENTS] =
{
  {iToL, iToL,  iToL,  iToL,  iToL }, //INIT_STATE
  {none, none,  lToM,  lToM,  lToM }, //LOW_STATE
  {none, mToL,  none, mToH, mToH},  //MEDIUM_STATE
  {none, hToM, hToM, none,  hToC}, //HIGH_STATE
  {none, cToH, cToH, cToH, none },  //CRITICAL_STATE
  //INIT_CONDITION_EVENT, LOW_CONDITION_EVENT, MID_CONDITION_EVENT, HIGH_CONDITION_EVENT, CRITICAL_CONDITION_EVENT
};

void readSensors() {
  currentSensor++;
  currentSensor = currentSensor % SENSORS_COUNT;
  TempAndHumidity dataDTH ;

  switch (currentSensor) {
    case CO2_SENSOR:
      CO2_VALUE = readPotentiometerData();
      break;
    case DISTANCE_SENSOR:
      DISTANCE_VALUE = readUltrasonicDistance();
      break;
    case TEMP_HUMIDITY_SENSOR:
      dataDTH = readDHT();
      HUMIDITY_VALUE = dataDTH.humidity;
      TEMPERATURE_VALUE = dataDTH.temperature;
      break;
    default:
      currentSensor = CO2_SENSOR;
      break;
  }
}

EVENT getEvent() {
  if (
    TEMPERATURE_VALUE > TEMPERATURE_HIGH ||
    CO2_VALUE > CO2_HIGH ||
    HUMIDITY_VALUE < MIN_HUMIDITY_HIGH ||
    HUMIDITY_VALUE > MAX_HUMIDITY_HIGH
  ) return CRITICAL_CONDITION_EVENT;

  if (
    TEMPERATURE_VALUE > TEMPERATURE_MID ||
    CO2_VALUE > CO2_MID ||
    HUMIDITY_VALUE < MIN_HUMIDITY_MID ||
    HUMIDITY_VALUE > MAX_HUMIDITY_MID
  ) return HIGH_CONDITION_EVENT;

  if (
    TEMPERATURE_VALUE > TEMPERATURE_LOW ||
    CO2_VALUE > CO2_LOW ||
    HUMIDITY_VALUE < MIN_HUMIDITY_LOW ||
    HUMIDITY_VALUE > MAX_HUMIDITY_LOW ||
    DISTANCE_VALUE > DISTANCE_LOW
  ) return MID_CONDITION_EVENT;

  return LOW_CONDITION_EVENT;
}

const int MAX_MESSAGES = 5;
String messages[MAX_MESSAGES];
int messageCount;
void updateAlertWarningMessages() {
  messageCount = 0;
  if (TEMPERATURE_VALUE > TEMPERATURE_LOW) messages[messageCount++] = "Temp > " + String(TEMPERATURE_LOW);
  if (CO2_VALUE > CO2_LOW) messages[messageCount++] = "CO2 > " + String(CO2_LOW);
  if (HUMIDITY_VALUE < MIN_HUMIDITY_LOW) messages[messageCount++] = "Humidity < " + String(MIN_HUMIDITY_LOW);
  if (HUMIDITY_VALUE > MAX_HUMIDITY_LOW) messages[messageCount++] = "Humidity > " + String(MAX_HUMIDITY_LOW);
  if (DISTANCE_VALUE > DISTANCE_LOW) messages[messageCount++] = "Door Open";
}

String getStateName(int state) {
  switch (state) {
    case  INIT_STATE: return "inicial";
    case  LOW_STATE: return "low";
    case  MEDIUM_STATE: return "medium";
    case  HIGH_STATE: return "high";
    case  CRITICAL_STATE: return "critical";
  }
}

void notify() {
  Serial.println("ha ocurrido un cambio de estado del estado: " + getStateName(currentState) + " al estado: " + getStateName(nextState));

  updateAlertWarningMessages();
  if (messageCount > 0) {
    Serial.print("las mediciones afectadas fueron: ");
    for (int i = 0; i < messageCount; i++) {
      Serial.print(messages[i] + " ");
    }
    Serial.println();
  }
}

void setup() {
  Serial.begin(115200);
  initSensors();
  initActuators();
}

void loop() {
  readSensors();
  EVENT event = getEvent();
  nextState = state_table_next_state[currentState][event];
  transition action = state_table_actions[currentState][event];
  action();
  currentState = nextState;
}