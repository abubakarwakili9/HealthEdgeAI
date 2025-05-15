package com.example.healthedgeai.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.healthedgeai.databinding.ActivityRegisterBinding
import com.example.healthedgeai.model.UserRole
import com.example.healthedgeai.util.ImageHelper
import com.example.healthedgeai.viewmodel.RegisterViewModel
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private val TAG = "RegisterActivity"
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel

    // Add image helper and profile image path
    private lateinit var imageHelper: ImageHelper
    private var profileImagePath: String? = null

    // Register for activity results
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            profileImagePath = imageHelper.getCurrentPhotoPath()
            updateProfileImagePreview()
        }
    }

    private val selectPictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val selectedImageUri = result.data?.data
            selectedImageUri?.let {
                profileImagePath = imageHelper.saveImageFromGallery(it)
                updateProfileImagePreview()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize image helper
        imageHelper = ImageHelper(this)

        // Remove any input filters that might prevent spaces
        binding.etName.filters = arrayOfNulls(0)
        binding.etEmail.filters = arrayOfNulls(0)
        binding.etPassword.filters = arrayOfNulls(0)
        binding.etContactNumber.filters = arrayOfNulls(0)

        // Handle role-specific fields that might be null with safe calls
        binding.etSpecialization?.filters = arrayOfNulls(0)
        binding.etLicenseNumber?.filters = arrayOfNulls(0)
        binding.etFacility?.filters = arrayOfNulls(0)
        binding.etWorkerLicenseNumber?.filters = arrayOfNulls(0)
        binding.etWorkerFacility?.filters = arrayOfNulls(0)
        binding.etMedicalHistory?.filters = arrayOfNulls(0)

        // Handle all TextInputEditText fields in the layout using a more general approach
        findAndClearAllEditTextFilters(binding.root)

        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        setupGenderDropdown()
        setupRoleSelection()
        setupListeners()
        observeViewModel()

        // Setup image buttons if they exist in the layout
        setupImageButtons()
    }

    private fun setupImageButtons() {
        // Setup image selection buttons if they exist in layout
        binding.btnTakePhoto?.setOnClickListener {
            if (checkCameraPermission()) {
                imageHelper.dispatchTakePictureIntent(takePictureLauncher)
            } else {
                requestCameraPermission()
            }
        }

        binding.btnSelectGallery?.setOnClickListener {
            if (checkStoragePermission()) {
                imageHelper.dispatchSelectPictureIntent(selectPictureLauncher)
            } else {
                requestStoragePermission()
            }
        }
    }

    private fun updateProfileImagePreview() {
        binding.ivProfileImage?.let { imageView ->
            profileImagePath?.let {
                try {
                    val bitmap = BitmapFactory.decodeFile(it)
                    imageView.setImageBitmap(bitmap)
                    imageView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading profile image: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Find all EditText fields in the view hierarchy and remove their input filters
     */
    private fun findAndClearAllEditTextFilters(view: View) {
        if (view is TextInputEditText || view is EditText) {
            (view as? EditText)?.filters = arrayOfNulls(0)
        } else if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                findAndClearAllEditTextFilters(view.getChildAt(i))
            }
        }
    }

    // UPDATED GENDER DROPDOWN METHOD
    private fun setupGenderDropdown() {
        val genders = arrayOf("Male", "Female", "Other")

        // Using simple_dropdown_item_1line which is a built-in layout
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        binding.actvGender.setAdapter(adapter)

        // Ensure dropdown shows when clicked
        binding.actvGender.setOnClickListener {
            binding.actvGender.showDropDown()
        }

        // For debugging
        binding.actvGender.setOnItemClickListener { parent, _, position, _ ->
            val selectedGender = genders[position]
            Log.d(TAG, "Selected gender: $selectedGender")
        }

        // Set threshold to 1 to show dropdown after typing one character
        binding.actvGender.threshold = 1
    }

    private fun setupRoleSelection() {
        binding.rgUserRole.setOnCheckedChangeListener { _, checkedId ->
            // Hide all role-specific layouts first
            binding.layoutDoctorFields.visibility = View.GONE
            binding.layoutHealthWorkerFields.visibility = View.GONE
            binding.layoutPatientFields.visibility = View.GONE
            binding.layoutAdminFields?.visibility = View.GONE // Using safe call in case it's not in the layout

            when (checkedId) {
                binding.rbDoctor.id -> {
                    Log.d(TAG, "Doctor role selected")
                    binding.layoutDoctorFields.visibility = View.VISIBLE
                }
                binding.rbHealthWorker.id -> {
                    Log.d(TAG, "Health Worker role selected")
                    binding.layoutHealthWorkerFields.visibility = View.VISIBLE
                }
                binding.rbPatient.id -> {
                    Log.d(TAG, "Patient role selected")
                    binding.layoutPatientFields.visibility = View.VISIBLE
                }
                binding.rbAdmin.id -> {
                    Log.d(TAG, "Administrator role selected")
                    binding.layoutAdminFields?.visibility = View.VISIBLE
                }
            }
        }

        // Default to first option
        binding.rbDoctor.isChecked = true
        binding.layoutDoctorFields.visibility = View.VISIBLE
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            Log.d(TAG, "Register button clicked")
            if (validateInputs()) {
                registerUser()
            }
        }

        binding.tvLogin.setOnClickListener {
            Log.d(TAG, "Login link clicked")
            // Navigate to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            binding.etName.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            binding.etEmail.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Invalid email format"
            binding.etEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            binding.etPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            binding.etPassword.requestFocus()
            return false
        }

        // Check if a role is selected
        if (binding.rgUserRole.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate role-specific fields
        val selectedRoleId = binding.rgUserRole.checkedRadioButtonId

        when (selectedRoleId) {
            binding.rbDoctor.id -> {
                val specialization = binding.etSpecialization.text.toString().trim()
                val licenseNumber = binding.etLicenseNumber.text.toString().trim()

                if (specialization.isEmpty()) {
                    binding.etSpecialization.error = "Specialization is required"
                    binding.etSpecialization.requestFocus()
                    return false
                }

                if (licenseNumber.isEmpty()) {
                    binding.etLicenseNumber.error = "License number is required"
                    binding.etLicenseNumber.requestFocus()
                    return false
                }
            }
            binding.rbHealthWorker.id -> {
                // No mandatory fields for health worker
            }
            binding.rbPatient.id -> {
                val age = binding.etAge.text.toString().trim()
                val gender = binding.actvGender.text.toString().trim()

                if (age.isEmpty()) {
                    binding.etAge.error = "Age is required"
                    binding.etAge.requestFocus()
                    return false
                }

                try {
                    val ageValue = age.toInt()
                    if (ageValue < 0 || ageValue > 120) {
                        binding.etAge.error = "Please enter a valid age"
                        binding.etAge.requestFocus()
                        return false
                    }
                } catch (e: NumberFormatException) {
                    binding.etAge.error = "Please enter a valid age"
                    binding.etAge.requestFocus()
                    return false
                }

                if (gender.isEmpty()) {
                    binding.actvGender.error = "Gender is required"
                    binding.actvGender.requestFocus()
                    return false
                }
            }
        }

        return true
    }

    private fun registerUser() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val contactNumber = binding.etContactNumber.text.toString().trim()

        // Determine selected role
        val selectedRoleId = binding.rgUserRole.checkedRadioButtonId
        val roleRadioButton = findViewById<RadioButton>(selectedRoleId)
        val roleName = roleRadioButton.text.toString()

        val role = when (roleName) {
            "Doctor" -> UserRole.DOCTOR
            "Health Worker" -> UserRole.HEALTH_WORKER
            "Patient" -> UserRole.PATIENT
            "Administrator" -> UserRole.ADMIN
            else -> UserRole.PATIENT // Default
        }

        Log.d(TAG, "Registering user with role: $role")

        // Get role-specific fields
        var specialization: String? = null
        var licenseNumber: String? = null
        var associatedFacility: String? = null
        var age: Int? = null
        var gender: String? = null
        var medicalHistory: String? = null

        when (role) {
            UserRole.DOCTOR -> {
                specialization = binding.etSpecialization.text.toString().trim()
                licenseNumber = binding.etLicenseNumber.text.toString().trim()
                associatedFacility = binding.etFacility.text.toString().trim()

                Log.d(TAG, "Doctor fields: specialization=$specialization, licenseNumber=$licenseNumber, facility=$associatedFacility")
            }
            UserRole.HEALTH_WORKER -> {
                licenseNumber = binding.etWorkerLicenseNumber.text.toString().trim()
                associatedFacility = binding.etWorkerFacility.text.toString().trim()

                Log.d(TAG, "Health Worker fields: licenseNumber=$licenseNumber, facility=$associatedFacility")
            }
            UserRole.PATIENT -> {
                try {
                    val ageText = binding.etAge.text.toString().trim()
                    age = if (ageText.isNotEmpty()) ageText.toInt() else null
                } catch (e: Exception) {
                    age = null
                    Log.e(TAG, "Error parsing age", e)
                }

                gender = binding.actvGender.text.toString().trim()
                medicalHistory = binding.etMedicalHistory.text.toString().trim()

                Log.d(TAG, "Patient fields: age=$age, gender=$gender, medicalHistory=$medicalHistory")
            }
            UserRole.ADMIN -> {
                // No special fields for admin
                Log.d(TAG, "Admin role - no special fields")
            }
        }

        // Register the user with profile image path
        viewModel.register(
            name = name,
            email = email,
            password = password,
            role = role,
            contactNumber = contactNumber.ifEmpty { null },
            specialization = specialization,
            licenseNumber = licenseNumber,
            associatedFacility = associatedFacility,
            age = age,
            gender = gender,
            medicalHistory = medicalHistory,
            profileImagePath = profileImagePath
        )
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !isLoading
        }

        viewModel.registrationStatus.observe(this) { status ->
            when (status) {
                is RegisterViewModel.RegistrationStatus.Success -> {
                    Log.d(TAG, "Registration successful: ${status.user}")
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                    // Go back to login screen with pre-filled email
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.putExtra("EMAIL", status.user.email)
                    startActivity(intent)
                    finish()
                }
                is RegisterViewModel.RegistrationStatus.EmailTaken -> {
                    Log.d(TAG, "Registration failed: Email already taken")
                    binding.etEmail.error = "Email already registered"
                    binding.etEmail.requestFocus()
                    Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                }
                is RegisterViewModel.RegistrationStatus.ValidationFailed -> {
                    Log.d(TAG, "Registration failed: Validation failed")
                    Toast.makeText(this, "Please fill all required fields correctly", Toast.LENGTH_SHORT).show()
                }
                is RegisterViewModel.RegistrationStatus.Error -> {
                    Log.e(TAG, "Registration error: ${status.message}")
                    Toast.makeText(this, "Registration failed: ${status.message}", Toast.LENGTH_SHORT).show()
                }
                null -> {
                    // Initial state, do nothing
                }
            }
        }
    }

    // Permission checking and requesting methods for camera and storage
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    imageHelper.dispatchTakePictureIntent(takePictureLauncher)
                } else {
                    Toast.makeText(this, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    imageHelper.dispatchSelectPictureIntent(selectPictureLauncher)
                } else {
                    Toast.makeText(this, "Storage permission is required to select an image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 101
        private const val STORAGE_PERMISSION_REQUEST_CODE = 102
    }
}