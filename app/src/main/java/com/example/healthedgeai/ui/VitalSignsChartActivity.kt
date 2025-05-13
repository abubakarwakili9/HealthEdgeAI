package com.example.healthedgeai.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.healthedgeai.databinding.ActivityVitalSignsChartBinding
import com.example.healthedgeai.model.HealthRecord
import com.example.healthedgeai.viewmodel.VitalSignsChartViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class VitalSignsChartActivity : AppCompatActivity() {
    private val TAG = "VitalSignsChartActivity"

    private lateinit var binding: ActivityVitalSignsChartBinding
    private lateinit var viewModel: VitalSignsChartViewModel

    private var patientId: String? = null
    private var recordId: String? = null

    private val chartTypes = listOf(
        "Temperature (°C)",
        "Heart Rate (bpm)",
        "Blood Pressure (mmHg)",
        "Respiration Rate (breaths/min)",
        "Oxygen Saturation (%)",
        "Blood Glucose (mg/dL)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Initializing VitalSignsChartActivity")

        binding = ActivityVitalSignsChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        patientId = intent.getStringExtra("PATIENT_ID")
        recordId = intent.getStringExtra("RECORD_ID")

        if (patientId == null) {
            Log.e(TAG, "Patient ID is required")
            Toast.makeText(this, "Patient ID is required", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[VitalSignsChartViewModel::class.java]

        setupChartTypeSpinner()
        setupChart()
        setupListeners()

        // Load data
        viewModel.loadPatient(patientId!!)
        viewModel.loadHealthRecords(patientId!!)

        // Observe data
        observeViewModel()
    }

    private fun setupChartTypeSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, chartTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerChartType.adapter = adapter

        binding.spinnerChartType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateChart()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupChart() {
        with(binding.lineChart) {
            description.isEnabled = false
            setDrawGridBackground(false)

            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.valueFormatter = object : ValueFormatter() {
                private val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

                override fun getFormattedValue(value: Float): String {
                    val date = Date(value.toLong())
                    return dateFormat.format(date)
                }
            }

            axisLeft.setDrawGridLines(true)
            axisRight.isEnabled = false

            legend.isEnabled = true

            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Back button clicked, finishing activity")
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.patient.observe(this) { patient ->
            if (patient != null) {
                binding.tvPatientName.text = patient.name
                binding.tvPatientDetails.text = "Age: ${patient.age} | Gender: ${patient.gender}"
            }
        }

        viewModel.healthRecords.observe(this) { records ->
            if (records.isNotEmpty()) {
                updateChart()
            } else {
                binding.tvNoRecords.visibility = View.VISIBLE
                binding.lineChart.visibility = View.GONE
            }
        }
    }

    private fun updateChart() {
        val records = viewModel.healthRecords.value ?: return
        if (records.isEmpty()) return

        val selectedChartType = binding.spinnerChartType.selectedItemPosition

        // Sort records by timestamp
        val sortedRecords = records.sortedBy { it.timestamp }

        when (selectedChartType) {
            0 -> createTemperatureChart(sortedRecords)
            1 -> createHeartRateChart(sortedRecords)
            2 -> createBloodPressureChart(sortedRecords)
            3 -> createRespirationRateChart(sortedRecords)
            4 -> createOxygenSaturationChart(sortedRecords)
            5 -> createBloodGlucoseChart(sortedRecords)
        }

        binding.tvNoRecords.visibility = View.GONE
        binding.lineChart.visibility = View.VISIBLE

        // Highlight the current record if provided
        if (recordId != null) {
            val currentRecord = records.find { it.recordId == recordId }
            if (currentRecord != null) {
                val index = sortedRecords.indexOf(currentRecord)
                if (index >= 0) {
                    binding.lineChart.highlightValue(index.toFloat(), 0)
                }
            }
        }
    }

    private fun createTemperatureChart(records: List<HealthRecord>) {
        val entries = records.mapIndexed { index, record ->
            Entry(record.timestamp.toFloat(), record.temperature)
        }

        val dataSet = LineDataSet(entries, "Temperature (°C)").apply {
            color = Color.RED
            setCircleColor(Color.RED)
            valueTextSize = 12f
            lineWidth = 2f
        }

        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate()
    }

    private fun createHeartRateChart(records: List<HealthRecord>) {
        val entries = records.mapIndexed { index, record ->
            Entry(record.timestamp.toFloat(), record.heartRate.toFloat())
        }

        val dataSet = LineDataSet(entries, "Heart Rate (bpm)").apply {
            color = Color.BLUE
            setCircleColor(Color.BLUE)
            valueTextSize = 12f
            lineWidth = 2f
        }

        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate()
    }

    private fun createBloodPressureChart(records: List<HealthRecord>) {
        val systolicEntries = records.mapIndexed { index, record ->
            Entry(record.timestamp.toFloat(), record.bloodPressureSystolic.toFloat())
        }

        val diastolicEntries = records.mapIndexed { index, record ->
            Entry(record.timestamp.toFloat(), record.bloodPressureDiastolic.toFloat())
        }

        val systolicDataSet = LineDataSet(systolicEntries, "Systolic").apply {
            color = Color.RED
            setCircleColor(Color.RED)
            valueTextSize = 12f
            lineWidth = 2f
        }

        val diastolicDataSet = LineDataSet(diastolicEntries, "Diastolic").apply {
            color = Color.BLUE
            setCircleColor(Color.BLUE)
            valueTextSize = 12f
            lineWidth = 2f
        }

        val lineData = LineData(systolicDataSet, diastolicDataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate()
    }

    private fun createRespirationRateChart(records: List<HealthRecord>) {
        val entries = records.mapIndexed { index, record ->
            Entry(record.timestamp.toFloat(), record.respirationRate.toFloat())
        }

        val dataSet = LineDataSet(entries, "Respiration Rate").apply {
            color = Color.GREEN
            setCircleColor(Color.GREEN)
            valueTextSize = 12f
            lineWidth = 2f
        }

        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate()
    }

    private fun createOxygenSaturationChart(records: List<HealthRecord>) {
        val entries = records.mapIndexed { index, record ->
            Entry(record.timestamp.toFloat(), record.oxygenSaturation.toFloat())
        }

        val dataSet = LineDataSet(entries, "Oxygen Saturation (%)").apply {
            color = Color.CYAN
            setCircleColor(Color.CYAN)
            valueTextSize = 12f
            lineWidth = 2f
        }

        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate()
    }

    private fun createBloodGlucoseChart(records: List<HealthRecord>) {
        val entries = records.mapIndexed { index, record ->
            Entry(record.timestamp.toFloat(), record.bloodGlucose)
        }

        val dataSet = LineDataSet(entries, "Blood Glucose (mg/dL)").apply {
            color = Color.MAGENTA
            setCircleColor(Color.MAGENTA)
            valueTextSize = 12f
            lineWidth = 2f
        }

        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate()
    }
}