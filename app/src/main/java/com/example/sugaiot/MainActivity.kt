package com.example.sugaiot

import android.app.Activity
import android.app.Instrumentation
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.*
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelUuid
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.sugaiot.broadcastreceiver.BluetoothGattStateInformationReceiver
import com.example.sugaiot.databinding.ActivityMainBinding
import com.example.sugaiot.model.GlucoseMeasurementRecord
import com.example.sugaiot.notification.NotificationUtils
import com.example.sugaiot.service.SugaIOTBluetoothLeService
import com.example.sugaiot.ui.recyclerview.bluetoothdevicesdisplay.BluetoothDevicesRecyclerViewAdapter
import com.example.sugaiot.viewmodel.MainActivityViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
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
const val ENABLE_BLUETOOTH_REQUEST_CODE = 1024
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

    @Inject
    lateinit var notificationUtils: NotificationUtils

    private val turnOnBluetoothResultLancher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        if(result.resultCode == Activity.RESULT_OK){
            scanForLeDevices()
        }
    }
    private val bluetoothGattStateInformationReceiver = BluetoothGattStateInformationReceiver(
        bluetoothGattStateInformationCallback =
        object : BluetoothGattStateInformationReceiver.BluetoothGattStateInformationCallback {
            override fun glucoseMeasurementRecordAvailable(glucoseMeasurementRecord: GlucoseMeasurementRecord) {

            }

            override fun connectedToAGattServer(connectedDevice: BluetoothDevice) {
                displayToast("Connected to a Gatt Server")
            }

            override fun disconnectedFromAGattServer() {
                displayToast("Disconnected from a Gatt Server")
            }
        }
    )

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
        bluetoothDevicesRecyclerViewAdapter = BluetoothDevicesRecyclerViewAdapter(
            BluetoothDevicesRecyclerViewAdapter.BluetoothDeviceOnConnectClickListener {
                connectToBluetoothLeDevice(it)
            })
        registerBluetoothGattStateInformationReceiver()
        activityMainBinding.apply {
            startSearchButton.setOnClickListener {
                if (mainActivityViewModel.isScanning.value!!) {
                    stopScanForLeDevices()
                } else {
                    turnOnDeviceLocation()
                }
            }
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
        IntentFilter().apply {
            addAction(BluetoothGattStateInformationReceiver.BLUETOOTH_LE_GATT_ACTION_CONNECTED_TO_DEVICE)
            addAction(BluetoothGattStateInformationReceiver.BLUETOOTH_LE_GATT_ACTION_DISCONNECTED_FROM_DEVICE)
            addAction(BluetoothGattStateInformationReceiver.BLUETOOTH_LE_GATT_ACTION_GLUCOSE_MEASUREMENT_RECORD_AVAILABLE)
            addAction(BluetoothGattStateInformationReceiver.BLUETOOTH_LE_GATT_GLUCOSE_MEASUREMENT_RECORD_EXTRA)
            registerReceiver(bluetoothGattStateInformationReceiver, this)
        }
    }

    private fun unregisterBluetoothGattStateInformationReceiver() {
        unregisterReceiver(bluetoothGattStateInformationReceiver)
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

        val locationRequestTwo = LocationRequest.create().apply{
            interval = 6000
            fastestInterval = 4000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val locationRequest = LocationRequest.create().apply{
            interval = 3000
            fastestInterval = 1500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .addLocationRequest(locationRequestTwo)

        val client : SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {locationSettingsResponse ->
            scanForLeDevices()
        }
        task.addOnFailureListener{ exception ->
            if(exception is ResolvableApiException){
                try{
                    exception.startResolutionForResult(this@MainActivity, CHECK_LOCATION_SETTINGS)
                }catch(sendEx: IntentSender.SendIntentException){
                    // Ignore the error
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            CHECK_LOCATION_SETTINGS -> {
                if(resultCode == Activity.RESULT_OK){
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
                    mutableListOf<ScanFilter>(),
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
                    turnOnBluetoothResultLancher.launch(this)
                }
            }
        }
    }

    override fun onDestroy() {
        // unbind from SugaIOTBluetoothLeService when onDestroy is called
        sugaIOTBluetoothLeService?.unbindService(serviceConnection)
        unregisterBluetoothGattStateInformationReceiver()
        super.onDestroy()
    }
}