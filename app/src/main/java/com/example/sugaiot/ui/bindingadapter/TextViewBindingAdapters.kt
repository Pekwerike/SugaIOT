package com.example.sugaiot.ui.bindingadapter

import android.bluetooth.BluetoothDevice
import android.graphics.Typeface.BOLD
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.sugaiot.R


@BindingAdapter("setDeviceName", "setDeviceAddress", requireAll = true)
fun TextView.setDeviceNameAndAddress(deviceName: String, deviceAddress: String) {
    val spannedStringBuilder = SpannableStringBuilder().apply {
        append("${deviceName}\n").apply {
            setSpan(
                TextAppearanceSpan(
                    rootView.context,
                    R.style.TextAppearance_MaterialComponents_Body1
                ), 0, deviceName.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            //   setSpan(StyleSpan(BOLD),0, deviceName.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE )
        }
        append(deviceAddress).apply {

        }
    }
    text = spannedStringBuilder
}

@BindingAdapter("setCanConnectText")
fun TextView.setCanConnectText(connectionStatus: String) {
    if (connectionStatus.contains("Can connect", true)) {
        text = connectionStatus
        setTextColor(ContextCompat.getColor(rootView.context, android.R.color.holo_green_light))
    } else {
        text = connectionStatus
        setTextColor(ContextCompat.getColor(rootView.context, android.R.color.holo_red_light))
    }
}

@BindingAdapter("setDeviceDetails")
fun TextView.setConnectedDeviceDetails(connectedDevice : BluetoothDevice){
    var accumulatedLength : Int = 0
    val spannedStringBuilder = SpannableStringBuilder().apply {
        append("Name: ") // length 4
        accumulatedLength = "Name: ".length
        append("${connectedDevice.name}\n").apply {
            setSpan(StyleSpan(BOLD), accumulatedLength, connectedDevice.name.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        }
        accumulatedLength += connectedDevice.name.length
        append("Status: ")
        append("connected \n").apply {
            setSpan(StyleSpan(BOLD), accumulatedLength, "connected".length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        }
    }
    text = spannedStringBuilder
}