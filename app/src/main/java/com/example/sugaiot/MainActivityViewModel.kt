package com.example.sugaiot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {

    private val _isScanning = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isScanning: LiveData<Boolean> = _isScanning

    fun scanStateUpdated() {
        _isScanning.value = !_isScanning.value!!
    }

}