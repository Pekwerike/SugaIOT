package com.example.sugaiot.ui.recyclerview.bluetoothdevicesdisplay

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter

class BluetoothDevicesRecyclerViewAdapter(
    private val bluetoothDeviceOnConnectClickListener: BluetoothDeviceOnConnectClickListener
) :
    ListAdapter<ScanResult, BluetoothDeviceLayoutItemViewHolder>(
        BluetoothDevicesRecyclerViewAdapterDiffUtil
    ) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BluetoothDeviceLayoutItemViewHolder {
        return BluetoothDeviceLayoutItemViewHolder.createViewHolder(parent)
    }

    override fun onBindViewHolder(holder: BluetoothDeviceLayoutItemViewHolder, position: Int) {
        holder.bindDeviceData(
            bluetoothScanResult = getItem(position),
            bluetoothDeviceOnConnectClickListener = bluetoothDeviceOnConnectClickListener
        )
    }

    class BluetoothDeviceOnConnectClickListener(
        private val onConnectClickListener:
            (BluetoothDevice) -> Unit
    ) {
        fun onConnectClicked(device: BluetoothDevice) = onConnectClickListener(device)
    }
}