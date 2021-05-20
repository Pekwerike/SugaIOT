package com.example.sugaiot.glucoseprofilemanager

import java.util.*


/*
The 16-bit UUID Numbers Document contains a list of 16-bit Universally Unique Identifier (UUID) Values
for GATT Service, GATT Unit, GATT Declaration, GATT Descriptor, GATT Characteristic and Object Type,
16-bit UUID for members, Protocol Identifier, SDO GATT Service, Service Class and Profile
https://www.bluetooth.com/specifications/assigned-numbers/

The UUID configurations below are those relevant for interacting with a Glucose profile in a
Ble enabled glucose meter.

Additional references
https://www.bluetooth.com/specifications/specs/
https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Characteristics/org.bluetooth.characteristic.glucose_measurement.xml
https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Characteristics/org.bluetooth.characteristic.date_time.xml
https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Services/org.bluetooth.service.continuous_glucose_monitoring.xml
https://github.com/oesmith/gatt-xml/blob/master/org.bluetooth.characteristic.record_access_control_point.xml
*/
object GlucoseProfileConfiguration {

    val DEVICE_BATTERY_CHARACTERISTIC_UUID: UUID =
        UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
    val GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID: UUID =
        UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb")
    val GLUCOSE_FEATURE_CHARACTERISTIC_UUID: UUID =
        UUID.fromString("00002A51-0000-1000-8000-00805f9b34fb")
    val GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID: UUID =
        UUID.fromString("00002A34-0000-1000-8000-00805f9b34fb")
    val RECORD_ACCESS_CONTROL_POINT_CHARACTERISTIC_UUID: UUID =
        UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb")
    val GLUCOSE_SERVICE_UUID: UUID = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb")
    val CLIENT_CHARACTERISTICS_CONFIGURATION_DESCRIPTOR: UUID =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // Glucose profile opcodes
    const val OP_CODE_REPORT_STORED_RECORDS = 1
    const val OP_CODE_DELETE_STORED_RECORDS = 2
    const val OP_CODE_ABORT_OPERATION = 3
    const val OP_CODE_REPORT_NUMBER_OF_RECORDS = 4
    const val OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE = 5
    const val OP_CODE_RESPONSE_CODE = 6

    // Glucose profile operators
    const val OPERATOR_NULL = 0
    const val OPERATOR_ALL_RECORDS = 1
    const val OPERATOR_LESS_THEN_OR_EQUAL = 2
    const val OPERATOR_GREATER_THEN_OR_EQUAL = 3
    const val OPERATOR_WITHING_RANGE = 4
    const val OPERATOR_FIRST_RECORD = 5
    const val OPERATOR_LAST_RECORD = 6

    // Glucose measurement filter type
    const val FILTER_TYPE_NULL = 0
    const val FILTER_TYPE_SEQUENCE_NUMBER = 1
    const val FILTER_TYPE_USER_FACING_TIME = 2

    // Glucose profile response type
    const val RESPONSE_SUCCESS: Int = 1
    const val RESPONSE_OP_CODE_NOT_SUPPORTED = 2
    const val RESPONSE_INVALID_OPERATOR = 3
    const val RESPONSE_OPERATOR_NOT_SUPPORTED = 4
    const val RESPONSE_INVALID_OPERAND = 5
    const val RESPONSE_NO_RECORDS_FOUND = 6
    const val RESPONSE_ABORT_UNSUCCESSFUL = 7
    const val RESPONSE_PROCEDURE_NOT_COMPLETED = 8
    const val RESPONSE_OPERAND_NOT_SUPPORTED = 9

}
