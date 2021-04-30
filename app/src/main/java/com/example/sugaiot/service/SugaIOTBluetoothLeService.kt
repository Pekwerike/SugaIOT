package com.example.sugaiot.service

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.sugaiot.glucoseprofilemanager.SugaIOTGlucoseProfileManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SugaIOTBluetoothLeService : Service() {
    private val sugaIOTBluetoothLeServiceBinder: IBinder =
        SugaIOTBluetoothLeServiceBinder()

    @Inject
    lateinit var sugaIOTGlucoseProfileManager: SugaIOTGlucoseProfileManager

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }

    private lateinit var bluetoothGatt: BluetoothGatt

    override fun onBind(intent: Intent): IBinder {
        return sugaIOTBluetoothLeServiceBinder
    }

    inner class SugaIOTBluetoothLeServiceBinder : Binder() {
        fun getServiceInstance(): SugaIOTBluetoothLeService {
            return this@SugaIOTBluetoothLeService
        }
    }


    fun connectToBluetoothLeDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(this, true, sugaIOTGlucoseProfileManager)
    }

}