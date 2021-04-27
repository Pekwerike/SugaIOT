package com.example.sugaiot.ui.recyclerview.bluetoothdevicesdisplay

import android.bluetooth.BluetoothDevice
import androidx.recyclerview.widget.DiffUtil

object BluetoothDevicesRecyclerViewAdapterDiffUtil : DiffUtil.ItemCallback<BluetoothDevice>() {
    override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
        return oldItem.address == newItem.address
    }

    override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
        return oldItem == newItem
    }

}