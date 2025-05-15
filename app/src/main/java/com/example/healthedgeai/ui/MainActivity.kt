package com.example.healthedgeai.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthedgeai.R
import com.example.healthedgeai.databinding.ActivityMainBinding
import com.example.healthedgeai.service.SyncService
import com.example.healthedgeai.viewmodel.PatientViewModel

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: PatientViewModel
    private lateinit var adapter: PatientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Initializing MainActivity")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(this)[PatientViewModel::class.java]

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        // Add a sample patient for testing if no patients exist
        viewModel.allPatients.observe(this) { patients ->
            if (patients.isEmpty()) {
                addSamplePatient()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = PatientAdapter(
            context = this,
            onNewAssessment = { patient ->
                val intent = Intent(this, HealthAssessmentActivity::class.java)
                intent.putExtra("PATIENT_ID", patient.patientId)
                startActivity(intent)
            }
        )

        binding.rvPatients.layoutManager = LinearLayoutManager(this)
        binding.rvPatients.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAddPatient.setOnClickListener {
            Log.d(TAG, "Add patient button clicked")
            val dialog = AddPatientDialog()
            dialog.show(supportFragmentManager, "AddPatientDialog")
        }
    }

    private fun observeViewModel() {
        viewModel.allPatients.observe(this) { patients ->
            Log.d(TAG, "Patient list updated: ${patients.size} patients")
            adapter.submitList(patients)

            // Show/hide empty state
            binding.tvNoPatients.visibility = if (patients.isEmpty()) View.VISIBLE else View.GONE
            binding.rvPatients.visibility = if (patients.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    /**
     * Add a sample patient for testing
     */
    private fun addSamplePatient() {
        Log.d(TAG, "Adding sample patient for testing")
        viewModel.addPatient(
            name = "John Doe",
            age = 45,
            gender = "Male",
            contactNumber = "123-456-7890",
            address = "123 Main St, Anytown, MA",
            medicalHistory = "Hypertension, Type 2 Diabetes"
        )

        viewModel.addPatient(
            name = "Jane Smith",
            age = 35,
            gender = "Female",
            contactNumber = "555-123-4567",
            address = "456 Oak Ave, Somewhere, MA",
            medicalHistory = "Asthma, Allergies"
        )

        Toast.makeText(this, "Sample patients added for testing", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sync -> {
                Log.d(TAG, "Sync menu item selected")
                // Start sync service
                Intent(this, SyncService::class.java).also { intent ->
                    startService(intent)
                }
                Toast.makeText(this, "Syncing data...", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_profile -> {
                Log.d(TAG, "Profile menu item selected")
                // Navigate to profile screen
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_logout -> {
                Log.d(TAG, "Logout menu item selected")
                // Logout and navigate to login screen
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity visible to user")

        // Refresh patient list when returning to this screen
        viewModel.refreshPatients()
    }
}