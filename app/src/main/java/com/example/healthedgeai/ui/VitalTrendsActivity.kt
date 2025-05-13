package com.example.healthedgeai.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.healthedgeai.databinding.ActivityVitalTrendsBinding
import com.example.healthedgeai.model.HealthRecord
import com.example.healthedgeai.viewmodel.VitalTrendsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class VitalTrendsActivity : AppCompatActivity() {
    private val TAG = "VitalTrendsActivity"

    private lateinit var binding: ActivityVitalTrendsBinding
    private lateinit var viewModel: VitalTrendsViewModel

    private var patientId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Initializing VitalTrendsActivity")

        binding = ActivityVitalTrendsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        patientId = intent.getStringExtra("PATIENT_ID")
        if (patientId == null) {
            Log.e(TAG, "No patient ID provided")
            Toast.makeText(this, "Patient ID is required", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[VitalTrendsViewModel::class.java]

        setupListeners()
        observeViewModel()

        // Load patient data
        viewModel.loadPatient(patientId!!)
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.patient.observe(this) { patient ->
            binding.tvPatientName.text = patient.name
            binding.tvPatientDetails.text = "Age: ${patient.age} | Gender: ${patient.gender}"
        }

        viewModel.healthRecords.observe(this) { records ->
            if (records.isNotEmpty()) {
                updateTemperatureChart(records)
                updateHeartRateChart(records)
                updateBloodPressureChart(records)
                updateOxygenSaturationChart(records)
            } else {
                Toast.makeText(this, "No health records available for this patient", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTemperatureChart(records: List<HealthRecord>) {
        val entries = records.mapIndexed { index, record ->
            Entry(index.toFloat(), record.temperature)
        }

        val dataSet = LineDataSet(entries, "Temperature").apply {
            color = Color.RED
            valueTextColor = Color.BLACK
            setDrawCircles(true)
            setDrawValues(true)
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(Color.RED)
        }

        val lineData = LineData(dataSet)
        binding.chartTemperature.apply {
            data = lineData
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.valueFormatter = DateValueFormatter(records)
            axisRight.isEnabled = false
            legend.isEnabled = true
            invalidate() // refresh
        }
    }

    private fun updateHeartRateChart(records: List<HealthRecord>) {
        val entries = records.mapIndexed { index, record ->
            Entry(index.toFloat(), record.heartRate.toFloat())
        }

        val dataSet = LineDataSet(entries, "Heart Rate").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            setDrawCircles(true)
            setDrawValues(true)
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(Color.BLUE)
        }

        val lineData = LineData(dataSet)
        binding.chartHeartRate.apply {
            data = lineData
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.valueFormatter = DateValueFormatter(records)
            axisRight.isEnabled = false
            legend.isEnabled = true
            invalidate() // refresh
        }
    }

    private fun updateBloodPressureChart(records: List<HealthRecord>) {
        val systolicEntries = records.mapIndexed { index, record ->
            Entry(index.toFloat(), record.bloodPressureSystolic.toFloat())
        }

        val diastolicEntries = records.mapIndexed { index, record ->
            Entry(index.toFloat(), record.bloodPressureDiastolic.toFloat())
        }

        val systolicDataSet = LineDataSet(systolicEntries, "Systolic").apply {
            color = Color.parseColor("#D32F2F") // Dark Red
            valueTextColor = Color.BLACK
            setDrawCircles(true)
            setDrawValues(true)
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(Color.parseColor("#D32F2F"))
        }

        val diastolicDataSet = LineDataSet(diastolicEntries, "Diastolic").apply {
            color = Color.parseColor("#1976D2") // Dark Blue
            valueTextColor = Color.BLACK
            setDrawCircles(true)
            setDrawValues(true)
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(Color.parseColor("#1976D2"))
        }

        val lineData = LineData(systolicDataSet, diastolicDataSet)
        binding.chartBloodPressure.apply {
            data = lineData
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.valueFormatter = DateValueFormatter(records)
            axisRight.isEnabled = false
            legend.isEnabled = true
            invalidate() // refresh
        }
    }

    private fun updateOxygenSaturationChart(records: List<HealthRecord>) {
        val entries = records.mapIndexed { index, record ->
            Entry(index.toFloat(), record.oxygenSaturation.toFloat())
        }

        val dataSet = LineDataSet(entries, "Oxygen Saturation").apply {
            color = Color.GREEN
            valueTextColor = Color.BLACK
            setDrawCircles(true)
            setDrawValues(true)
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(Color.GREEN)
        }

        val lineData = LineData(dataSet)
        binding.chartOxygenSaturation.apply {
            data = lineData
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.valueFormatter = DateValueFormatter(records)
            axisRight.isEnabled = false
            legend.isEnabled = true
            invalidate() // refresh
        }
    }

    // Custom formatter to display dates on X-axis
    inner class DateValueFormatter(private val records: List<HealthRecord>) : ValueFormatter() {
        private val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return if (index >= 0 && index < records.size) {
                dateFormat.format(Date(records[index].timestamp))
            } else {
                ""
            }
        }
    }
}