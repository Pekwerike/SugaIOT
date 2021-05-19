package com.example.sugaiot.service

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.sugaiot.glucoseprofilemanager.GlucoseProfileConfiguration
import com.example.sugaiot.glucoseprofilemanager.SugaIOTGlucoseProfileManager
import com.example.sugaiot.notification.NotificationUtils
import dagger.hilt.android.AndroidEntryPoint
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

    override fun onBind(intent: Intent): IBinder {
        return sugaIOTBluetoothLeServiceBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            1010,
            notificationUtils.configureGlucoseSensorCommunicationNotification()
        )
        intent?.let {
            val device = intent.getParcelableExtra<BluetoothDevice>(DEVICE_TO_CONNECT_EXTRA)!!
            connectToBluetoothLeDevice(device)
        }
        return START_NOT_STICKY
    }

    inner class SugaIOTBluetoothLeServiceBinder : Binder() {
        fun getServiceInstance(): SugaIOTBluetoothLeService {
            return this@SugaIOTBluetoothLeService
        }
    }

    private fun connectToBluetoothLeDevice(device: BluetoothDevice) {
        //    device.createBond()
        bluetoothGatt = device.connectGatt(this@SugaIOTBluetoothLeService, false, sugaIOTGlucoseProfileManager)
    }

    private val bleCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED
                && gatt != null
            ) {

                bluetoothGatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            val glucoseService = bluetoothGatt.getService(
                GlucoseProfileConfiguration
                    .GLUCOSE_SERVICE_UUID
            )

            val glmCharacteristic = glucoseService.getCharacteristic(
                GlucoseProfileConfiguration
                    .GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID
            )
            bluetoothGatt.setCharacteristicNotification(glmCharacteristic, true)
            val cccd = glmCharacteristic.getDescriptor(
                GlucoseProfileConfiguration
                    .CLIENT_CHARACTERISTICS_CONFIGURATION_DESCRIPTOR
            )
            cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            val rr = bluetoothGatt.writeDescriptor(cccd)
            rr
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            val kels = 30
            kels
        }
    }


}