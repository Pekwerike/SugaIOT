package com.example.sugaiot.ui.recyclerview.bluetoothdevicesdisplay

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.recyclerview.widget.DiffUtil

object BluetoothDevicesRecyclerViewAdapterDiffUtil : DiffUtil.ItemCallback<ScanResult>() {
    override fun areItemsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
        return oldItem.device.address == newItem.device.address
    }

    override fun areContentsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
        return oldItem == newItem
    }

}