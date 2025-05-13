package com.example.healthedgeai.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.healthedgeai.databinding.ItemHealthRecordBinding
import com.example.healthedgeai.model.HealthRecord
import java.text.SimpleDateFormat
import java.util.*

class HealthRecordAdapter(
    private val fragmentManager: FragmentManager
) : ListAdapter<HealthRecord, HealthRecordAdapter.HealthRecordViewHolder>(HealthRecordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealthRecordViewHolder {
        val binding = ItemHealthRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HealthRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HealthRecordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HealthRecordViewHolder(private val binding: ItemHealthRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: HealthRecord) {
            // Format date and time
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val recordDate = Date(record.timestamp)

            binding.tvDate.text = dateFormat.format(recordDate)
            binding.tvTime.text = timeFormat.format(recordDate)

            // Set assessment result
            val assessmentResult = when {
                record.aiAssessment.contains("Healthy") -> "Healthy"
                record.aiAssessment.contains("Moderate") -> "Moderate Concern"
                record.aiAssessment.contains("Critical") -> "Critical"
                else -> "Completed"
            }
            binding.tvAssessmentResult.text = assessmentResult

            // Set color based on assessment
            val assessmentColor = when {
                record.aiAssessment.contains("Healthy") -> Color.parseColor("#4CAF50") // Green
                record.aiAssessment.contains("Moderate") -> Color.parseColor("#FF9800") // Orange
                record.aiAssessment.contains("Critical") -> Color.parseColor("#F44336") // Red
                else -> Color.parseColor("#607D8B") // Gray
            }
            binding.tvAssessmentResult.setTextColor(assessmentColor)

            // Set vital signs summary
            val vitalSigns = StringBuilder()
            vitalSigns.append("Temp: ${record.temperature}°C | ")
            vitalSigns.append("HR: ${record.heartRate} bpm | ")
            vitalSigns.append("BP: ${record.bloodPressureSystolic}/${record.bloodPressureDiastolic} mmHg | ")
            vitalSigns.append("SpO2: ${record.oxygenSaturation}%")
            binding.tvVitalSigns.text = vitalSigns.toString()

            // Set notes
            binding.tvNotes.text = if (record.notes.isNotEmpty()) record.notes else "No notes."

            // Set click listener for view details button
            binding.btnViewDetails.setOnClickListener {
                showHealthRecordDetails(record)
            }
        }

        private fun showHealthRecordDetails(record: HealthRecord) {
            val dialog = HealthRecordDetailsFragment.newInstance(record.recordId)
            dialog.show(fragmentManager, "HealthRecordDetails")
        }
    }

    private class HealthRecordDiffCallback : DiffUtil.ItemCallback<HealthRecord>() {
        override fun areItemsTheSame(oldItem: HealthRecord, newItem: HealthRecord): Boolean {
            return oldItem.recordId == newItem.recordId
        }

        override fun areContentsTheSame(oldItem: HealthRecord, newItem: HealthRecord): Boolean {
            return oldItem == newItem
        }
    }
}