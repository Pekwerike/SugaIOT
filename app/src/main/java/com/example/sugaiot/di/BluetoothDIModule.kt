package com.example.sugaiot.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import androidx.core.content.ContextCompat.getSystemService
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class BluetoothDIModule {

    fun getBluetoothManager(@ApplicationContext context: Context): BluetoothManager {
        val bluetoothManager: BluetoothManager by lazy {
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        }
        return bluetoothManager
    }

    fun getBluetoothAdapter(bluetoothManager: BluetoothManager): BluetoothAdapter {
        val bluetoothAdapter: BluetoothAdapter by lazy {
            bluetoothManager.adapter
        }
        return bluetoothAdapter
    }

    fun getBluetoothLeScanner(bluetoothAdapter: BluetoothAdapter): BluetoothLeScanner{
        val bluetoothLeScanner : BluetoothLeScanner by lazy{
            bluetoothAdapter.bluetoothLeScanner
        }
        return bluetoothLeScanner
    }
}