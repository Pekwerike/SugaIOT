package com.example.sugaiot.model

import android.icu.util.Calendar

data class GlucoseMeasurementRecord(
    val sequenceNumber: Int,
    val calendar: Calendar,
    val timeOffset: Int,
    val glucoseConcentrationMeasurementUnit: String,
    val glucoseConcentrationValue: Float,
    val type: Int,
    val sampleLocation: Int,
    var testBloodType: String = ""
) {
    init {
        when (type) {
            0 -> testBloodType = "Reserved for future use"
            1 -> testBloodType = "Capillary Whole blood"
            2 -> testBloodType = "Capillary Plasma"
            3 -> testBloodType = "Venous Whole blood"
            4 -> testBloodType = "Venous Plasma"
            5 -> testBloodType = "Arterial Whole blood"
        }
    }
}