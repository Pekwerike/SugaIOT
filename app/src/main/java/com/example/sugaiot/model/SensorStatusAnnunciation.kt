package com.example.sugaiot.model

/*
SensorStatusAnnunciation provide additional information about the state of the
glucose sensor at the time of the  glucose concentration value measurement
*/
data class SensorStatusAnnunciation(
    val deviceBatteryLowAtTimeOfMeasurement: Boolean,
    val sensorMalfunctionAtTimeOfMeasurement: Boolean,
    val bloodSampleInsufficientAtTimeOfMeasurement: Boolean,
    val stripInsertionError: Boolean,
    val stripTypeIncorrectForDevice: Boolean,
    val sensorResultHigherThanDeviceCanProcess: Boolean,
    val sensorResultLowerThanTheDeviceCanProcess: Boolean,
    val sensorTemperatureTooHighForValidTestResult: Boolean,
    val sensorTemperatureTooLowForValidTestResult: Boolean,
    val sensorReadInterruptedBecauseStripWasPulledTooSoon: Boolean,
    val generalDeviceFaultHasOccurredInSensor: Boolean,
    val timeFaultHasOccurredInTheSensor: Boolean,
)
