package com.example.sugaiot.ui.recyclerview.glucoserecordresult

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sugaiot.model.GlucoseMeasurementRecord

class GlucoseRecordsRecyclerViewAdapter :
    ListAdapter<GlucoseRecordRecyclerViewData, RecyclerView.ViewHolder>(
        GlucoseRecordsRecyclerViewAdapterDiffUtil
    ) {
    companion object {
        const val HEADER = 1
        const val ITEM = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GlucoseRecordRecyclerViewData.GlucoseMeasurement -> ITEM
            is GlucoseRecordRecyclerViewData.GlucoseMeasurementGroup -> HEADER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER -> GlucoseRecordHeaderLayoutItemViewHolder.createViewHolder(parent)
            ITEM -> GlucoseRecordLayoutItemViewHolder.createViewHolder(parent)
            else -> GlucoseRecordLayoutItemViewHolder.createViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is GlucoseRecordHeaderLayoutItemViewHolder -> {
                holder.bindGlucoseRecordData((getItem(position) as GlucoseRecordRecyclerViewData.GlucoseMeasurementGroup).date)
            }
            is GlucoseRecordLayoutItemViewHolder -> {
                holder.bindGlucoseRecordData((getItem(position) as GlucoseRecordRecyclerViewData.GlucoseMeasurement).glucoseMeasurementRecord)
            }
        }
    }
}

object GlucoseRecordsRecyclerViewAdapterDiffUtil :
    DiffUtil.ItemCallback<GlucoseRecordRecyclerViewData>() {
    override fun areItemsTheSame(
        oldItem: GlucoseRecordRecyclerViewData,
        newItem: GlucoseRecordRecyclerViewData
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: GlucoseRecordRecyclerViewData,
        newItem: GlucoseRecordRecyclerViewData
    ): Boolean {
        return oldItem == newItem
    }
}

sealed class GlucoseRecordRecyclerViewData {
    abstract val id: String

    data class GlucoseMeasurement(val glucoseMeasurementRecord: GlucoseMeasurementRecord) :
        GlucoseRecordRecyclerViewData() {
        override val id: String
            get() = glucoseMeasurementRecord.sequenceNumber.toString()
    }

    data class GlucoseMeasurementGroup(val date: String) :
        GlucoseRecordRecyclerViewData() {
        override val id: String
            get() = date
    }
}