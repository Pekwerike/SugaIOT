package com.example.sugaiot.broadcastreceiver

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SystemBluetoothEventReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            when (intent.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {

                }
            }
        }
    }
}