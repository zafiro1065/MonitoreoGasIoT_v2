# Sistema IoT de Monitoreo de Gas (MQ-2) y Seguridad OT

Sistema de monitoreo de concentración de gas en tiempo real mediante arquitectura Android (Kotlin) y microcontrolador Arduino UNO (C++), integrado bajo estándares de ciberseguridad industrial **IEC 62443** (Control OT) e **ISO/IEC 27001** (Autenticación Segura).

---

## Características Principales

- **Control de Acceso Seguro (ISO/IEC 27001):** Autenticación en la App Android mediante derivación de credenciales con hashing **SHA-256**.
- **Monitoreo en Tiempo Real:** Visualización continua de concentración de gas (PPM) del sensor MQ-2 mediante conexión Bluetooth SPP (HC-05).
- **Seguridad Operacional (IEC 62443):** Protocolo de comandos estructurados (`VALVULA:ABRIR` / `VALVULA:CERRAR`) para mitigar inyecciones maliciosas de tramas serie.
- **Fail-Safe Descentralizado:** El microcontrolador ejecuta de forma autónoma el cierre de la válvula de emergencia ante niveles peligrosos ($\ge 600$ PPM) sin depender de la conectividad de la app.

---

## Tecnologías y Hardware Utilizado

| Componente | Descripción / Especificación |
| :--- | :--- |
| **Android App** | Kotlin, Material Design Components, Bluetooth Socket (SPP) |
| **Microcontrolador** | Arduino UNO R3 (C++) |
| **Sensor de Gas** | MQ-2 (LPG, Propano, Humo, CO) |
| **Módulo Bluetooth** | HC-05 (UART a 9600 baudios) |
| **Actuador** | Servomotor / Módulo Relé de Válvula de Emergencia |

---

## 📂 Estructura del Repositorio

- `/MonitoreoGasIoT.zip` — Proyecto completo de Android Studio en Kotlin.
- `/Arduino_MonitoreoGas.ino` — Sketch fuente del firmware de Arduino UNO.
