package com.example.sugaiot.ui.bindingadapter

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.sugaiot.R


@BindingAdapter("setDeviceName", "setDeviceAddress", requireAll = true)
fun TextView.setDeviceNameAndAddress(deviceName: String, deviceAddress: String) {
    val spannedStringBuilder = SpannableStringBuilder().apply {
        append("${deviceName}\n")
        append(deviceAddress).apply {
            setSpan(
                TextAppearanceSpan(
                    rootView.context,
                    R.style.TextAppearance_MaterialComponents_Caption
                ), 0, deviceAddress.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
    }
    text = spannedStringBuilder
}