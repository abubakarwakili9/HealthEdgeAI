package com.example.healthedgeai.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.healthedgeai.databinding.ActivityPatientDashboardBinding
import com.example.healthedgeai.viewmodel.ProfileViewModel

class PatientDashboardActivity : AppCompatActivity() {
    private val TAG = "PatientDashboardActivity"
    private lateinit var binding: ActivityPatientDashboardBinding
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the user ID passed from login
        val userId = intent.getStringExtra("USER_ID") ?: return finish()

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // Load patient data
        viewModel.loadUser(userId)

        setupObservers()
        setupUI()
    }

    private fun setupObservers() {
        viewModel.user.observe(this) { user ->
            if (user != null) {
                // Update UI with user details
                binding.tvPatientName.text = user.name
                binding.tvPatientEmail.text = user.email
                // Update other fields as needed
            }
        }
    }

    private fun setupUI() {
        // Setup navigation, buttons, etc.
        binding.btnViewHistory.setOnClickListener {
            // Navigate to history
        }

        binding.btnScheduleAppointment.setOnClickListener {
            // Schedule appointment flow
        }

        // Logout button
        binding.btnLogout.setOnClickListener {
            // Logout logic
            finish()
        }
    }
}