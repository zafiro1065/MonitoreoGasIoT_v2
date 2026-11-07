package com.example.monitoreogasiot

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
// CAMBIO: Importamos la función de extensión toColorInt() para colores en Kotlin
import androidx.core.graphics.toColorInt
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.monitoreogasiot.databinding.ActivityMainBinding
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    @Volatile private var isListening = true

    companion object {
        private val HC05_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val DEVICE_MAC = "00:21:13:01:40:88"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        connectToBluetooth()
    }

    private fun setupUI() {
        binding.sliderUmbral.addOnChangeListener { _, value, _ ->
            val valInt = value.toInt()
            // CAMBIO: Uso de getString(R.string...) en lugar de concatenación directa para evitar advertencias
            binding.tvUmbralValor.text = getString(R.string.umbral_alerta, valInt)
        }

        binding.rgModo.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                // CAMBIO: Textos de Toast extraídos a strings.xml
                R.id.rbModoContinuo -> Toast.makeText(this, getString(R.string.modo_continuo_msg), Toast.LENGTH_SHORT).show()
                R.id.rbModoAhorro -> Toast.makeText(this, getString(R.string.modo_ahorro_msg), Toast.LENGTH_SHORT).show()
            }
        }

        binding.switchValvula.setOnCheckedChangeListener { _, isChecked ->
            val comando = if (isChecked) "VALVULA:CERRAR\n" else "VALVULA:ABRIR\n"
            sendBluetoothData(comando)
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // CAMBIO: Textos extraídos a strings.xml
                Toast.makeText(this, getString(R.string.error_permiso_bt), Toast.LENGTH_LONG).show()
                binding.tvEstadoConexion.text = getString(R.string.estado_sin_permiso)
                return
            }
        }

        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        @Suppress("DEPRECATION")
        val adapter: BluetoothAdapter? = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()

        if (adapter == null || !adapter.isEnabled) {
            // CAMBIO: Textos extraídos a strings.xml
            Toast.makeText(this, getString(R.string.activar_bt_msg), Toast.LENGTH_LONG).show()
            binding.tvEstadoConexion.text = getString(R.string.estado_bt_desactivado)
            return
        }

        thread {
            try {
                val device = adapter.getRemoteDevice(DEVICE_MAC)
                bluetoothSocket = device.createRfcommSocketToServiceRecord(HC05_UUID)
                bluetoothSocket?.connect()
                inputStream = bluetoothSocket?.inputStream
                outputStream = bluetoothSocket?.outputStream

                runOnUiThread {
                    // CAMBIO: Texto extraído a strings.xml
                    binding.tvEstadoConexion.text = getString(R.string.estado_conectado)
                }
                listenForData()
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    // CAMBIO: Texto extraído a strings.xml
                    binding.tvEstadoConexion.text = getString(R.string.estado_error)
                }
            }
        }
    }

    private fun listenForData() {
        val buffer = ByteArray(1024)
        thread {
            while (isListening) {
                try {
                    val bytes = inputStream?.read(buffer) ?: -1
                    if (bytes > 0) {
                        val mensaje = String(buffer, 0, bytes).trim()
                        if (mensaje.startsWith("GAS:")) {
                            val valorGas = mensaje.removePrefix("GAS:").toIntOrNull() ?: 0
                            runOnUiThread { updateUIState(valorGas) }
                        }
                    }
                } catch (e: Exception) {
                    isListening = false
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateUIState(gasValue: Int) {
        // CAMBIO: Uso de getString(R.string...) con formato para concatenar la variable
        binding.tvLecturaGas.text = getString(R.string.lectura_gas, gasValue)

        when {
            gasValue < 300 -> {
                // CAMBIO: Texto extraído a strings.xml y uso de .toColorInt() en vez de Color.parseColor()
                binding.tvNivelRiesgo.text = getString(R.string.riesgo_normal)
                binding.cardEstado.setCardBackgroundColor("#4CAF50".toColorInt())
            }
            gasValue in 300..599 -> {
                // CAMBIO: Texto extraído a strings.xml y uso de .toColorInt()
                binding.tvNivelRiesgo.text = getString(R.string.riesgo_advertencia)
                binding.cardEstado.setCardBackgroundColor("#FFC107".toColorInt())
            }
            else -> {
                // CAMBIO: Texto extraído a strings.xml y uso de .toColorInt()
                binding.tvNivelRiesgo.text = getString(R.string.riesgo_peligro)
                binding.cardEstado.setCardBackgroundColor("#F44336".toColorInt())
            }
        }
    }

    private fun sendBluetoothData(cmd: String) {
        thread {
            try {
                outputStream?.write(cmd.toByteArray())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isListening = false
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
