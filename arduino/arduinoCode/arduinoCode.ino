#include <dht11.h>
const int pumpPinO = 12;
const int fanPinO = 9;
const int moisturePinI = A0;
const int humidityTempPinI = 10;


int Threshold = 50;
int Margin = 5;
unsigned long WateringInterval = 5000;
unsigned long fanUntil = 0;

unsigned long WateringWaiting = 900000;
bool isWaitingWatering = false;
unsigned long  pauseWatering = 0;
unsigned long pumpUntil = 0;
unsigned long pumpIsRunning = 0;

dht11 DHT11;


void setup() {
  Serial.begin(9600);

  pinMode(pumpPinO, OUTPUT);
  pinMode(fanPinO, OUTPUT);
}

float getSoilHumidity() {

  return map( analogRead(moisturePinI), 1023, 0, 0, 100);
}

void readAir() {
  int chk = DHT11.read(humidityTempPinI);
}


void loop() {
  if (Serial.available() > 0) {
    String data = Serial.readStringUntil('\n');
    //Serial.println(data);
    if (data.charAt(0) == '0') {
      data = data.substring(2);
      Threshold = data.toInt();
    }
    else if (data.charAt(0) == '1') {
      int soil_humidity = getSoilHumidity();
      readAir();
      int air_humidity = DHT11.humidity;
      int temperature = DHT11.temperature;
      String dataToSend = String(soil_humidity) + ":" + String(temperature) + ":" + String(air_humidity);
      Serial.println(dataToSend);
    }
    else if (data.charAt(0) == '2') {
      data = data.substring(2);
      fanUntil = millis() + data.toInt();
   
      digitalWrite(fanPinO, HIGH);
    }
  }
  int soil_humidity = getSoilHumidity();


  if (soil_humidity < Threshold - Margin && isWaitingWatering == false) {
    pumpUntil = millis() + WateringInterval;
    digitalWrite(pumpPinO, HIGH);
    isWaitingWatering = true;
    pauseWatering = millis() + WateringWaiting;
  }

  if (millis() >= pumpUntil) {
    digitalWrite(pumpPinO, LOW);
  }

  if (millis() >= fanUntil) {
    digitalWrite(fanPinO, LOW);
  }


  if (millis() >= pauseWatering) {
    isWaitingWatering = false;
  }


}
