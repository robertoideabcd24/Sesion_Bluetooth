package com.example.sesion_bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ListView listViewDispositivos;
    private ArrayAdapter<String> dispositivosArrayAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice hc05Device;
    private InputStream inputStream;
    private TextView tvDatos;

    private static final UUID UUID_HC05 = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewDispositivos = findViewById(R.id.listViewDispositivos);
        Button btnBuscar = findViewById(R.id.btnBuscar);
        tvDatos = findViewById(R.id.tvDatos);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        dispositivosArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listViewDispositivos.setAdapter(dispositivosArrayAdapter);

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth no soportado", Toast.LENGTH_LONG).show();
            return;
        }

        // Verifica permisos de Bluetooth en tiempo de ejecución
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_PERMISSION_REQUEST_CODE);
            }
        }

        // Verifica permisos de escritura en el almacenamiento externo
        checkStoragePermission();

        btnBuscar.setOnClickListener(v -> buscarDispositivos());
        listViewDispositivos.setOnItemClickListener((parent, view, position, id) -> conectarDispositivo(position));
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
    }

    private void buscarDispositivos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            Set<BluetoothDevice> dispositivosEmparejados = bluetoothAdapter.getBondedDevices();
            dispositivosArrayAdapter.clear();

            if (dispositivosEmparejados.size() > 0) {
                for (BluetoothDevice device : dispositivosEmparejados) {
                    dispositivosArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else {
                dispositivosArrayAdapter.add("No hay dispositivos emparejados");
            }
        } else {
            Toast.makeText(this, "Permiso Bluetooth necesario", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_PERMISSION_REQUEST_CODE);
        }
    }

    private void conectarDispositivo(int position) {
        String info = dispositivosArrayAdapter.getItem(position);
        String address = info.substring(info.length() - 17);

        hc05Device = bluetoothAdapter.getRemoteDevice(address);

        new Thread(() -> {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                bluetoothSocket = hc05Device.createRfcommSocketToServiceRecord(UUID_HC05);
                bluetoothSocket.connect();
                runOnUiThread(() -> Toast.makeText(this, "Conectado a " + hc05Device.getName(), Toast.LENGTH_SHORT).show());
                leerDatos();
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void leerDatos() {
        new Thread(() -> {
            try {
                inputStream = bluetoothSocket.getInputStream();
                StringBuilder datosRecibidos = new StringBuilder();  // Usamos StringBuilder para acumular los datos
                byte[] buffer = new byte[256];
                int bytes;

                while (true) {
                    bytes = inputStream.read(buffer);
                    String datos = new String(buffer, 0, bytes);

                    Log.d("Bluetooth", "Datos recibidos: " + datos);

                    // Acumula los datos recibidos
                    datosRecibidos.append(datos);

                    Log.d("Bluetooth", "Datos acumulados: " + datosRecibidos.toString());

                    int saltoDeLinea = datosRecibidos.indexOf("\n");  // Busca el primer salto de línea

                    while (saltoDeLinea != -1) {
                        String mensajeCompleto = datosRecibidos.substring(0, saltoDeLinea).trim();  // Extraemos el mensaje completo
                        Log.d("Bluetooth", "Mensaje completo recibido: " + mensajeCompleto);

                        // Obtener la fecha y hora actual
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String fechaHora = sdf.format(new Date());

                        // Concatenar la fecha y hora con el mensaje
                        String mensajeConFecha = fechaHora + " - " + mensajeCompleto;

                        // Procesamos el mensaje en la UI
                        runOnUiThread(() -> {
                            dispositivosArrayAdapter.add(mensajeConFecha);  // Agrega el mensaje con fecha y hora a la UI
                        });

                        // Guardar los datos en un archivo
                        guardarEnArchivo(mensajeConFecha);

                        // Eliminamos el mensaje procesado del acumulador
                        datosRecibidos.delete(0, saltoDeLinea + 1);

                        // Buscamos otro salto de línea
                        saltoDeLinea = datosRecibidos.indexOf("\n");
                    }
                }
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Error al leer datos", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void guardarEnArchivo(String datos) {
        // Verifica si el almacenamiento externo está disponible para escribir
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // Obtén la ruta al directorio "Documents"
            File directorio = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "lecturas_bluetooth.txt");

            try {
                // Si el archivo no existe, crea uno nuevo
                if (!directorio.exists()) {
                    directorio.createNewFile();
                }

                // Abre el archivo en modo de agregar
                FileWriter writer = new FileWriter(directorio, true);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);

                // Escribe la lectura en el archivo con un salto de línea
                bufferedWriter.write(datos + "\n");

                // Cierra el archivo después de escribir
                bufferedWriter.close();

                // Muestra un mensaje en la UI
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Lectura guardada en " + directorio.getAbsolutePath(), Toast.LENGTH_SHORT).show());

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error al guardar lectura", Toast.LENGTH_SHORT).show());
            }
        } else {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "El almacenamiento externo no está disponible", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso Bluetooth concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso Bluetooth denegado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de escritura concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de escritura denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
