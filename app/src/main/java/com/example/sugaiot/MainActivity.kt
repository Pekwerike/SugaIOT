package com.example.sugaiot

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.sugaiot.broadcastreceiver.BluetoothGattStateInformationReceiver
import com.example.sugaiot.broadcastreceiver.SystemBluetoothEventReceiver
import com.example.sugaiot.databinding.ActivityMainBinding
import com.example.sugaiot.glucoseprofilemanager.GlucoseProfileConfiguration
import com.example.sugaiot.model.GlucoseMeasurementRecord
import com.example.sugaiot.notification.NotificationUtils
import com.example.sugaiot.service.SugaIOTBluetoothLeService
import com.example.sugaiot.ui.recyclerview.bluetoothdevicesdisplay.BluetoothDevicesRecyclerViewAdapter
import com.example.sugaiot.ui.recyclerview.glucoserecordresult.GlucoseRecordRecyclerViewData
import com.example.sugaiot.ui.recyclerview.glucoserecordresult.GlucoseRecordsRecyclerViewAdapter
import com.example.sugaiot.ui.recyclerview.glucoserecordresult.months
import com.example.sugaiot.viewmodel.MainActivityViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


const val ACCESS_TO_FINE_LOCATION_PERMISSION_REQUEST_CODE = 1010
const val CHECK_LOCATION_SETTINGS = 2020
const val EXTRA_BLE_DEVICE = "ExtraBleDevice"
const val ENABLE_BLUETOOTH_REQUEST_CODE = 1024

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var localBroadcastManager: LocalBroadcastManager

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }
    private val glucoseRecordsRecyclerViewAdapter = GlucoseRecordsRecyclerViewAdapter()
    private var sugaIOTBluetoothLeService: SugaIOTBluetoothLeService? = null

    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    private lateinit var activityMainBinding: ActivityMainBinding
    private val bluetoothDevicesRecyclerViewAdapter: BluetoothDevicesRecyclerViewAdapter =
        BluetoothDevicesRecyclerViewAdapter(
            BluetoothDevicesRecyclerViewAdapter.BluetoothDeviceOnConnectClickListener {
                connectToBluetoothLeDevice(it)

            })


    @Inject
    lateinit var notificationUtils: NotificationUtils

    private val turnOnBluetoothResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                scanForLeDevices()
            }
        }
    private val bluetoothGattStateInformationReceiver = BluetoothGattStateInformationReceiver(
        bluetoothGattStateInformationCallback =
        object : BluetoothGattStateInformationReceiver.BluetoothGattStateInformationCallback {
            override fun glucoseMeasurementRecordAvailable(glucoseMeasurementRecord: GlucoseMeasurementRecord) {
                mainActivityViewModel.addGlucoseMeasurementRecord(glucoseMeasurementRecord)
            }

            override fun connectedToAGattServer(connectedDevice: BluetoothDevice) {
                displayToast("Connected to a Gatt Server")
            }

            override fun disconnectedFromAGattServer() {
                displayToast("Disconnected from a Gatt Server")
            }

            override fun bondStateExtra(boundState: Int) {
                if (boundState == BluetoothDevice.BOND_BONDED) {
                    displayToast("Bounded")
                }
            }

            override fun recordsSentComplete() {
                Log.i("GlucoseResult", "Records completed")
                mainActivityViewModel.createGlucoseMeasurementRecordsRecyclerviewData()
            }
        }
    )

    private val systemBluetoothEventReceiver = SystemBluetoothEventReceiver()

    // code to manage SugaIOTBluetoothLeService lifecycle
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            sugaIOTBluetoothLeService =
                (service as SugaIOTBluetoothLeService.SugaIOTBluetoothLeServiceBinder)
                    .getServiceInstance()

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            sugaIOTBluetoothLeService = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        activityMainBinding.apply {
            startSearchButton.setOnClickListener {
                if (mainActivityViewModel.isScanning.value!!) {
                    stopScanForLeDevices()
                } else {
                    turnOnDeviceLocation()
                }
            }
            readAllResultsButton.setOnClickListener {
                sugaIOTBluetoothLeService?.readAllResults()
            }
            glucoseRecordsResultRecyclerview.adapter = glucoseRecordsRecyclerViewAdapter
            discoveredPeersRecyclerView.adapter = bluetoothDevicesRecyclerViewAdapter
        }
        // create notification channel
        notificationUtils.createGlucoseSensorCommunicationChannel()
        observeMainActivityViewModelLiveData()
        // bind to SugaIOTBluetoothService when ever on create is called
        Intent(this, SugaIOTBluetoothLeService::class.java).also { intent ->
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }
    }

    private fun registerBluetoothGattStateInformationReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothGattStateInformationReceiver.BLUETOOTH_LE_GATT_ACTION_CONNECTED_TO_DEVICE)
            addAction(BluetoothGattStateInformationReceiver.BLUETOOTH_LE_GATT_ACTION_DISCONNECTED_FROM_DEVICE)
            addAction(BluetoothGattStateInformationReceiver.BLUETOOTH_LE_GATT_ACTION_GLUCOSE_MEASUREMENT_RECORD_AVAILABLE)
            addAction(BluetoothGattStateInformationReceiver.BLUETOOTH_LE_GATT_GLUCOSE_MEASUREMENT_RECORD_EXTRA)
            addAction(BluetoothGattStateInformationReceiver.RECORDS_SENT_COMPLETE)
        }
        localBroadcastManager
            .registerReceiver(bluetoothGattStateInformationReceiver, intentFilter)
    }

    private fun connectToBluetoothLeDevice(device: BluetoothDevice) {
        // onConnect to device, put the SugaIOTBluetoothLeService in the foreground
        Intent(this, SugaIOTBluetoothLeService::class.java).also { intent ->
            intent.putExtra(SugaIOTBluetoothLeService.DEVICE_TO_CONNECT_EXTRA, device)
            startService(intent)
        }

    }

    private fun stopScanForLeDevices() {
        if (!mainActivityViewModel.isScanning.value!!) return
        bluetoothLeScanner?.stopScan(bluetoothLeScanCallback)
        mainActivityViewModel.scanStateUpdated()
    }

    private fun turnOnDeviceLocation() {

        val locationRequestTwo = LocationRequest.create().apply {
            interval = 6000
            fastestInterval = 4000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 3000
            fastestInterval = 1500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .addLocationRequest(locationRequestTwo)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            scanForLeDevices()
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this@MainActivity, CHECK_LOCATION_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CHECK_LOCATION_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    scanForLeDevices()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun scanForLeDevices() {
        if (bluetoothAdapter == null) return

        if (!hasAccessToDeviceFineLocation()) {
            requestAccessToDeviceFineLocation()
            return
        }
        // ensure that the user device location is turned on

        if (!mainActivityViewModel.isScanning.value!!) {
            if (bluetoothAdapter?.isEnabled == false) {
                switchBluetooth()
                return
            }
            mainActivityViewModel.scanStateUpdated()
            // todo, Improve the scan process to look for only devices with the glucose service uuid
            CoroutineScope(Dispatchers.Main).launch {
                bluetoothLeScanner?.startScan(
                    mutableListOf<ScanFilter>(
                        ScanFilter.Builder().setServiceUuid(
                            ParcelUuid.fromString(GlucoseProfileConfiguration.GLUCOSE_SERVICE_UUID.toString())
                        ).build()
                    ),
                    ScanSettings.Builder().setScanMode(
                        ScanSettings.SCAN_MODE_LOW_LATENCY
                    ).build(), bluetoothLeScanCallback
                )
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
        mainActivityViewModel.bluetoothLeScanResult.observe(this) {
            it?.let {
                bluetoothDevicesRecyclerViewAdapter.submitList(
                    it
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
        mainActivityViewModel.glucoseRecordRecyclerViewDataList.observe(this) {
            it?.let {
                glucoseRecordsRecyclerViewAdapter.submitList(it)
                if (it.isNotEmpty()) {
                    activityMainBinding.apply {
                        glucoseRecordsResultRecyclerview.animate().alpha(1f)
                        discoveredPeersRecyclerView.animate().alpha(0f)
                        startSearchButton.animate().alpha(0f)
                    }
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
                turnOnDeviceLocation()
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

                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                    turnOnBluetoothResultLauncher.launch(this)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(
            systemBluetoothEventReceiver,
            IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        )
        registerBluetoothGattStateInformationReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(
            systemBluetoothEventReceiver
        )
        localBroadcastManager.unregisterReceiver(bluetoothGattStateInformationReceiver)
    }

    override fun onDestroy() {
        // unbind from SugaIOTBluetoothLeService when onDestroy is called
        sugaIOTBluetoothLeService?.unbindService(serviceConnection)
        super.onDestroy()
    }
}
