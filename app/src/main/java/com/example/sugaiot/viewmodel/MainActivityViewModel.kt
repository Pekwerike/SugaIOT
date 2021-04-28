package com.example.sugaiot.viewmodel

import android.bluetooth.le.ScanResult
import android.util.ArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor() : ViewModel() {

    // boolean that determines the ui state of the search button
    private val _isScanning: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val isScanning: LiveData<Boolean> = _isScanning

    // list of scan results that will be displayed on the ui
    private val _bluetoothLeScanResultMap =
        MutableLiveData<ArrayMap<String, ScanResult>>(ArrayMap())
    val bluetoothLeScanResultMap: LiveData<ArrayMap<String, ScanResult>> = _bluetoothLeScanResultMap

    fun scanStateUpdated() {
        _isScanning.value = !_isScanning.value!!
    }

    fun addBluetoothLeScanResult(scanResult: ScanResult) {
        _bluetoothLeScanResultMap.value = _bluetoothLeScanResultMap.value?.apply {
            put(scanResult.device.address, scanResult)
        }
    }
}