package com.indusair.basebluetooth.bleapplication

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import no.nordicsemi.android.ble.data.Data
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.BleManager


open class MyBleManager(context: Context) : BleManager(context) {
    private var notificationCharacteristic: BluetoothGattCharacteristic? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null

    override fun initialize() {
        // Configurar callback de notificaciones
        setNotificationCallback(notificationCharacteristic)
            .with { _, data ->
                Log.d("MyBleManager", "Notificación recibida: ${String(data.value ?: byteArrayOf())}")
            }
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {

        gatt.services.forEach { service ->
            val characteristics = service.characteristics
            characteristics.forEach { characteristic ->
                Log.d("MyBleManager", "Characteristic UUID: ${characteristic.uuid}")

                if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                    notificationCharacteristic = characteristic
                }

                if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                    writeCharacteristic = characteristic
                }
            }
        }

        return notificationCharacteristic != null
    }

    override fun onServicesInvalidated() {
        notificationCharacteristic = null
    }

    fun enableNotifications(onNotificationReceived: (Data) -> Unit) {
        if (notificationCharacteristic == null) {
            Log.e("MyBleManager", "La característica para notificaciones no está disponible.")
            return
        }

        // Configurar callback y habilitar notificaciones
        setNotificationCallback(notificationCharacteristic)
            .with { _, data ->
                onNotificationReceived(data)
            }

        enableNotifications(notificationCharacteristic)
            .fail { _, status ->
                Log.e("MyBleManager", "Error al habilitar notificaciones: $status")
            }
            .enqueue()
    }

    fun disableNotifications() {
        if (notificationCharacteristic == null) {
            Log.e("MyBleManager", "La característica para notificaciones no está disponible.")
            return
        }

        // Deshabilitar notificaciones
        disableNotifications(notificationCharacteristic)
            .fail { _, status ->
                Log.e("MyBleManager", "Error al deshabilitar notificaciones: $status")
            }
            .enqueue()
    }
    fun writeToCharacteristic(data: ByteArray, onSuccess: () -> Unit = {}, onError: (Int) -> Unit = {}) {
        if (writeCharacteristic == null) {
            Log.e("MyBleManager", "La característica para escritura no está disponible.")
            return
        }

        var hola ="hola"
        // Enviar datos a la característica
        writeCharacteristic(writeCharacteristic, hola.toByteArray())
            .done {
                Log.d("MyBleManager", "Escritura exitosa.")
                onSuccess()
            }
            .fail { _, status ->
                Log.e("MyBleManager", "Error al escribir en la característica: $status")
                onError(status)
            }
            .enqueue()
    }

}
