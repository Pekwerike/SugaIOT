package com.example.sugaiot


/*
The 16-bit UUID Numbers Document contains a list of 16-bit Universally Unique Identifier (UUID) Values
for GATT Service, GATT Unit, GATT Declaration, GATT Descriptor, GATT Characteristic and Object Type,
16-bit UUID for members, Protocol Identifier, SDO GATT Service, Service Class and Profile
https://www.bluetooth.com/specifications/assigned-numbers/

The UUID configurations below are those relevant for interacting with a Glucose profile in a
Ble enabled glucose meter
* */
object GlucoseProfileConfiguration {
    const val GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID = "00002A18-0000-1000-8000-00805F9B34FB"
    const val GLUCOSE_FEATURE_CHARACTERISTIC_UUID = "00002A51-0000-1000-8000-00805F9B34FB"
    const val GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID =
        "00002A34-0000-1000-8000-00805F9B34FB"
    const val RECORD_ACCESS_CONTROL_POINT_CHARACTERISTIC_UUID =
        "00002A52-0000-1000-8000-00805F9B34FB"
    const val GLUCOSE_SERVICE_UUID = "00001808-0000-1000-8000-00805F9B34FB"
    const val CLIENT_CHARACTERISTICS_CONFIGURATION_DESCRIPTOR="00002902-0000-1000-8000-00805F9B34FB"
}