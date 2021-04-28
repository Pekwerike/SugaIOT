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
                        var offset: Int = 0
                        val flag: Int =
                            characteristic.getIntValue(
                                BluetoothGattCharacteristic.FORMAT_UINT8,
                                offset
                            )

                        offset += 1 // offset is 1

                        val sequenceNumber: Int =
                            characteristic.getIntValue(
                                BluetoothGattCharacteristic.FORMAT_UINT16,
                                offset
                            )
                        offset += 1 // offset is 2

                        val baseTimeYear: Int =
                            characteristic.getIntValue(
                                BluetoothGattCharacteristic.FORMAT_UINT16,
                                offset
                            )
                        offset += 2 // offset is 4

                        val baseTimeMonth: Int =
                            characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 4)
                        val baseTimeDay: Int =
                            characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 5)
                        val baseTimeHours: Int =
                            characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 6)
                        val baseTimeMinutes: Int =
                            characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 7)
                        val baseTimeSeconds: Int =
                            characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 8)
                        offset += 5 // offset is 9

                        val timeOffSet: Int = if (flag and (1 shl 0) > 0) {
                            offset += 2 // offset is 11
                            characteristic.getIntValue(
                                BluetoothGattCharacteristic.FORMAT_UINT16,
                                9
                            )
                        } else {
                            0
                        }

                        val glucoseConcentrationUnit: String
                        if (flag and (1 shl 1) > 0) { // glucose concentration field exists
                            val glucoseConcentration = if (flag and (1 shl 2) > 0) {
                                // glucose concentration unit of measurement is mol/L
                                characteristic.getFloatValue(
                                    BluetoothGattCharacteristic.FORMAT_SFLOAT,
                                    offset
                                )
                                glucoseConcentrationUnit = "mol/L"
                            } else {
                                // glucose concentration unit of measurement is kg/L
                                characteristic.getFloatValue(
                                    BluetoothGattCharacteristic.FORMAT_SFLOAT,
                                    offset
                                )
                                glucoseConcentrationUnit = "kg/L"
                            }
                            offset += 2 // offset is 13
                            val typeAndSampleLocation = characteristic.getIntValue(
                                BluetoothGattCharacteristic.FORMAT_UINT8,
                                offset
                            )
                            val type = typeAndSampleLocation shr 4
                            val sampleLocation = typeAndSampleLocation and 15
                            offset += 1 // offset is 14
                        }

                    }
                }
            }
            /* TODO, 1. Check if the characteristics with new update is the Glucose measurement characteristic
                 2. If it is glucose measurement characteristics, read the new value, and jump over to onCharacteristicRead
                   */
        }
    }
}