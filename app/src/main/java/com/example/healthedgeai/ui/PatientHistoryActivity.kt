package com.example.healthedgeai.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthedgeai.databinding.ActivityPatientHistoryBinding
import com.example.healthedgeai.viewmodel.PatientHistoryViewModel

class PatientHistoryActivity : AppCompatActivity() {
    private val TAG = "PatientHistoryActivity"

    private lateinit var binding: ActivityPatientHistoryBinding
    private lateinit var viewModel: PatientHistoryViewModel
    private lateinit var adapter: HealthRecordAdapter

    private var patientId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Initializing PatientHistoryActivity")

        binding = ActivityPatientHistoryBinding.inflate(layoutInflater)
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

        Log.d(TAG, "Patient ID: $patientId")

        viewModel = ViewModelProvider(this)[PatientHistoryViewModel::class.java]

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        // Load patient data and health records
        viewModel.loadPatientData(patientId!!)
    }

    private fun setupRecyclerView() {
        adapter = HealthRecordAdapter(supportFragmentManager)

        binding.rvHealthRecords.layoutManager = LinearLayoutManager(this)
        binding.rvHealthRecords.adapter = adapter
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Back button clicked, finishing activity")
            finish()
        }

        binding.fabNewAssessment.setOnClickListener {
            Log.d(TAG, "New assessment button clicked")
            val intent = Intent(this, HealthAssessmentActivity::class.java)
            intent.putExtra("PATIENT_ID", patientId)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.patient.observe(this) { patient ->
            if (patient != null) {
                Log.d(TAG, "Patient data received: ${patient.name}")
                binding.tvPatientName.text = patient.name
                binding.tvPatientDetails.text = "Age: ${patient.age} | Gender: ${patient.gender}"
            } else {
                Log.e(TAG, "Null patient data received")
            }
        }

        viewModel.healthRecords.observe(this) { records ->
            Log.d(TAG, "Health records received: ${records.size} records")
            adapter.submitList(records)

            if (records.isEmpty()) {
                binding.tvNoRecords.visibility = View.VISIBLE
                binding.rvHealthRecords.visibility = View.GONE
            } else {
                binding.tvNoRecords.visibility = View.GONE
                binding.rvHealthRecords.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity visible to user")

        // Refresh data when returning to this screen
        patientId?.let {
            viewModel.loadPatientData(it)
        }
    }
}