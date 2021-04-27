package com.example.sugaiot

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import com.example.sugaiot.databinding.ActivityMainBinding
import com.example.sugaiot.ui.recyclerview.bluetoothdevicesdisplay.BluetoothDevicesRecyclerViewAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* todo,
Display discovered devices on recycler view

*/
class MainActivity : AppCompatActivity() {
    private val bluetoothManager: BluetoothManager by lazy {
        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }

    private val bluetoothLowEnergyScanner: BluetoothLeScanner? by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }

    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var bluetoothDevicesRecyclerViewAdapter: BluetoothDevicesRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bluetoothDevicesRecyclerViewAdapter = BluetoothDevicesRecyclerViewAdapter(
            BluetoothDevicesRecyclerViewAdapter.BluetoothDeviceOnConnectClickListener {
                Toast.makeText(this, "Selected ${it.name}", Toast.LENGTH_SHORT).show()
            })
        activityMainBinding.apply {
            startSearchButtonLabel = getString(R.string.start_search_label)
            startSearchButton.setOnClickListener {
                scanForDevices()
            }
            discoveredPeersRecyclerView.adapter = bluetoothDevicesRecyclerViewAdapter
        }

    }

    private fun observeMainActivityViewModelLiveData() {
        mainActivityViewModel.bluetoothLeScanResult.observe(this) {
            it?.let {
                bluetoothDevicesRecyclerViewAdapter.submitList(it.map { scanResult ->
                    scanResult.device
                })
            }
        }

        mainActivityViewModel.isScanning.observe(this) {
            it?.let {
                activityMainBinding.startSearchButtonLabel = if (it) {
                    getString(R.string.stop_search_label)
                } else {
                    getString(R.string.start_search_label)
                }
            }
        }
    }

    private fun scanForDevices(): Boolean {
        if (bluetoothAdapter == null) return false
        if (!mainActivityViewModel.isScanning.value!!) {
            mainActivityViewModel.scanStateUpdated()
            if (bluetoothAdapter?.isEnabled == false) switchBluetooth()
            // todo, Improve the scan process to look for only devices with the glucose service uuid
            CoroutineScope(Dispatchers.Main).launch {
                bluetoothLowEnergyScanner?.startScan(bluetoothLeScanCallback)
                delay(60000) // stop scan after 1 minute
                bluetoothLowEnergyScanner?.stopScan(bluetoothLeScanCallback)
                mainActivityViewModel.scanStateUpdated()
            }
        }
        return true
    }

    private val bluetoothLeScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                mainActivityViewModel.addBluetoothLeScanResult(scanResult = result)
            }
        }
    }

    private fun switchBluetooth() {
        bluetoothAdapter?.let {
            if (it.isEnabled) {
                it.disable()
            } else {
                it.enable()
            }
        }
    }
}