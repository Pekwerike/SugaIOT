package com.example.sugaiot.service

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.sugaiot.glucoseprofilemanager.GlucoseProfileConfiguration
import com.example.sugaiot.glucoseprofilemanager.SugaIOTGlucoseProfileManager
import com.example.sugaiot.notification.NotificationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SugaIOTBluetoothLeService : Service() {
    companion object {
        const val DEVICE_TO_CONNECT_EXTRA = "DeviceToConnectToExtra"
    }

    private val sugaIOTBluetoothLeServiceBinder: IBinder =
        SugaIOTBluetoothLeServiceBinder()

    @Inject
    lateinit var sugaIOTGlucoseProfileManager: SugaIOTGlucoseProfileManager

    @Inject
    lateinit var notificationUtils: NotificationUtils
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    private lateinit var bluetoothGatt: BluetoothGatt
    private lateinit var glucoseService: BluetoothGattService
    private lateinit var glucoseMeasurementCharacteristics: BluetoothGattCharacteristic
    private lateinit var racpCharacteristic: BluetoothGattCharacteristic

    override fun onBind(intent: Intent): IBinder {
        return sugaIOTBluetoothLeServiceBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            1010,
            notificationUtils.configureGlucoseSensorCommunicationNotification()
        )
        val device = intent!!.getParcelableExtra<BluetoothDevice>(DEVICE_TO_CONNECT_EXTRA)!!
        connectToBluetoothLeDevice(device)

        return START_NOT_STICKY
    }

    inner class SugaIOTBluetoothLeServiceBinder : Binder() {
        fun getServiceInstance(): SugaIOTBluetoothLeService {
            return this@SugaIOTBluetoothLeService
        }
    }

    private fun connectToBluetoothLeDevice(device: BluetoothDevice) {

        //    device.createBond()
        bluetoothGatt = device.connectGatt(
            this@SugaIOTBluetoothLeService,
            false,
            sugaIOTGlucoseProfileManager
        )
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
                bluetoothGatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            glucoseService =
                bluetoothGatt.getService(GlucoseProfileConfiguration.GLUCOSE_SERVICE_UUID)
            glucoseMeasurementCharacteristics =
                glucoseService.getCharacteristic(GlucoseProfileConfiguration.GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID)
            setCharacteristicClientConfigDescriptor(
                bluetoothGatt,
                glucoseMeasurementCharacteristics, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            )
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (descriptor!!.characteristic.uuid) {
                    GlucoseProfileConfiguration.GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID -> {
                        Log.i("GlucoseResult", "Written to GMC")
                        racpCharacteristic = glucoseService.getCharacteristic(
                            GlucoseProfileConfiguration
                                .RECORD_ACCESS_CONTROL_POINT_CHARACTERISTIC_UUID
                        )
                        setCharacteristicClientConfigDescriptor(
                            bluetoothGatt,
                            racpCharacteristic,
                            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        )
                    }
                    GlucoseProfileConfiguration.RECORD_ACCESS_CONTROL_POINT_CHARACTERISTIC_UUID -> {
                       readAllResults()
                    }
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            Log.i("GlucoseResult", "NotificationReceived")
            var g = 0
            g
        }
    }

    private fun setCharacteristicClientConfigDescriptor(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ): Boolean {
        gatt.setCharacteristicNotification(characteristic, true)
        val clientCharacteristicConfigDes = characteristic.getDescriptor(
            GlucoseProfileConfiguration.CLIENT_CHARACTERISTICS_CONFIGURATION_DESCRIPTOR
        ).apply {
            this.value = value
        }
        return gatt.writeDescriptor(clientCharacteristicConfigDes)
    }


    fun readAllResults() {
        CoroutineScope(Dispatchers.IO).launch {
            racpCharacteristic.value = ByteArray(2)
            racpCharacteristic.setValue(
                GlucoseProfileConfiguration.OP_CODE_REPORT_STORED_RECORDS,
                BluetoothGattCharacteristic.FORMAT_UINT8,
                0
            )
            racpCharacteristic.setValue(
                GlucoseProfileConfiguration.OPERATOR_ALL_RECORDS,
                BluetoothGattCharacteristic.FORMAT_UINT8,
                1
            )

            delay(300)
            bluetoothGatt.writeCharacteristic(racpCharacteristic)
        }
    }

}