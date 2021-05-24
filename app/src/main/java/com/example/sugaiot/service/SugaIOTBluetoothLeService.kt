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

    private lateinit var bluetoothGatt: BluetoothGatt


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

    fun connectToBluetoothLeDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(
            this@SugaIOTBluetoothLeService,
            false,
            sugaIOTGlucoseProfileManager
        )
    }
}