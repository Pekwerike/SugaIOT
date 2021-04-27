package com.example.sugaiot.ui.recyclerview.bluetoothdevicesdisplay

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.sugaiot.R
import com.example.sugaiot.databinding.BluetoothDeviceLayoutItemBinding

class BluetoothDeviceLayoutItemViewHolder(
    private val bluetoothDeviceLayoutItemBinding:
    BluetoothDeviceLayoutItemBinding
) : RecyclerView.ViewHolder(bluetoothDeviceLayoutItemBinding.root) {

    fun bindDeviceData(
        bluetoothDevice: BluetoothDevice,
        bluetoothDeviceOnConnectClickListener:
        BluetoothDevicesRecyclerViewAdapter.BluetoothDeviceOnConnectClickListener
    ) {
        bluetoothDeviceLayoutItemBinding.apply {
            deviceName = bluetoothDevice.name
            connectToBluetoothDeviceButton.setOnClickListener {
                bluetoothDeviceOnConnectClickListener.onConnectClicked(
                    device = bluetoothDevice
                )
            }
        }
    }

    companion object {
        fun createViewHolder(parent: ViewGroup): BluetoothDeviceLayoutItemViewHolder {
            val layoutBinding = DataBindingUtil.inflate<BluetoothDeviceLayoutItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.bluetooth_device_layout_item,
                parent,
                false
            )
            return BluetoothDeviceLayoutItemViewHolder(layoutBinding)
        }
    }
}