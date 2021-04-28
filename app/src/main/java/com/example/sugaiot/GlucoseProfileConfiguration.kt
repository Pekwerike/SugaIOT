package com.example.sugaiot

import android.util.ArrayMap
import java.util.*


/*
The 16-bit UUID Numbers Document contains a list of 16-bit Universally Unique Identifier (UUID) Values
for GATT Service, GATT Unit, GATT Declaration, GATT Descriptor, GATT Characteristic and Object Type,
16-bit UUID for members, Protocol Identifier, SDO GATT Service, Service Class and Profile
https://www.bluetooth.com/specifications/assigned-numbers/

The UUID configurations below are those relevant for interacting with a Glucose profile in a
Ble enabled glucose meter
* */
object GlucoseProfileConfiguration {
    val uuidLookUp : ArrayMap<String, UUID> = ArrayMap()
    const val GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID = "00002A18-0000-1000-8000-00805F9B34FB"
    const val GLUCOSE_FEATURE_CHARACTERISTIC_UUID = "00002A51-0000-1000-8000-00805F9B34FB"
    const val GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID =
        "00002A34-0000-1000-8000-00805F9B34FB"
    const val RECORD_ACCESS_CONTROL_POINT_CHARACTERISTIC_UUID =
        "00002A52-0000-1000-8000-00805F9B34FB"
    const val GLUCOSE_SERVICE_UUID = "00001808-0000-1000-8000-00805F9B34FB"
    const val CLIENT_CHARACTERISTICS_CONFIGURATION_DESCRIPTOR =
        "00002902-0000-1000-8000-00805F9B34FB"

    init {
        // generate the UUID for all the relevant configurations, in order to reduce the number of calls to java UUID.fromString
        uuidLookUp.apply {
            put(GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID, UUID.fromString(
                GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID))
            put(GLUCOSE_FEATURE_CHARACTERISTIC_UUID, UUID.fromString(
                GLUCOSE_FEATURE_CHARACTERISTIC_UUID))
            put(GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID, UUID.fromString(
                GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID))
            put(RECORD_ACCESS_CONTROL_POINT_CHARACTERISTIC_UUID, UUID.fromString(
                RECORD_ACCESS_CONTROL_POINT_CHARACTERISTIC_UUID))
            put(GLUCOSE_SERVICE_UUID, UUID.fromString(GLUCOSE_SERVICE_UUID))
            put(CLIENT_CHARACTERISTICS_CONFIGURATION_DESCRIPTOR, UUID.fromString(
                CLIENT_CHARACTERISTICS_CONFIGURATION_DESCRIPTOR))
        }
    }
}