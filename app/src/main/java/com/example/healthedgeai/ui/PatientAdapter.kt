package com.example.healthedgeai.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.healthedgeai.databinding.ItemPatientBinding
import com.example.healthedgeai.model.Patient
import java.text.SimpleDateFormat
import java.util.*

class PatientAdapter(
    private val context: Context,
    private val onNewAssessment: (Patient) -> Unit
) : ListAdapter<Patient, PatientAdapter.PatientViewHolder>(PatientDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val binding = ItemPatientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PatientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PatientViewHolder(private val binding: ItemPatientBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(patient: Patient) {
            binding.tvPatientName.text = patient.name
            binding.tvPatientDetails.text = "Age: ${patient.age} | Gender: ${patient.gender}"

            // Format last visit date
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val lastVisitDate = dateFormat.format(Date(patient.lastVisitTimestamp))
            binding.tvLastVisit.text = "Last Visit: $lastVisitDate"

            binding.btnNewAssessment.setOnClickListener {
                onNewAssessment(patient)
            }

            binding.btnViewHistory.setOnClickListener {
                // Navigate to PatientHistoryActivity
                val intent = Intent(context, PatientHistoryActivity::class.java)
                intent.putExtra("PATIENT_ID", patient.patientId)
                context.startActivity(intent)
            }
        }
    }

    private class PatientDiffCallback : DiffUtil.ItemCallback<Patient>() {
        override fun areItemsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem.patientId == newItem.patientId
        }

        override fun areContentsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem == newItem
        }
    }
}