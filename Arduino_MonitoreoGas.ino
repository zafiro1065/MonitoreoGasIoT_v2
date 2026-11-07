#include <SoftwareSerial.h>

// Definición de pines para módulo Bluetooth HC-05
const int BT_RX = 2; // Pin RX de Arduino a TX de HC-05
const int BT_TX = 3; // Pin TX de Arduino a RX de HC-05 (con divisor de voltaje)

// Pines de sensores y actuadores
const int PIN_MQ2 = A0;      // Entrada analógica del sensor de gas MQ-2
const int PIN_VALVULA = 8;   // Salida Digital para Válvula / Relé (Fail-safe)

SoftwareSerial BTSerial(BT_RX, BT_TX);

int lecturaGas = 0;
String comandoEntrante = "";

void setup() {
  pinMode(PIN_VALVULA, OUTPUT);
  digitalWrite(PIN_VALVULA, LOW); // Estado inicial: Válvula abierta (normal)

  Serial.begin(9600);      // Monitor Serie para depuración
  BTSerial.begin(9600);    // Comunicación serie con HC-05
  Serial.println("Sistema OT Monitoreo de Gas Iniciado.");
}

void loop() {
  // 1. Lectura del sensor analógico MQ-2
  lecturaGas = analogRead(PIN_MQ2);

  // 2. Transmisión de telemetría a la App Android mediante Bluetooth SPP
  BTSerial.print("GAS:");
  BTSerial.println(lecturaGas);

  // 3. Lógica Fail-Safe (IEC 62443): Cierre automático por hardware si se supera el umbral crítico
  if (lecturaGas >= 600) {
    digitalWrite(PIN_VALVULA, HIGH); // Activar solenoide / CERRAR Válvula
    Serial.println("¡ALERTA OT! Nivel de peligro detectado. Válvula cerrada automáticamente.");
  }

  // 4. Procesamiento de comandos remotos desde la App Android
  while (BTSerial.available()) {
    char c = BTSerial.read();
    if (c == '\n') {
      comandoEntrante.trim();
      procesarComando(comandoEntrante);
      comandoEntrante = "";
    } else {
      comandoEntrante += c;
    }
  }

  delay(1000); // Muestreo cada 1 segundo
}

void procesarComando(String cmd) {
  if (cmd == "VALVULA:CERRAR") {
    digitalWrite(PIN_VALVULA, HIGH);
    Serial.println("Comando recibido: Válvula CERRADA manualmente.");
  } else if (cmd == "VALVULA:ABRIR") {
    digitalWrite(PIN_VALVULA, LOW);
    Serial.println("Comando recibido: Válvula ABIERTA manualmente.");
  }
}
