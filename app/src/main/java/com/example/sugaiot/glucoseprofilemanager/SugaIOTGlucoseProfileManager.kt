package com.example.sugaiot.glucoseprofilemanager

import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.sugaiot.broadcastreceiver.BluetoothGattStateInformationReceiver
import com.example.sugaiot.model.GlucoseMeasurementRecord
import com.example.sugaiot.model.SensorStatusAnnunciation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SugaIOTGlucoseProfileManager @Inject constructor(
    private val localBroadcastManager: LocalBroadcastManager
) : BluetoothGattCallback() {
    private val bluetoothGattStateIntent = Intent()
    private var glucoseService: BluetoothGattService? = null

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> {
                    // TODO Use local broadcast manager to tell the application that the peripheral has been connected to
                    gatt?.let {
                        gatt.discoverServices()
                    }
                }

                BluetoothGatt.STATE_DISCONNECTED -> {
                    bluetoothGattStateIntent.apply {
                        action =
                            BluetoothGattStateInformationReceiver.BLUETOOTH_LE_GATT_ACTION_DISCONNECTED_FROM_DEVICE
                        localBroadcastManager.sendBroadcast(this)
                    }
                }
            }
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        // get the glucose  service
        gatt?.let {

            glucoseService =
                gatt.getService(
                    GlucoseProfileConfiguration.GLUCOSE_SERVICE_UUID
                )

            // set characteristic notification for the glucose measurement characteristic
            glucoseService?.getCharacteristic(
                GlucoseProfileConfiguration.GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID
            )?.let {
                setCharacteristicClientConfigDescriptor(
                    gatt,
                    it, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                )
            }
        }
    }


    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
            descriptor?.let {
                when (it.characteristic.uuid) {
                    GlucoseProfileConfiguration.GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID -> {
                        // set characteristic notification on glucose measurement context if it is support by the server
                        val glucoseMeasurementContextCharacteristic = glucoseService!!
                            .getCharacteristic(GlucoseProfileConfiguration.GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID)

                        if (glucoseMeasurementContextCharacteristic != null) {
                            setCharacteristicClientConfigDescriptor(
                                gatt,
                                glucoseMeasurementContextCharacteristic,
                                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            )
                        } else {
                            val recordControlAccessPointCharacteristic = glucoseService!!
                                .getCharacteristic(GlucoseProfileConfiguration.RECORD_ACCESS_CONTROL_POINT_CHARACTERISTIC_UUID)
                            setCharacteristicClientConfigDescriptor(
                                gatt,
                                recordControlAccessPointCharacteristic,
                                BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                            )
                        }
                    }
                    GlucoseProfileConfiguration.GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID -> {
                        // set characteristic indication on glucose record access control point characteristic
                        glucoseService!!
                            .getCharacteristic(GlucoseProfileConfiguration.RECORD_ACCESS_CONTROL_POINT_CHARACTERISTIC_UUID)!!
                            .let { recordAccessControlPointCharacteristic ->
                                setCharacteristicClientConfigDescriptor(
                                    gatt,
                                    recordAccessControlPointCharacteristic,
                                    BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                                )
                            }
                    }

                    GlucoseProfileConfiguration.RECORD_ACCESS_CONTROL_POINT_CHARACTERISTIC_UUID -> {
                        // done, indication set for record access control point characteristic
                        // go ahead and begin receiving notification
                        // write to the RACP to send all reports on patient glucose level
                        CoroutineScope(Dispatchers.IO).launch {
                            val recordAccessControlPointCharacteristic =
                                gatt.getService(GlucoseProfileConfiguration.GLUCOSE_SERVICE_UUID)
                                    .getCharacteristic(GlucoseProfileConfiguration.RECORD_ACCESS_CONTROL_POINT_CHARACTERISTIC_UUID)
                            recordAccessControlPointCharacteristic!!.let { racp ->
                                racp.value = ByteArray(2)
                                racp.setValue(
                                    GlucoseProfileConfiguration.OP_CODE_REPORT_STORED_RECORDS,
                                    BluetoothGattCharacteristic.FORMAT_UINT8,
                                    0
                                )
                                racp.setValue(
                                    GlucoseProfileConfiguration.OPERATOR_ALL_RECORDS,
                                    BluetoothGattCharacteristic.FORMAT_UINT8,
                                    1
                                )

                                delay(400)
                                gatt.writeCharacteristic(racp)
                            }
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }


    @Synchronized
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {

        characteristic!!.let {
            when (characteristic.uuid) {
                GlucoseProfileConfiguration.GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID -> {
                    val glucoseMeasurementRecord = GlucoseMeasurementRecord()
                    var offset = 0
                    val flag: Int =
                        characteristic.getIntValue(
                            BluetoothGattCharacteristic.FORMAT_UINT8,
                            offset
                        )

                    offset += 1 // offset is 1

                    glucoseMeasurementRecord.sequenceNumber =
                        characteristic.getIntValue(
                            BluetoothGattCharacteristic.FORMAT_UINT16,
                            offset
                        )
                    offset += 2 // offset is 3

                    val baseTimeYear: Int =
                        characteristic.getIntValue(
                            BluetoothGattCharacteristic.FORMAT_UINT16,
                            offset
                        )
                    offset += 2 // offset is 5

                    val baseTimeMonth: Int =
                        characteristic.getIntValue(
                            BluetoothGattCharacteristic.FORMAT_UINT8,
                            offset++
                        ) // offset is 5 before use, and 6 after use
                    val baseTimeDay: Int =
                        characteristic.getIntValue(
                            BluetoothGattCharacteristic.FORMAT_UINT8,
                            offset++
                        ) // offset is 6 before use, and 7 after use
                    val baseTimeHours: Int =
                        characteristic.getIntValue(
                            BluetoothGattCharacteristic.FORMAT_UINT8,
                            offset++
                        ) // offset is 7 before use, and 8 after use
                    val baseTimeMinutes: Int =
                        characteristic.getIntValue(
                            BluetoothGattCharacteristic.FORMAT_UINT8,
                            offset++
                        ) // offset is 8 before use, and 9 after use
                    val baseTimeSeconds: Int =
                        characteristic.getIntValue(
                            BluetoothGattCharacteristic.FORMAT_UINT8,
                            offset++
                        ) // offset is 9 before use, and 10 after use

                    glucoseMeasurementRecord.calendar = GregorianCalendar(
                        baseTimeYear,
                        baseTimeMonth,
                        baseTimeDay,
                        baseTimeHours,
                        baseTimeMinutes,
                        baseTimeSeconds
                    )

                    val timeOffset: Int = if (flag and (1 shl 0) > 0) {
                        offset += 2 // offset is 12
                        characteristic.getIntValue(
                            BluetoothGattCharacteristic.FORMAT_UINT16,
                            10
                        )
                    } else {
                        0
                    }
                    glucoseMeasurementRecord.timeOffset = timeOffset

                    if (flag and (1 shl 1) > 0) { // glucose concentration field exists
                        glucoseMeasurementRecord.glucoseConcentrationValue =
                            if (flag and (1 shl 2) > 0) {
                                // glucose concentration unit of measurement is mol/L
                                glucoseMeasurementRecord.glucoseConcentrationMeasurementUnit =
                                    GlucoseMeasurementRecord.GlucoseConcentrationMeasurementUnit.MOLES_PER_LITRE
                                characteristic.getFloatValue(
                                    BluetoothGattCharacteristic.FORMAT_SFLOAT,
                                    offset
                                )
                            } else {
                                // glucose concentration unit of measurement is kg/L
                                glucoseMeasurementRecord.glucoseConcentrationMeasurementUnit =
                                    GlucoseMeasurementRecord.GlucoseConcentrationMeasurementUnit.KILOGRAM_PER_LITRE
                                characteristic.getFloatValue(
                                    BluetoothGattCharacteristic.FORMAT_SFLOAT,
                                    offset
                                )
                            }

                        offset += 2 // offset is 14
                        val typeAndSampleLocation = characteristic.getIntValue(
                            BluetoothGattCharacteristic.FORMAT_UINT8,
                            offset
                        )
                        glucoseMeasurementRecord.type = typeAndSampleLocation shr 4
                        glucoseMeasurementRecord.sampleLocationInteger =
                            typeAndSampleLocation and 0x0F
                        offset += 1 // offset is 15
                    }
                    if (flag and (1 shl 2) > 0) { // Sensor Status Annunciation field is present
                        val sensorStatusAnnunciationValue = characteristic.getIntValue(
                            BluetoothGattCharacteristic.FORMAT_UINT16,
                            offset
                        )
                        offset += 2 // offset is 16 or 12 or 9
                        val sensorStatusAnnunciation = SensorStatusAnnunciation()
                        sensorStatusAnnunciation.deviceBatteryLowAtTimeOfMeasurement =
                            sensorStatusAnnunciationValue and (1 shl 0) > 0
                        sensorStatusAnnunciation.sensorMalfunctionAtTimeOfMeasurement =
                            sensorStatusAnnunciationValue and (1 shl 1) > 0
                        sensorStatusAnnunciation.bloodSampleInsufficientAtTimeOfMeasurement =
                            sensorStatusAnnunciationValue and (1 shl 2) > 0
                        sensorStatusAnnunciation.stripInsertionError =
                            sensorStatusAnnunciationValue and (1 shl 3) > 0
                        sensorStatusAnnunciation.stripTypeIncorrectForDevice =
                            sensorStatusAnnunciationValue and (1 shl 4) > 0
                        sensorStatusAnnunciation.sensorResultHigherThanDeviceCanProcess =
                            sensorStatusAnnunciationValue and (1 shl 5) > 0
                        sensorStatusAnnunciation.sensorResultLowerThanTheDeviceCanProcess =
                            sensorStatusAnnunciationValue and (1 shl 6) > 0
                        sensorStatusAnnunciation.sensorTemperatureTooHighForValidTestResult =
                            sensorStatusAnnunciationValue and (1 shl 7) > 0
                        sensorStatusAnnunciation.sensorTemperatureTooLowForValidTestResult =
                            sensorStatusAnnunciationValue and (1 shl 8) > 0
                        sensorStatusAnnunciation.sensorReadInterruptedBecauseStripWasPulledTooSoon =
                            sensorStatusAnnunciationValue and (1 shl 9) > 0
                        sensorStatusAnnunciation.generalDeviceFaultHasOccurredInSensor =
                            sensorStatusAnnunciationValue and (1 shl 10) > 0
                        sensorStatusAnnunciation.timeFaultHasOccurredInTheSensor =
                            sensorStatusAnnunciationValue and (1 shl 11) > 0

                        glucoseMeasurementRecord.sensorStatusAnnunciation =
                            sensorStatusAnnunciation

                    }

                    localBroadcastManager.sendBroadcast(bluetoothGattStateIntent.apply {
                        action =
                            BluetoothGattStateInformationReceiver.BLUETOOTH_LE_GATT_ACTION_GLUCOSE_MEASUREMENT_RECORD_AVAILABLE
                        putExtra(
                            BluetoothGattStateInformationReceiver.BLUETOOTH_LE_GATT_GLUCOSE_MEASUREMENT_RECORD_EXTRA,
                            glucoseMeasurementRecord
                        )
                    })
                }

                GlucoseProfileConfiguration.GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID -> {
                    // Todo, get characteristic value of the glucose measurement context characteristic
                }
                GlucoseProfileConfiguration.RECORD_ACCESS_CONTROL_POINT_CHARACTERISTIC_UUID -> {
                    localBroadcastManager.sendBroadcast(
                        bluetoothGattStateIntent.apply {
                            action = BluetoothGattStateInformationReceiver.RECORDS_SENT_COMPLETE
                        }
                    )
                }
                else -> {

                }
            }
        }

    }


    private fun setCharacteristicClientConfigDescriptor(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ): Boolean {
        gatt.setCharacteristicNotification(characteristic, true)
        val clientCharacteristicConfigDes = characteristic.getDescriptor(
            GlucoseProfileConfiguration.CLIENT_CHARACTERISTICS_CONFIGURATION_DESCRIPTOR
        ).apply {
            this.value = value
        }
        return gatt.writeDescriptor(clientCharacteristicConfigDes)
    }
}