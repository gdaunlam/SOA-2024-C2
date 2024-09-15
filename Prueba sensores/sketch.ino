//URL: https://wokwi.com/projects/408520560202539009


#define PIN_TRIGGER_ULTRASONIDO 13  // Pin de Trig del sensor
#define PIN_ECHO_ULTRASONIDO 12 // Pin de Echo del sensor
#define PIN_DHT 32
#define POT_PIN 25 // Pin al que está conectado el potenciómetro
#define PIN_BUZZER 26
#define PIN_RELAY 14
#define PIN_LED_RED 23
#define PIN_LED_BLUE 18
#define PIN_LED_GREEN 22

#include "DHTesp.h"   //Libreria de sensor de T y H

DHTesp dht; //Instanciamos el DHT

void turnOnBuzzer(){
  //Depende de la humedad
  digitalWrite(PIN_BUZZER, HIGH);
  Serial.println("Buzzer prendido.");
}

void turnOffBuzzer(){
  //Depende de la humedad
  digitalWrite(PIN_BUZZER, LOW);
  Serial.println("Buzzer apagado.");
}

void turnOnRelay(){
  //Depende de la humedad
  digitalWrite(PIN_RELAY, HIGH);
  Serial.println("Rele prendido.");
}

void turnOffRelay(){
  //Depende de la humedad
  digitalWrite(PIN_RELAY, LOW);
  Serial.println("Rele apagado.");
}

TempAndHumidity readDHT(){
  return dht.getTempAndHumidity();
}

long readUltrasonicDistance()
{
  //Iniciamos el pin del emisor de reuido en salida
  pinMode(PIN_TRIGGER_ULTRASONIDO, OUTPUT);
  //Apagamos el emisor de sonido
  digitalWrite(PIN_TRIGGER_ULTRASONIDO, LOW);
  //Retrasamos la emision de sonido por 2 milesismas de segundo
  delayMicroseconds(2);
  // Comenzamos a emitir sonido
  digitalWrite(PIN_TRIGGER_ULTRASONIDO, HIGH);
  //Retrasamos la emision de sonido por 2 milesismas de segundo
  delayMicroseconds(10);
  //Apagamos el emisor de sonido
  digitalWrite(PIN_TRIGGER_ULTRASONIDO, LOW);
  //Comenzamos a escuchar el sonido
  pinMode(PIN_ECHO_ULTRASONIDO, INPUT);
  // Calculamos el tiempo que tardo en regresar el sonido
  return pulseIn(PIN_ECHO_ULTRASONIDO, HIGH);
}

int leerDatosPotenciometro(){
  // Leer el valor del potenciómetro
  int auxPpmValue = analogRead(POT_PIN);
  // Mapear el valor del potenciómetro de 0 a 150
  return map(auxPpmValue, 0, 4095, 0, 255);
}

void setColorLed(int red, int green, int blue) {
  delay(3000);
  analogWrite(PIN_LED_RED, red);
  analogWrite(PIN_LED_GREEN, green);
  analogWrite(PIN_LED_BLUE, blue);
}

void setup() {
  Serial.begin(115200);  
  pinMode(PIN_TRIGGER_ULTRASONIDO, OUTPUT);
  pinMode(PIN_ECHO_ULTRASONIDO, INPUT); 
  pinMode(POT_PIN, INPUT); 
  pinMode(PIN_BUZZER, OUTPUT);
  pinMode(PIN_RELAY, OUTPUT);
  pinMode(PIN_LED_RED, OUTPUT);
  pinMode(PIN_LED_BLUE, OUTPUT);
  pinMode(PIN_LED_GREEN, OUTPUT);
  dht.setup(PIN_DHT, DHTesp::DHT22);  // Inicializar el sensor DHT
}

void loop() {
  // put your main code here, to run repeatedly:
  long distance;
  TempAndHumidity dataDTH;

  // Calcula la distancia en centímetros (velocidad del sonido: 34300 cm/s)
  distance = 0.017 * readUltrasonicDistance();
  dataDTH = readDHT();

  // Imprime la distancia en el monitor serial
  Serial.print("Distancia: ");
  Serial.print(distance);
  Serial.println(" cm");
  Serial.println("---");
  // Imprime la temperatura y humedad actual
  float temperatura = dataDTH.temperature;
  if(temperatura >50){
    turnOnBuzzer();
    turnOnRelay();
  } else {
    turnOffBuzzer();
    turnOffRelay();
  }

  setColorLed(255,0,0); //Rojo
  setColorLed(255,165,0); //Naranja
  setColorLed(255,255,0); //Amarillo
  setColorLed(0,255,0); //Verde

  Serial.println("Temperatura: " + String(dataDTH.temperature, 2) + "°C");
  Serial.println("Humedad: " + String(dataDTH.humidity, 1) + "%");
  Serial.println("---");
  Serial.print("CO2 (ppm):");
  int ppm = leerDatosPotenciometro();
  Serial.println(ppm);
  delay(1000); // Espera un segundo antes de medir nuevamente
}
