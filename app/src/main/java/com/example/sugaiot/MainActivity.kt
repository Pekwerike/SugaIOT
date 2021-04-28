package com.example.sugaiot

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelUuid
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.sugaiot.databinding.ActivityMainBinding
import com.example.sugaiot.service.SugaIOTBluetoothLeService
import com.example.sugaiot.ui.recyclerview.bluetoothdevicesdisplay.BluetoothDevicesRecyclerViewAdapter
import com.example.sugaiot.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


const val ACCESS_TO_FINE_LOCATION_PERMISSION_REQUEST_CODE = 1010

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }

    private var sugaIOTBluetoothLeService: SugaIOTBluetoothLeService? = null

    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var bluetoothDevicesRecyclerViewAdapter: BluetoothDevicesRecyclerViewAdapter

    // code to manager SugaIOTBluetoothLeService lifecycle
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            sugaIOTBluetoothLeService = (service as SugaIOTBluetoothLeService.SugaIOTBluetoothLeServiceBinder)
                .getServiceInstance()

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            sugaIOTBluetoothLeService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bluetoothDevicesRecyclerViewAdapter = BluetoothDevicesRecyclerViewAdapter(
            BluetoothDevicesRecyclerViewAdapter.BluetoothDeviceOnConnectClickListener {
                displayToast(it.address)
            })
        activityMainBinding.apply {
            startSearchButton.setOnClickListener {
                if (mainActivityViewModel.isScanning.value!!) {
                    stopScanForLeDevices()
                } else {
                    scanForLeDevices()
                }
            }
            discoveredPeersRecyclerView.adapter = bluetoothDevicesRecyclerViewAdapter
        }
        observeMainActivityViewModelLiveData()
    }

    private fun connectToBlutoothLeDevice(device: BluetoothDevice) {

    }

    private fun stopScanForLeDevices() {
        if (!mainActivityViewModel.isScanning.value!!) return
        bluetoothLeScanner?.stopScan(bluetoothLeScanCallback)
        mainActivityViewModel.scanStateUpdated()
    }

    private fun scanForLeDevices() {
        if (bluetoothAdapter == null) return
        if (!hasAccessToDeviceFineLocation()) {
            requestAccessToDeviceFineLocation()
            return
        }
        if (!mainActivityViewModel.isScanning.value!!) {
            if (bluetoothAdapter?.isEnabled == false) switchBluetooth()
            mainActivityViewModel.scanStateUpdated()
            // todo, Improve the scan process to look for only devices with the glucose service uuid
            CoroutineScope(Dispatchers.Main).launch {
                bluetoothLeScanner?.startScan(bluetoothLeScanCallback)
                delay(60000) // stop scan after 1 minute
                bluetoothLeScanner?.stopScan(bluetoothLeScanCallback)
                mainActivityViewModel.scanStateUpdated()
            }
        }
        return
    }

    private val bluetoothLeScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                mainActivityViewModel.addBluetoothLeScanResult(scanResult = result)
                AdvertiseData.Builder().addServiceData(
                    ParcelUuid
                        .fromString(
                            UUID.nameUUIDFromBytes(result.scanRecord!!.bytes).toString()
                        ), result.scanRecord?.bytes
                ).setIncludeDeviceName(true).build().serviceData
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.let {
                it.forEach { scanResult ->
                    mainActivityViewModel.addBluetoothLeScanResult(scanResult = scanResult)
                }
            }
        }
    }

    private fun observeMainActivityViewModelLiveData() {
        mainActivityViewModel.bluetoothLeScanResultMap.observe(this) {
            it?.let {
                bluetoothDevicesRecyclerViewAdapter.submitList(
                    it.map { entry ->
                        entry.value.device
                    }.toMutableList()
                )
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

    private fun displayToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun hasAccessToDeviceFineLocation(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAccessToDeviceFineLocation() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            ACCESS_TO_FINE_LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (permissions.contains(android.Manifest.permission.ACCESS_FINE_LOCATION)
            && (requestCode == ACCESS_TO_FINE_LOCATION_PERMISSION_REQUEST_CODE)
        ) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                scanForLeDevices()
            } else {
                // Handle situation when user denies app permission request
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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