package com.example.healthedgeai.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.healthedgeai.databinding.FragmentHealthRecordDetailsBinding
import com.example.healthedgeai.model.HealthRecord
import com.example.healthedgeai.util.PdfExporter
import com.example.healthedgeai.viewmodel.HealthRecordDetailsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HealthRecordDetailsFragment : DialogFragment() {
    private val TAG = "HealthRecordDetailsFragment"

    private var _binding: FragmentHealthRecordDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HealthRecordDetailsViewModel
    private var recordId: String? = null

    companion object {
        private const val ARG_RECORD_ID = "recordId"

        fun newInstance(recordId: String): HealthRecordDetailsFragment {
            val fragment = HealthRecordDetailsFragment()
            val args = Bundle()
            args.putString(ARG_RECORD_ID, recordId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_MinWidth)

        recordId = arguments?.getString(ARG_RECORD_ID)
        if (recordId == null) {
            Log.e(TAG, "Record ID is required")
            dismiss()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthRecordDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[HealthRecordDetailsViewModel::class.java]

        setupListeners()

        // Load record data
        recordId?.let { id ->
            viewModel.loadHealthRecord(id)
        }

        // Observe the health record
        viewModel.healthRecord.observe(viewLifecycleOwner) { record ->
            if (record != null) {
                displayHealthRecord(record)
            } else {
                Log.e(TAG, "Failed to load health record")
                Toast.makeText(context, "Failed to load health record", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnExportPdf.setOnClickListener {
            recordId?.let { id ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val record = viewModel.getHealthRecordById(id)
                    if (record != null) {
                        // Export to PDF
                        context?.let { ctx ->
                            val pdfExporter = PdfExporter(ctx)
                            val result = pdfExporter.exportToPdf(record)

                            if (result) {
                                Toast.makeText(ctx, "PDF exported successfully", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(ctx, "Failed to export PDF", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        binding.btnViewCharts.setOnClickListener {
            recordId?.let { id ->
                val patientId = viewModel.healthRecord.value?.patientId
                if (patientId != null) {
                    // Navigate to charts activity
                    val intent = android.content.Intent(context, VitalSignsChartActivity::class.java)
                    intent.putExtra("PATIENT_ID", patientId)
                    intent.putExtra("RECORD_ID", id)
                    startActivity(intent)
                }
            }
        }
    }

    private fun displayHealthRecord(record: HealthRecord) {
        // Format date and time
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(record.timestamp))

        // Set basic info
        binding.tvDateTime.text = formattedDate

        // Set vital signs
        binding.tvTemperature.text = "${record.temperature}°C"
        binding.tvHeartRate.text = "${record.heartRate} bpm"
        binding.tvBloodPressure.text = "${record.bloodPressureSystolic}/${record.bloodPressureDiastolic} mmHg"
        binding.tvRespirationRate.text = "${record.respirationRate} breaths/min"
        binding.tvOxygenSaturation.text = "${record.oxygenSaturation}%"
        binding.tvBloodGlucose.text = "${record.bloodGlucose} mg/dL"

        // Set other measurements
        if (record.weight > 0) {
            binding.tvWeight.text = "${record.weight} kg"
        } else {
            binding.tvWeight.text = "Not recorded"
        }

        if (record.height > 0) {
            binding.tvHeight.text = "${record.height} cm"
        } else {
            binding.tvHeight.text = "Not recorded"
        }

        // Calculate BMI if both weight and height are available
        if (record.weight > 0 && record.height > 0) {
            val heightInMeters = record.height / 100
            val bmi = record.weight / (heightInMeters * heightInMeters)
            binding.tvBMI.text = String.format("%.1f", bmi)

            // BMI category
            val bmiCategory = when {
                bmi < 18.5 -> "Underweight"
                bmi < 25 -> "Normal"
                bmi < 30 -> "Overweight"
                else -> "Obese"
            }
            binding.tvBMICategory.text = bmiCategory
            binding.layoutBMI.visibility = View.VISIBLE
        } else {
            binding.layoutBMI.visibility = View.GONE
        }

        // Set notes
        if (record.notes.isNotEmpty()) {
            binding.tvNotes.text = record.notes
            binding.layoutNotes.visibility = View.VISIBLE
        } else {
            binding.layoutNotes.visibility = View.GONE
        }

        // Set AI assessment
        if (record.aiAssessment.isNotEmpty()) {
            binding.tvAssessment.text = record.aiAssessment

            // Set color based on assessment
            val assessmentColor = when {
                record.aiAssessment.contains("Healthy") -> android.graphics.Color.parseColor("#4CAF50") // Green
                record.aiAssessment.contains("Moderate") -> android.graphics.Color.parseColor("#FF9800") // Orange
                record.aiAssessment.contains("Critical") -> android.graphics.Color.parseColor("#F44336") // Red
                else -> android.graphics.Color.parseColor("#607D8B") // Gray
            }
            binding.tvAssessmentIndicator.setTextColor(assessmentColor)

            // Set assessment indicator text
            val assessmentText = when {
                record.aiAssessment.contains("Healthy") -> "Healthy"
                record.aiAssessment.contains("Moderate") -> "Moderate Concern"
                record.aiAssessment.contains("Critical") -> "Critical"
                else -> "Assessment Completed"
            }
            binding.tvAssessmentIndicator.text = assessmentText
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}