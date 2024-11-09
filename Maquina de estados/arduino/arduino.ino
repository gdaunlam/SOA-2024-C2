#include "DHTesp.h"

#define CO2_KEY "CO2"
#define DIST_KEY "DIST"
#define HUM_KEY "HUM"
#define TEMP_KEY "TEMP"
#define OPEN_DOOR_KEY "OPEN_DOOR"
#define STATE_KEY "STATE"

typedef int STATE_VALUES;
const STATE_VALUES CO2_HIGH_PORCENT = 5;
const STATE_VALUES TEMP_HIGH = 35;
const STATE_VALUES CO2_HIGH = 1000;
const STATE_VALUES MIN_HUM_HIGH = 20;
const STATE_VALUES MAX_HUM_HIGH = 70;

const STATE_VALUES TEMP_MID = 32;
const STATE_VALUES CO2_MID = 600;
const STATE_VALUES MIN_HUM_MID = 30;
const STATE_VALUES MAX_HUM_MID = 60;

const STATE_VALUES TEMP_LOW = 24;
const STATE_VALUES CO2_LOW = 300;
const STATE_VALUES MIN_HUM_LOW = 35;
const STATE_VALUES MAX_HUM_LOW = 50;
const STATE_VALUES DIST_LOW = 5;

typedef int STATE;
const STATE INIT_STATE = 0;
const STATE L_STATE = 1;
const STATE M_STATE = 2;
const STATE H_STATE = 3;
const STATE C_STATE = 4;
const int MAX_STATES = 5;

typedef int EVENT;
const EVENT INIT_EVENT = 0;
const EVENT L_EVENT = 1;
const EVENT M_EVENT = 2;
const EVENT H_EVENT = 3;
const EVENT C_EVENT = 4;
const EVENT CO2_C_EVENT = 5;
const EVENT CO2_H_EVENT = 6;
const EVENT CO2_M_EVENT = 7;
const EVENT CO2_L_EVENT = 8;
const EVENT DIST_M_EVENT = 9;
const EVENT DIST_L_EVENT = 10;
const EVENT MIN_HUM_C_EVENT = 11;
const EVENT MAX_HUM_C_EVENT = 12;
const EVENT MIN_HUM_H_EVENT = 13;
const EVENT MAX_HUM_H_EVENT = 14;
const EVENT MIN_HUM_M_EVENT = 15;
const EVENT MAX_HUM_M_EVENT = 16;
const EVENT MAX_HUM_L_EVENT = 17;
const EVENT TEMP_C_EVENT = 18;
const EVENT TEMP_H_EVENT = 19;
const EVENT TEMP_M_EVENT = 20;
const EVENT TEMP_L_EVENT = 21;
const int MAX_EVENTS = 22;

typedef int EVENTS_GROUPS;
const EVENTS_GROUPS SENSORS_EVENTS = 0;
const EVENTS_GROUPS EMBED_EVENTS = 1;
const int EVENTS_GROUPS_COUNT = 2;

typedef int SENSOR;
const SENSOR CO2_SENSOR = 0;
const SENSOR DIST_SENSOR = 1;
const SENSOR HUM_SENSOR = 2;
const SENSOR TEMP_SENSOR = 3;
const int SENSORS_COUNT = 4;

STATE currentState = INIT_STATE;
STATE nextState = INIT_STATE;
SENSOR currentSensor = CO2_SENSOR;
EVENTS_GROUPS currentEventGroup = SENSORS_EVENTS;
long DIST_VALUE = 0;
float TEMP_VALUE = 0;
float HUM_VALUE = 0;
float CO2_VALUE = 0;

STATE state_table_next_state[MAX_EVENTS][MAX_STATES] = {
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // INIT_EVENT
  { L_STATE, L_STATE, L_STATE, L_STATE, L_STATE },  // L_EVENT
  { M_STATE, M_STATE, M_STATE, M_STATE, M_STATE },  // M_EVENT
  { H_STATE, H_STATE, H_STATE, H_STATE, H_STATE },  // H_EVENT
  { C_STATE, C_STATE, C_STATE, C_STATE, C_STATE },  // C_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // CO2_C_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // CO2_H_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // CO2_M_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // CO2_L_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // DIST_M_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // DIST_L_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // MIN_HUM_C_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // MAX_HUM_C_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // MIN_HUM_H_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // MAX_HUM_H_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // MIN_HUM_M_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // MAX_HUM_M_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // MAX_HUM_L_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // TEMP_C_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // TEMP_H_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // TEMP_M_EVENT
  { L_STATE, L_STATE, M_STATE, H_STATE, C_STATE },  // TEMP_L_EVENT
  // INIT_STATE, L_STATE, M_STATE, H_STATE, C_STATE
};

