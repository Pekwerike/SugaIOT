package com.example.sugaiot.ui.recyclerview.glucoserecordresult

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewParent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.sugaiot.R
import com.example.sugaiot.databinding.GlucoseRecordHeaderLayoutItemBinding
import com.example.sugaiot.model.GlucoseMeasurementRecord
import java.util.*


class GlucoseRecordHeaderLayoutItemViewHolder
    (
    private val glucoseRecordHeaderLayoutItemBinding:
    GlucoseRecordHeaderLayoutItemBinding
) : RecyclerView.ViewHolder(glucoseRecordHeaderLayoutItemBinding.root) {

    fun bindGlucoseRecordData(date: String) {
        glucoseRecordHeaderLayoutItemBinding.groupDate = date
    }

    companion object {
        fun createViewHolder(parent: ViewGroup): GlucoseRecordHeaderLayoutItemViewHolder {
            val layoutBinding = DataBindingUtil.inflate<GlucoseRecordHeaderLayoutItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.glucose_record_header_layout_item,
                parent,
                false
            )
            return GlucoseRecordHeaderLayoutItemViewHolder(layoutBinding)
        }
    }
}