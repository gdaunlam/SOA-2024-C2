typedef int STATE;
const STATE LOW_STATE = 0;
const STATE MEDIUM_STATE = 1;
const STATE HIGH_STATE = 2;
const STATE CRITICAL_STATE = 3;
const int MAX_STATES = 4;

typedef int EVENT;
const EVENT LOW_CONDITION_EVENT = 0;
const EVENT MID_CONDITION_EVENT = 1;
const EVENT HIGHT_CONDITION_EVENT = 2;
const EVENT CRITICAL_CONDITION_EVENT = 3;
const int MAX_EVENTS = 4;

typedef int SENSOR;
const SENSOR C02_SENSOR = 0;
const SENSOR DISTANCE_SENSOR = 1;
const SENSOR TEMP_HUMIDITY_SENSOR = 2;
const int SENSORS_COUNT = 3;

STATE actualState = LOW_STATE;
SENSOR actualSensor = C02_SENSOR;
long DISTANCE_VALUE = 0; //TODO CM?
long TEMPERATURE_VALUE = 0;
long HUMIDITY_VALUE = 0;
long C02_VALUE = 0;

typedef void (*transition)();
void lToM() {
  Serial.println("lToM");
  return;
}
void mToL() {
  Serial.println("mToL");
  return;
}
void mToH() {
  Serial.println("mToH");
  return;
}
void hToM() {
  Serial.println("hToM");
  return;
}
void hToC() {
  Serial.println("hToC");
  return;
}
void cToH() {
  Serial.println("cToH");
  return;
}
void none() {
  Serial.println("none");
  return;
}

STATE state_table_next_state[MAX_STATES][MAX_EVENTS] =
{
  {LOW_STATE, MEDIUM_STATE, MEDIUM_STATE, MEDIUM_STATE }, //LOW_STATE
  {LOW_STATE, MEDIUM_STATE, HIGH_STATE, HIGH_STATE}, //MEDIUM_STATE
  {MEDIUM_STATE, MEDIUM_STATE, HIGH_STATE, CRITICAL_STATE}, //HIGH_STATE
  {HIGH_STATE, HIGH_STATE, HIGH_STATE, CRITICAL_STATE }, //CRITICAL_STATE
  //LOW_CONDITION_EVENT, MID_CONDITION_EVENT, HIGHT_CONDITION_EVENT, CRITICAL_CONDITION_EVENT
};
transition state_table_actions[MAX_STATES][MAX_EVENTS] =
{
  {none,  lToM,  lToM,  lToM }, //LOW_STATE
  {mToL,  none, mToH, mToH},  //MEDIUM_STATE
  {hToM, hToM, none,  hToC}, //HIGH_STATE
  {cToH, cToH, cToH, none },  //CRITICAL_STATE
  //LOW_CONDITION_EVENT, MID_CONDITION_EVENT, HIGHT_CONDITION_EVENT, CRITICAL_CONDITION_EVENT
};

String getSensorName(SENSOR sensor) {
  switch (actualSensor) {
    case C02_SENSOR : return "C02_SENSOR";
    case DISTANCE_SENSOR : return "DISTANCE_SENSOR";
    case TEMP_HUMIDITY_SENSOR : return "TEMP_HUMIDITY_SENSOR";
    default: return "NONE";
  }
}
void readSensors() {
  actualSensor++;
  actualSensor = actualSensor % SENSORS_COUNT;

  Serial.print("Please enter a value for: ");
  Serial.println(getSensorName(actualSensor));
  while (!Serial.available()); // Wait for input
  String valueString = Serial.readStringUntil('\n');
  const int value = valueString.toInt();

  switch (actualSensor) {
    case C02_SENSOR:
      C02_VALUE = value;
      break;
    case DISTANCE_SENSOR:
      DISTANCE_VALUE = value;
      break;
    case TEMP_HUMIDITY_SENSOR:
      HUMIDITY_VALUE = value;
      TEMPERATURE_VALUE = value;
      break;
    default:
      actualSensor = C02_SENSOR;
      break;
  }
}
void printSensors() {
  Serial.print("Sensonrs: co2:");
  Serial.print(C02_VALUE);
  Serial.print(" distance:");
  Serial.print(DISTANCE_VALUE);
  Serial.print(" humidity: ");
  Serial.println(HUMIDITY_VALUE);
}

EVENT getEvent() {
  if (TEMPERATURE_VALUE > 60 || C02_VALUE > 60) return  CRITICAL_CONDITION_EVENT;
  if (TEMPERATURE_VALUE > 30 || C02_VALUE > 30 || HUMIDITY_VALUE > 30) return HIGHT_CONDITION_EVENT;
  if (TEMPERATURE_VALUE > 20 || C02_VALUE > 10 || HUMIDITY_VALUE > 10 || DISTANCE_VALUE > 5) return MID_CONDITION_EVENT;
  return LOW_CONDITION_EVENT;
}

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  Serial.println("Hello, ESP32!");
}

void loop() {
  readSensors();
  printSensors();
  EVENT event = getEvent();
  STATE nextState = state_table_next_state[actualState][event];
  transition action = state_table_actions[actualState][event];
  action();
  actualState = nextState;
  delay(10); // this speeds up the simulation
}