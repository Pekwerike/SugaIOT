package com.example.sugaiot.model

import android.icu.util.Calendar

/*
GlucoseMeasurementRecord class is used to collect a single record of the Glucose Measurement
Characteristic value from  the glucose sensor
*/
data class GlucoseMeasurementRecord(
    val sequenceNumber: Int,
    val calendar: Calendar,
    val timeOffset: Int,
    val glucoseConcentrationMeasurementUnit: String,
    val glucoseConcentrationValue: Float,
    val type: Int,
    val sampleLocationInteger: Int,
    var testBloodType: String = "",
    var sampleLocation: String = "",
    val sensorStatusAnnunciation: SensorStatusAnnunciation
) {
    init {
        testBloodType = when (type) {
            0 -> "Reserved for future use"
            1 -> "Capillary Whole blood"
            2 -> "Capillary Plasma"
            3 -> "Venous Whole blood"
            4 -> "Venous Plasma"
            5 -> "Arterial Whole blood"
            6 -> "Arterial Plasma"
            7 -> "Undetermined Whole blood"
            8 -> "Undetermined Plasma"
            9 -> "Interstitial Fluid (ISF)"
            10 -> "Control Solution"
            else -> "Reserved for future use"
        }
        sampleLocation = when (sampleLocationInteger) {
            0 -> "Reserved for future use"
            1 -> "Finger"
            2 -> "Alternate Site Test (AST)"
            3 -> "Earlobe"
            4 -> "Control solution"
            15 -> "Sample Location value not available"
            else -> "Reserved for future use"
        }
    }
}