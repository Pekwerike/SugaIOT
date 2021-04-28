package com.example.sugaiot.service

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.sugaiot.GlucoseProfileConfiguration
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SugaIOTBluetoothLeService : Service() {
    private val sugaIOTBluetoothLeServiceBinder: IBinder =
        SugaIOTBluetoothLeServiceBinder()

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }

    private lateinit var bluetoothGatt: BluetoothGatt

    override fun onBind(intent: Intent): IBinder {
        return SugaIOTBluetoothLeServiceBinder()
    }

    inner class SugaIOTBluetoothLeServiceBinder : Binder() {
        fun getServiceInstance(): SugaIOTBluetoothLeService {
            return this@SugaIOTBluetoothLeService
        }
    }


    fun connectToBluetoothLeDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(this, true, bluetoothGattContext)
    }

    private val bluetoothGattContext = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (status) {
                BluetoothGatt.STATE_CONNECTED -> {
                    // TODO Use local broadcast manager to tell the application that the peripheral has been connected to
                    gatt?.discoverServices()
                }

                BluetoothGatt.STATE_DISCONNECTED -> {
                    // TODO Use local broadcast manager to tell the application that the peripheral has been disconnected from

                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            // get the glucose  service immediately
            gatt?.let {
                val glucoseService: BluetoothGattService =
                    it.getService(UUID.fromString(GlucoseProfileConfiguration.GLUCOSE_SERVICE_UUID))
                val glucoseMeasurementCharacteristics: BluetoothGattCharacteristic =
                    glucoseService.getCharacteristic(
                        UUID.fromString(
                            GlucoseProfileConfiguration.GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID
                        )
                    )
                it.setCharacteristicNotification(glucoseMeasurementCharacteristics, true)

            }
        }
    }
}