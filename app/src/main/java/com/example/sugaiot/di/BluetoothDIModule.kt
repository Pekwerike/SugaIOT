package com.example.sugaiot.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import androidx.core.content.ContextCompat.getSystemService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class BluetoothDIModule {

    @Provides
    @Singleton
    fun getBluetoothAdapter(@ApplicationContext context: Context): BluetoothAdapter {
        val bluetoothAdapter: BluetoothAdapter by lazy {
            (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        }
        return bluetoothAdapter
    }

    @Provides
    @Singleton
    fun getBluetoothLeScanner(bluetoothAdapter: BluetoothAdapter): BluetoothLeScanner {
        val bluetoothLeScanner: BluetoothLeScanner by lazy {
            bluetoothAdapter.bluetoothLeScanner
        }
        return bluetoothLeScanner
    }
}