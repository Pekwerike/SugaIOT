package com.example.sugaiot

import android.bluetooth.le.ScanResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {

    private val _isScanning: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val isScanning: LiveData<Boolean> = _isScanning

    private val _bluetoothLeScanResult = MutableLiveData<MutableList<ScanResult>>(mutableListOf())
    val bluetoothLeScanResult: LiveData<MutableList<ScanResult>> = _bluetoothLeScanResult

    fun scanStateUpdated() {
        _isScanning.value = !_isScanning.value!!
    }

    fun addBluetoothLeScanResult(scanResult: ScanResult) {
        _bluetoothLeScanResult.value = _bluetoothLeScanResult.value?.apply {
            add(scanResult)
        }
    }

}