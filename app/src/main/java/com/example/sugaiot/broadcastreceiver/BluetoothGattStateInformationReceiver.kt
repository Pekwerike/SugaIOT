package com.example.sugaiot.broadcastreceiver

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.sugaiot.model.GlucoseMeasurementRecord

/*
BluetoothGattStateInformationReceiver collects and distribute important information about the state
of the current Gatt connection.
More specifically this class receives updates from the SugaIOTBluetoothLeService and broadcasts
information to any other android component that registers for the broadcast
*/

class BluetoothGattStateInformationReceiver(private val bluetoothGattStateInformationCallback: BluetoothGattStateInformationCallback) :
    BroadcastReceiver() {

    interface BluetoothGattStateInformationCallback {
        fun glucoseMeasurementRecordAvailable(glucoseMeasurementRecord: GlucoseMeasurementRecord)
        fun connectedToAGattServer(connectedDevice: BluetoothDevice)
        fun disconnectedFromAGattServer()
        fun bondStateExtra(boundState: Int)
        fun recordsSentComplete()
    }

    companion object {
        const val BLUETOOTH_LE_GATT_ACTION_CONNECTED_TO_DEVICE =
            "com.pekwerike.sugaiot.bluetoothLeGattCallbackConnectedToDevice"
        const val BLUETOOTH_LE_GATT_ACTION_DISCONNECTED_FROM_DEVICE =
            "com.pekwerike.sugaiot.bluetoothLeGattCallbackDisconnectedFromDevice"
        const val BLUETOOTH_LE_GATT_ACTION_GLUCOSE_MEASUREMENT_RECORD_AVAILABLE =
            "com.pekwerike.sugaiot.bluetoothLeGattCallbackNewGlucoseRecordAvailable"
        const val BLUETOOTH_LE_GATT_GLUCOSE_MEASUREMENT_RECORD_EXTRA =
            "com.pekwerike.sugaiot.bluetoothLeGattGlucoseMeasurementRecordExtra"
        const val DEVICE_CONNECTED_TO_EXTRA =
            "com.pekwerike.sugaiot.bluetoothLeGattGlucoseMeasurementDeviceConnectedToExtra"
        const val RECORDS_SENT_COMPLETE =
            "com.pekwerike.sugaiot.bluetoothLeGattGlucoseMeasurementRecordSentComplete"

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            when (intent.action) {
                BLUETOOTH_LE_GATT_ACTION_CONNECTED_TO_DEVICE -> {
                    val connectedDevice =
                        intent.getParcelableExtra<BluetoothDevice>(DEVICE_CONNECTED_TO_EXTRA)!!
                    bluetoothGattStateInformationCallback.connectedToAGattServer(connectedDevice = connectedDevice)
                }
                BLUETOOTH_LE_GATT_ACTION_DISCONNECTED_FROM_DEVICE -> {
                    bluetoothGattStateInformationCallback.disconnectedFromAGattServer()
                }
                BLUETOOTH_LE_GATT_ACTION_GLUCOSE_MEASUREMENT_RECORD_AVAILABLE -> {
                    synchronized(this) {
                        intent.getParcelableExtra<GlucoseMeasurementRecord>(
                            BLUETOOTH_LE_GATT_GLUCOSE_MEASUREMENT_RECORD_EXTRA
                        )?.let { glucoseMeasurementRecord ->
                            // send the new glucoseMeasurementRecord to the main activity to display it to the user
                            bluetoothGattStateInformationCallback.glucoseMeasurementRecordAvailable(
                                glucoseMeasurementRecord
                            )
                        }
                    }
                }
                RECORDS_SENT_COMPLETE -> {
                    bluetoothGattStateInformationCallback.recordsSentComplete()
                }
                else -> {

                }
            }
        }
    }

}