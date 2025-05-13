package com.example.healthedgeai.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.healthedgeai.databinding.ActivityLoginBinding
import com.example.healthedgeai.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Initializing LoginActivity")

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // Add sample user to database if it's empty
        viewModel.addSampleUserIfEmpty()
        Log.d(TAG, "onCreate: Added sample user if database was empty")

        setupListeners()
        observeViewModel()

        // Auto-fill default credentials
        binding.etEmail.setText("worker@example.com")
        binding.etPassword.setText("password123")
        Log.d(TAG, "onCreate: Auto-filled default credentials")
    }

    private fun setupListeners() {
        Log.d(TAG, "setupListeners: Setting up button click listeners")

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            Log.d(TAG, "Login button clicked with email: $email")

            if (email.isEmpty() || password.isEmpty()) {
                Log.w(TAG, "Login attempt with empty fields")
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Attempting login with credentials: $email / ${password.replace(Regex("."), "*")}")
            viewModel.login(email, password)
        }
    }

    private fun observeViewModel() {
        Log.d(TAG, "observeViewModel: Setting up LiveData observers")

        viewModel.isLoading.observe(this) { isLoading ->
            Log.d(TAG, "Loading state changed: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        }

        viewModel.loginResult.observe(this) { user ->
            if (user != null) {
                Log.d(TAG, "Login successful for user: ${user.userId} (${user.name})")

                // Login successful, navigate to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("USER_ID", user.userId)
                startActivity(intent)
                finish()
            } else {
                Log.e(TAG, "Login failed - invalid credentials")

                // Login failed
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()

                // Add a debug message explaining what to do
                Log.d(TAG, "Debug tip: If you're using the default credentials and still " +
                        "can't log in, check if the database is properly initialized")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: LoginActivity visible to user")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: LoginActivity no longer visible")
    }
}