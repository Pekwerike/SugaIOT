package com.example.sugaiot

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    private var isScanning: Boolean = false
    private val bluetoothLeScanResult: MutableList<ScanResult> = mutableListOf()
    private val mainActivityViewModel : MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    private fun scanForDevices(): Boolean {
        if (bluetoothAdapter == null) return false
        if (!isScanning) {
            isScanning = true
            if (bluetoothAdapter?.isEnabled == false) switchBluetooth()
            // todo, Improve the scan process to look for only devices with the glucose service uuid
            CoroutineScope(Dispatchers.Main).launch {
                bluetoothLowEnergyScanner?.startScan(bluetoothLeScanCallback)
                delay(60000) // stop scan after 1 minute
                bluetoothLowEnergyScanner?.stopScan(bluetoothLeScanCallback)
            }
        }
        return true
    }

    private val bluetoothLeScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                bluetoothLeScanResult.add(result)
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