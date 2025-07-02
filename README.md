# 📲 Sesión Bluetooth – Android + Arduino  

Aplicación Android desarrollada en Java que se conecta vía Bluetooth a un módulo como **HC-05**, recibe datos desde un **Arduino** y los guarda en un archivo `.txt` con marcas de tiempo.  

## 🚀 Funcionalidades  
- 🔍 **Escaneo** de dispositivos Bluetooth emparejados  
- 🔗 **Conexión directa** a módulos Bluetooth (HC-05, HC-06, etc.)  
- 📥 **Recepción en tiempo real** de datos seriales desde Arduino  
- 💾 **Almacenamiento automático** en `Documents/lecturas_bluetooth.txt`  
- ⏱ **Marcas de tiempo** en cada registro (`YYYY-MM-DD HH:MM:SS`)  
- 📱 **Interfaz minimalista** con estado de conexión  

## ⚙️ Requisitos  
### Hardware  
- Dispositivo Android (4.4+) con Bluetooth  
- Módulo Bluetooth (HC-05 recomendado)  
- Placa Arduino (UNO, Nano, etc.)  

### Software  
- Android Studio (para compilar)  
- IDE Arduino (para programar el microcontrolador)  

### Permisos  
```xml
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
                 android:maxSdkVersion="28"/> <!-- Solo para Android < 10 -->


� Estructura del Proyecto

/Sesion_Bluetooth
├── /app
│   ├── /src/main
│   │   ├── /java/com/example/bluetooth
│   │   │   └── MainActivity.java  # Lógica principal
│   │   └── /res
│   │       ├── /layout
│   │       │   └── activity_main.xml  # Interfaz gráfica
│   │       └── /values
│   │           └── strings.xml  # Textos de la app
├── /arduino
│   └── sensor_example.ino  # Código de ejemplo para Arduino
└── lecturas_bluetooth.txt  # Archivo generado automáticamente

📄 Ejemplo de Datos

2025-07-02 18:23:45 - Temperatura: 32.5°C
2025-07-02 18:24:01 - Humedad: 45%
2025-07-02 18:24:17 - Sensor: 1,024

🛠 Tecnologías Utilizadas

Lenguaje: Java (Android SDK)
Comunicación: Bluetooth RFCOMM (Serial)
Almacenamiento: FileWriter + BufferedWriter
Manejo de tiempo: SimpleDateFormat

📌 Pasos para Usar

Programa tu Arduino con el sketch de ejemplo
Empareja el HC-05 con tu teléfono (PIN: 1234 por defecto)
Abre la app y selecciona tu módulo Bluetooth
Inicia la conexión y verifica los datos recibidos
Encuentra los datos en:
Android/data/com.example.bluetooth/files/Documents/lecturas_bluetooth.txt

🚨 Troubleshooting
Problema	Solución
No detecta dispositivos	Verifica que el módulo esté en modo pairing (LED parpadeando)
Conexión fallida	Revisa los baudios (9600 usualmente)
No guarda archivos	Checa permisos de almacenamiento en Android 10+


📜 Licencia

GNU GENERAL PUBLIC LICENSE v3.0
Este software puede ser modificado y redistribuido libremente
siempre que se mantenga esta licencia y se atribuya al autor.

👨‍💻 Autor
Roberto Castillo
https://img.shields.io/badge/GitHub-@robertoideabcd24-blue
🔹 Ingeniería en Software - UACM


