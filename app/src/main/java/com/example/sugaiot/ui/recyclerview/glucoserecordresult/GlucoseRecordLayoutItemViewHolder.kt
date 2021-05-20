package com.example.sugaiot.ui.recyclerview.glucoserecordresult

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.sugaiot.R
import com.example.sugaiot.databinding.GlucoseRecordLayoutItemBinding
import com.example.sugaiot.model.GlucoseMeasurementRecord
import java.util.*

class GlucoseRecordLayoutItemViewHolder(
    private val glucoseRecordLayoutItemBinding:
    GlucoseRecordLayoutItemBinding
) : RecyclerView.ViewHolder(glucoseRecordLayoutItemBinding.root) {

    fun bindGlucoseRecordData(glucoseMeasurementRecord: GlucoseMeasurementRecord) {
        glucoseRecordLayoutItemBinding.apply {
            recordTime = "${
                glucoseMeasurementRecord.calendar
                    .get(Calendar.HOUR)
            }:${
                glucoseMeasurementRecord.calendar
                    .get(Calendar.MINUTE)
            }${
                glucoseMeasurementRecord.calendar
                    .get(Calendar.AM_PM)
            }"
            glucoseConcentrationValue =
                glucoseMeasurementRecord.convertGlucoseConcentrationValueToMilligramsPerDeciliter()
        }
    }

    companion object {
        fun createViewHolder(parent: ViewGroup): GlucoseRecordLayoutItemViewHolder {
            val layoutBinding = DataBindingUtil.inflate<GlucoseRecordLayoutItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.glucose_record_layout_item,
                parent,
                false
            )
            return GlucoseRecordLayoutItemViewHolder(layoutBinding)
        }
    }
}