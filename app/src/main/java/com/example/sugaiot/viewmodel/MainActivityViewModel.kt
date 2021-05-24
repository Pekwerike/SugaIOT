package com.example.sugaiot.viewmodel

import android.bluetooth.le.ScanResult
import android.util.ArrayMap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sugaiot.model.GlucoseMeasurementRecord
import com.example.sugaiot.ui.recyclerview.glucoserecordresult.GlucoseRecordRecyclerViewData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

val months = mapOf(
    1 to "January", 2 to "February", 3 to "March",
    4 to "April", 5 to "May", 6 to "June", 7 to "July", 8 to "August", 9 to "September",
    10 to "October", 11 to "November", 12 to "December"
)

@HiltViewModel
class MainActivityViewModel @Inject constructor() : ViewModel() {
    var recordsSentCompletely = false
    private val collectionOfGlucoseResults: MutableList<GlucoseMeasurementRecord> = mutableListOf()
    private val glucoseRecordRecyclerViewDataTempList: MutableList<GlucoseRecordRecyclerViewData> =
        mutableListOf()
    private val _glucoseRecordRecyclerViewDataList =
        MutableLiveData<MutableList<GlucoseRecordRecyclerViewData>>(
            mutableListOf()
        )
    val glucoseRecordRecyclerViewDataList: LiveData<MutableList<GlucoseRecordRecyclerViewData>>
        get() = _glucoseRecordRecyclerViewDataList

    // boolean that determines the ui state of the search button
    private val _isScanning: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val isScanning: LiveData<Boolean> = _isScanning

    // list of scan results that will be displayed on the ui
    private val _bluetoothLeScanResultMap = ArrayMap<String, ScanResult>(ArrayMap())


    private val _bluetoothLeScanResult = MutableLiveData<MutableList<ScanResult>>(mutableListOf())
    val bluetoothLeScanResult: LiveData<MutableList<ScanResult>> = _bluetoothLeScanResult

    fun scanStateUpdated() {
        _isScanning.value = !_isScanning.value!!
    }

    fun addBluetoothLeScanResult(scanResult: ScanResult) {
        _bluetoothLeScanResultMap.apply {
            put(scanResult.scanRecord?.bytes.contentToString(), scanResult)
            if (this.values.size > _bluetoothLeScanResult.value!!.size) {
                _bluetoothLeScanResult.value = values.toMutableList()
            }
        }
    }

    fun createGlucoseMeasurementRecordsRecyclerviewData() {
        recordsSentCompletely = true
        viewModelScope.launch(Dispatchers.IO) {
            Log.i("CollectionSize", collectionOfGlucoseResults.size.toString())
            val groupedList = try {
                collectionOfGlucoseResults.toSet()
            }catch (concurrentMoficationException: ConcurrentModificationException){
                collectionOfGlucoseResults
            }
            groupedList.reversed().groupBy {
                "${it.calendar.get(Calendar.DAY_OF_MONTH)} " +
                        "${months[it.calendar.get(Calendar.MONTH)]}, " +
                        "${it.calendar.get(Calendar.YEAR)}"
            }.forEach { (t, u) ->
                glucoseRecordRecyclerViewDataTempList.add(
                    GlucoseRecordRecyclerViewData.GlucoseMeasurementGroup(
                        t
                    )
                )
                glucoseRecordRecyclerViewDataTempList.addAll(u.map {
                    GlucoseRecordRecyclerViewData.GlucoseMeasurement(it)
                })
            }
            withContext(Dispatchers.Main) {
                _glucoseRecordRecyclerViewDataList.value =
                    glucoseRecordRecyclerViewDataTempList
            }
        }
    }

    fun addGlucoseMeasurementRecord(glucoseMeasurementRecord: GlucoseMeasurementRecord) {
        collectionOfGlucoseResults.add(glucoseMeasurementRecord)

    }
}