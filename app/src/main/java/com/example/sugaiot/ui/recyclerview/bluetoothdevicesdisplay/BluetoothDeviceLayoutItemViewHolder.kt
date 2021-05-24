package com.example.sugaiot.ui.recyclerview.bluetoothdevicesdisplay

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
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
        bluetoothScanResult : ScanResult,
        bluetoothDeviceOnConnectClickListener:
        BluetoothDevicesRecyclerViewAdapter.BluetoothDeviceOnConnectClickListener
    ) {
        bluetoothDeviceLayoutItemBinding.apply {
            deviceName = bluetoothScanResult.device.name ?: "Unknown device"
            deviceAddress = bluetoothScanResult.device.address ?: "No address"
            canConnect = if(if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    bluetoothScanResult.isConnectable
                } else {
                    true
                }
            ) "Can connect" else "Cannot connect"

            if(bluetoothScanResult.timestampNanos == 0L){
                connectToBluetoothDeviceButton.text = root.context.getString(R.string.get_results_label)
            }
            connectToBluetoothDeviceButton.setOnClickListener {
                bluetoothDeviceOnConnectClickListener.onConnectClicked(
                    device = bluetoothScanResult.device
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