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
                    bluetoothGatt.getService(
                        GlucoseProfileConfiguration.GLUCOSE_SERVICE_UUID
                    )

                // set characteristic notification for the glucose measurement characteristic
                glucoseService.getCharacteristic(
                    GlucoseProfileConfiguration.GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID
                )?.let {
                    bluetoothGatt.setCharacteristicNotification(
                        it,
                        true
                    )
                    val glucoseMeasurementCharacteristicConfigDesc =
                        it.getDescriptor(
                            GlucoseProfileConfiguration.CLIENT_CHARACTERISTICS_CONFIGURATION_DESCRIPTOR
                        ).apply {
                            value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        }
                    bluetoothGatt.writeDescriptor(glucoseMeasurementCharacteristicConfigDesc)
                }

                // set characteristic notification for the glucose measurement context characteristic
                glucoseService.getCharacteristic(
                    GlucoseProfileConfiguration.GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID
                )?.let {
                    bluetoothGatt.setCharacteristicNotification(it, true)
                    val glucoseMeasurementContextCharacteristicConfigDesc =
                        it.getDescriptor(GlucoseProfileConfiguration.CLIENT_CHARACTERISTICS_CONFIGURATION_DESCRIPTOR)
                            .apply {
                                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            }
                    bluetoothGatt.writeDescriptor(glucoseMeasurementContextCharacteristicConfigDesc)
                }

                // set characteristic indication for the record access control point characteristic
                glucoseService.getCharacteristic(
                    GlucoseProfileConfiguration.RECORD_ACCESS_CONTROL_POINT_CHARACTERISTIC_UUID
                )?.let {
                    bluetoothGatt.setCharacteristicNotification(it, true)
                    it.getDescriptor(
                        GlucoseProfileConfiguration.CLIENT_CHARACTERISTICS_CONFIGURATION_DESCRIPTOR
                    ).apply {
                        value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                    }

                    // write to the RACP to send all reports on patient glucose level
                    // Opcode byte, Operand byte
                    // 0x01 as Opcode means report all record
                    // 0x01 as Operand means All record
                    it.value = ByteArray(2)
                    it.setValue(
                        GlucoseProfileConfiguration.OP_CODE_REPORT_STORED_RECORDS,
                        BluetoothGattCharacteristic.FORMAT_UINT8,
                        0
                    )
                    it.setValue(
                        GlucoseProfileConfiguration.OPERATOR_ALL_RECORDS,
                        BluetoothGattCharacteristic.FORMAT_UINT8,
                        1
                    )
                    bluetoothGatt.writeCharacteristic(it)
                }


            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            characteristic?.let {
                val characteristicUUID = characteristic.uuid
                when (characteristicUUID) {
                    GlucoseProfileConfiguration.GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID -> {
                        val flagField = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)

                        val sequenceNumberField = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1)

                    }
                }
            }
            /* TODO, 1. Check if the characteristics with new update is the Glucose measurement characteristic
                 2. If it is glucose measurement characteristics, read the new value, and jump over to onCharacteristicRead
                   */
        }
    }
}