typedef void (*transition)();
void low() {
  setColorLedGreen();
  turnOffBuzzer();
  turnOffRelay();
  ntfyState();
}
void mid() {
  setColorLedYellow();
  turnOffBuzzer();
  turnOffRelay();
  ntfyState();
}
void high() {
  setColorLedOrange();
  turnOffBuzzer();
  turnOnRelay();
  ntfyState();
}
void crit() {
  setColorLedRed();
  turnOnBuzzer();
  turnOnRelay();
  ntfyState();
}
void none() {}

transition state_table_actions[MAX_EVENTS][MAX_STATES] = {
  { low, none, none, none, none },                           // INIT_EVENT
  { low, none, low, low, low },                              // L_EVENT
  { mid, mid, none, mid, mid },                              // M_EVENT
  { high, high, high, none, high },                          // H_EVENT
  { crit, crit, crit, crit, none },                          // C_EVENT
  { ntfySensor, ntfySensor, ntfySensor, ntfySensor, none },  // CO2_C_EVENT
  { ntfySensor, ntfySensor, ntfySensor, none, none },        // CO2_H_EVENT
  { ntfySensor, ntfySensor, none, none, none },              // CO2_M_EVENT
  { ntfySensor, none, none, none, none },                    // CO2_L_EVENT
  { ntfySensor, ntfySensor, none, none, none },              // DIST_M_EVENT
  { ntfySensor, none, none, none, none },                    // DIST_L_EVENT
  { ntfySensor, ntfySensor, ntfySensor, ntfySensor, none },  // MIN_HUM_C_EVENT
  { ntfySensor, ntfySensor, ntfySensor, ntfySensor, none },  // MAX_HUM_C_EVENT
  { ntfySensor, ntfySensor, ntfySensor, none, none },        // MIN_HUM_H_EVENT
  { ntfySensor, ntfySensor, ntfySensor, none, none },        // MAX_HUM_H_EVENT
  { ntfySensor, ntfySensor, none, none, none },              // MIN_HUM_M_EVENT
  { ntfySensor, ntfySensor, none, none, none },              // MAX_HUM_M_EVENT
  { ntfySensor, none, none, none, none },                    // MAX_HUM_L_EVENT
  { ntfySensor, ntfySensor, ntfySensor, ntfySensor, none },  // TEMP_C_EVENT
  { ntfySensor, ntfySensor, ntfySensor, none, none },        // TEMP_H_EVENT
  { ntfySensor, ntfySensor, none, none, none },              // TEMP_M_EVENT
  { ntfySensor, none, none, none, none },                    // TEMP_L_EVENT
  // INIT_STATE, L_STATE, M_STATE, H_STATE, C_STATE
};

String getStateName(int state) {
  switch (state) {
    case INIT_STATE:
      return "inicial";
    case L_STATE:
      return "low";
    case M_STATE:
      return "medium";
    case H_STATE:
      return "high";
    case C_STATE:
      return "critical";
  }
}
void ntfyState() {
  Serial.println("ha ocurrido un cambio de estado del estado: " + getStateName(currentState) + " al estado: " + getStateName(nextState));
  sendEventsMqtt(getStateName(nextState), STATE_KEY);
}
void ntfySensor() {
  switch (currentSensor) {
    case CO2_SENSOR: {
      float co2 = CO2_VALUE * CO2_HIGH_PORCENT / CO2_HIGH;
      Serial.println("el CO2 en el ambiente ha cambiado a " + String(co2) + "%");
      sendEventsMqtt(String(co2), CO2_KEY);
      break;
    }
    case DIST_SENSOR: {
      bool open = DIST_VALUE > DIST_LOW;
      Serial.println(String("la puerta ha sido ") + (open ? "abierta" : "cerrada"));
      sendEventsMqtt(String(open), OPEN_DOOR_KEY);
      break;
    }
    case HUM_SENSOR: {
      Serial.println("la humedad en el ambiente ha cambiado a " + String(HUM_VALUE) + "%");
      sendEventsMqtt(String(HUM_VALUE), HUM_KEY);
      break;
    }
    case TEMP_SENSOR: {
      Serial.println("la temperatura en el ambiente ha cambiado a " + String(TEMP_VALUE) + "°c");
      sendEventsMqtt(String(TEMP_VALUE), TEMP_KEY);
      break;
    }
  }
}
void readSensors() {
  currentSensor++;
  currentSensor = currentSensor % SENSORS_COUNT;
  TempAndHumidity dataDTH;

  switch (currentSensor) {
    case CO2_SENSOR:
      CO2_VALUE = readC02Data();
      break;
    case DIST_SENSOR:
      DIST_VALUE = readUltrasonicDistance();
      break;
    case HUM_SENSOR:
      dataDTH = readDHT();
      HUM_VALUE = dataDTH.humidity;
      break;
    case TEMP_SENSOR:
      dataDTH = readDHT();
      TEMP_VALUE = dataDTH.temperature;
      break;
  }
}

