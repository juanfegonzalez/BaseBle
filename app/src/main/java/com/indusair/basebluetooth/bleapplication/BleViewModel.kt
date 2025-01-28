package com.indusair.basebluetooth.bleapplication


import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import java.util.*


@SuppressLint("MissingPermission")
class BluetoothViewModel(application: Application) : AndroidViewModel(application) {

    private val bleManager = MyBleManager(application)

    // Datos de notificaciones
    private val _notifications = MutableStateFlow("")
    val notifications = _notifications.asStateFlow()

    fun connectAndSubscribe(macAddress: String, characteristicUuid: UUID) {
        viewModelScope.launch {
            try {
                val deviceToConnect = devices.value.find { it.address == "A0:A3:B3:2B:09:72" }

                if (deviceToConnect == null) {
                    return@launch
                }
                Log.d(TAG, "Intentando conectar con: $deviceToConnect")
                var ayuda = bleManager.connect(deviceToConnect)
                    .retry(3, 100) // Intentar reconectar 3 veces si falla
                    .useAutoConnect(false)
                    .timeout(10000) // Timeout de 10 segundos
                    .done {
                        bleManager.enableNotifications { data ->
                            Log.d(
                                TAG,
                                "Notificado"
                            )
                            _notifications.value = String(data.value!!)
                            if (_notifications.value != "")
                                bleManager.disableNotifications()

                        }
                    }.fail { device, status ->
                        Log.d(
                            TAG,
                            "fallo al conectarse con status: $status"
                        )
                    }
                    .enqueue()

            } catch (e: Exception) {
                val errorMessage = "Error al conectar: ${e.message}"
                //_connectionState.value = DISCONNECTED
                Log.d(
                    TAG,
                    "Desconectado con error $errorMessage "
                )
            }
        }
    }

    // --- Constantes ---
    private val TAG = "BluetoothViewModel"

    // --- Adaptador Bluetooth y mapas de conexiones ---
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()


    // --- StateFlows para exponer estados a la UI ---
    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _receivedData = MutableStateFlow("")
    val receivedData: StateFlow<String> = _receivedData

    // --- Configuración de escaneo ---
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setReportDelay(0L) // Procesar resultados en tiempo real
        .build()

    // Conjunto temporal para no repetir dispositivos en la lista
    private val discoveredDevices = mutableSetOf<BluetoothDevice>()

    // Callback de escaneo
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device.name != null && discoveredDevices.add(device)) {
                _devices.value = discoveredDevices.toList()
                Log.d(
                    TAG,
                    "Dispositivo encontrado: ${device.name}, MAC: ${device.address}, RSSI: ${result.rssi}"
                )
            }
        }
    }

    /**
     * Inicia el escaneo de dispositivos BLE.
     */
    fun startScan() {
        viewModelScope.launch(Dispatchers.IO) {
            if (bluetoothAdapter?.isEnabled == true) {
                _isScanning.value = true
                discoveredDevices.clear()
                bluetoothAdapter.bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)
                // Escanea durante 20 segundos
                delay(20000)
                stopScan()
            } else {
                Log.w(TAG, "Bluetooth está desactivado o no disponible en este dispositivo.")
            }
        }
    }

    /**
     * Detiene el escaneo de dispositivos BLE.
     */
    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        _isScanning.value = false
    }

    /**
     * Conecta al dispositivo BLE especificado por su dirección MAC.
     */
    fun connectToDevice(macAddress: String) {
        viewModelScope.launch {
            connectAndSubscribe(macAddress, UUID.fromString("6216b086-e6d8-4f68-8891-87c9ea36fa5"))
        }

    }

}



