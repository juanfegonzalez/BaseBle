package com.indusair.basebluetooth.bleapplication


import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import no.nordicsemi.android.ble.data.Data
import android.content.Context
import android.util.Log

import no.nordicsemi.android.ble.BleManager
import java.util.UUID

open class MyBleManager(context: Context) : BleManager(context) {
    private var notificationCharacteristic: BluetoothGattCharacteristic? = null

    override fun initialize() {
        // Inicialización cuando la conexión BLE es establecida
        setNotificationCallback(notificationCharacteristic)
            .with { _, data ->
                Log.d("MyBleManager", "Notificación recibida: ${String(data.value ?: byteArrayOf())}")
            }

    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        // Aquí encuentras y guardas la característica para notificaciones
        val service = gatt.getService(UUID.fromString("")) // Ejemplo de UUID del servicio
        notificationCharacteristic = service?.getCharacteristic(UUID.fromString("")) // Ejemplo de UUID de característica
        return notificationCharacteristic != null
    }

    override fun onServicesInvalidated() {
        notificationCharacteristic = null
    }


    fun enableNotifications(onNotificationReceived: (Data) -> Unit) {
        setNotificationCallback(notificationCharacteristic)
            .with { _, data ->
                onNotificationReceived(data)
            }

        enableNotifications(notificationCharacteristic)
            .enqueue() // Encolar para ejecutar
    }

    fun disableNotification(){
        disableNotifications(notificationCharacteristic)
            .enqueue()
    }
}