EVENT getSensorsEvent() {
  switch (currentSensor) {
    case CO2_SENSOR:
      if (CO2_VALUE > CO2_HIGH)
        return CO2_C_EVENT;
      if (CO2_VALUE > CO2_MID)
        return CO2_H_EVENT;
      if (CO2_VALUE > CO2_LOW)
        return CO2_M_EVENT;
      return CO2_L_EVENT;

    case DIST_SENSOR:
      if (DIST_VALUE > DIST_LOW)
        return DIST_M_EVENT;
      return DIST_L_EVENT;

    case HUM_SENSOR:
      if (HUM_VALUE < MIN_HUM_HIGH)
        return MIN_HUM_C_EVENT;
      if (HUM_VALUE > MAX_HUM_HIGH)
        return MAX_HUM_C_EVENT;
      if (HUM_VALUE < MIN_HUM_MID)
        return MIN_HUM_H_EVENT;
      if (HUM_VALUE > MAX_HUM_MID)
        return MAX_HUM_H_EVENT;
      if (HUM_VALUE < MIN_HUM_LOW)
        return MIN_HUM_M_EVENT;
      if (HUM_VALUE > MAX_HUM_LOW)
        return MAX_HUM_M_EVENT;
      return MAX_HUM_L_EVENT;

    case TEMP_SENSOR:
      if (TEMP_VALUE > TEMP_HIGH)
        return TEMP_C_EVENT;
      if (TEMP_VALUE > TEMP_MID)
        return TEMP_H_EVENT;
      if (TEMP_VALUE > TEMP_LOW)
        return TEMP_M_EVENT;
      return TEMP_L_EVENT;
  }
}

EVENT getEmbedEvent() {
  if ((TEMP_VALUE > TEMP_HIGH) || (CO2_VALUE > CO2_HIGH) || (HUM_VALUE < MIN_HUM_HIGH) || (HUM_VALUE > MAX_HUM_HIGH))
    return C_EVENT;
  if ((TEMP_VALUE > TEMP_MID) || (CO2_VALUE > CO2_MID) || (HUM_VALUE < MIN_HUM_MID) || (HUM_VALUE > MAX_HUM_MID))
    return H_EVENT;
  if ((TEMP_VALUE > TEMP_LOW) || (CO2_VALUE > CO2_LOW) || (HUM_VALUE < MIN_HUM_LOW) || (HUM_VALUE > MAX_HUM_LOW) || (DIST_VALUE > DIST_LOW))
    return M_EVENT;
  return L_EVENT;
}

EVENT getEvent() {
  currentEventGroup++;
  currentEventGroup = currentEventGroup % EVENTS_GROUPS_COUNT;
  switch (currentEventGroup) {
    case SENSORS_EVENTS:
      readSensors();
      return getSensorsEvent();
    case EMBED_EVENTS:
      return getEmbedEvent();
  }
}

void setup() {
  Serial.begin(115200);
  initSensors();
  initActuators();
  initConnections();
}

void loop() {
  loopConnections();
  sendValuesMqtt(String(CO2_VALUE), String(DIST_VALUE), String(HUM_VALUE), String(TEMP_VALUE), String(nextState));
  loopActuators();

  EVENT event = getEvent();
  nextState = state_table_next_state[event][currentState];
  transition action = state_table_actions[event][currentState];
  action();
  currentState = nextState;
}