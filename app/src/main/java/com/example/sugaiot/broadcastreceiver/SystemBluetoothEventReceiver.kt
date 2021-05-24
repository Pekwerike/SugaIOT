package com.example.sugaiot.broadcastreceiver

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SystemBluetoothEventReceiver : BroadcastReceiver() {

    private var bondStateIndicator: ((Int, BluetoothDevice?) -> Unit)? = null

    fun setBondStateChangedReceiver(
        bondStateChangeReceiver: (Int, BluetoothDevice?) -> Unit
    ) {
        bondStateIndicator = bondStateChangeReceiver
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            when (intent.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val currentBondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 10)
                    if (currentBondState == BluetoothDevice.BOND_BONDED) {
                        val bondedDevice = intent.getParcelableExtra<BluetoothDevice>(
                            BluetoothDevice.EXTRA_DEVICE
                        )
                        bondStateIndicator?.invoke(currentBondState, bondedDevice)
                    } else {
                        bondStateIndicator?.invoke(currentBondState, null)
                    }
                }
                else -> {

                }
            }
        }
    }
}