package com.example.sugaiot.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/*
SensorStatusAnnunciation provide additional information about the state of the
glucose sensor at the time of the  glucose concentration value measurement
*/
@Parcelize
data class SensorStatusAnnunciation(
    var deviceBatteryLowAtTimeOfMeasurement: Boolean = false,
    var sensorMalfunctionAtTimeOfMeasurement: Boolean = false,
    var bloodSampleInsufficientAtTimeOfMeasurement: Boolean = false,
    var stripInsertionError: Boolean = false,
    var stripTypeIncorrectForDevice: Boolean = false,
    var sensorResultHigherThanDeviceCanProcess: Boolean = false,
    var sensorResultLowerThanTheDeviceCanProcess: Boolean = false,
    var sensorTemperatureTooHighForValidTestResult: Boolean = false,
    var sensorTemperatureTooLowForValidTestResult: Boolean = false,
    var sensorReadInterruptedBecauseStripWasPulledTooSoon: Boolean = false,
    var generalDeviceFaultHasOccurredInSensor: Boolean = false,
    var timeFaultHasOccurredInTheSensor: Boolean = false,
) : Parcelable
