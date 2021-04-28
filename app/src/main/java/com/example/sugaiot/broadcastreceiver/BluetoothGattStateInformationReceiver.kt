package com.example.sugaiot.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

/*
BluetoothGattStateInformationReceiver collects and distribute important information about the state
of the current Gatt connection.
More specifically this class receives updates from the SugaIOTBluetoothLeService and broadcasts
information to any other android component that registers for the broadcast
*/

class BluetoothGattStateInformationReceiver : BroadcastReceiver() {
    companion object {
        const val BLUETOOTH_LE_GATT_CALLBACK_ACTION =
            "com.pekwerike.sugaiot.bluetoothLeGattCallbackAction"
        const val BLUETOOTH_LE_GATT_CONNECTION_CHANGED =
            "com.pekwerike.sugaiot.bluetoothLeGattCallbackConnectionChanged"
    }

    override fun onReceive(context: Context?, intent: Intent?) {

    }

}