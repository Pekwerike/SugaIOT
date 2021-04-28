package com.example.sugaiot.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/*
BluetoothGattStateInformationReceiver collects and distribute important information about the state
of the current Gatt connection.
More specifically this class receives updates from the SugaIOTBluetoothLeService and broadcasts
information to any other android component that registers for the broadcast
*/
class BluetoothGattStateInformationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

    }

}