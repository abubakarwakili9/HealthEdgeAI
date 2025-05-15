package com.example.healthedgeai.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.healthedgeai.R
import com.example.healthedgeai.databinding.ActivityProfileBinding
import com.example.healthedgeai.model.User
import com.example.healthedgeai.model.UserRole
import com.example.healthedgeai.util.ImageHelper
import com.example.healthedgeai.viewmodel.ProfileViewModel

class ProfileActivity : AppCompatActivity() {

    private val TAG = "ProfileActivity"
    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var imageHelper: ImageHelper

    private var userId: String? = null
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

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userId = intent.getStringExtra("USER_ID")
        if (userId == null) {
            Toast.makeText(this, "User ID is required", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        imageHelper = ImageHelper(this)

        setupListeners()
        observeViewModel()

        // Load user data
        viewModel.loadUser(userId!!)
    }

    private fun setupListeners() {
        binding.btnTakePhoto.setOnClickListener {
            if (checkCameraPermission()) {
                imageHelper.dispatchTakePictureIntent(takePictureLauncher)
            } else {
                requestCameraPermission()
            }
        }

        binding.btnSelectGallery.setOnClickListener {
            if (checkStoragePermission()) {
                imageHelper.dispatchSelectPictureIntent(selectPictureLauncher)
            } else {
                requestStoragePermission()
            }
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        binding.btnChangePassword.setOnClickListener {
            changePassword()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.user.observe(this) { user ->
            if (user != null) {
                // Populate user data
                binding.etName.setText(user.name)
                binding.etEmail.setText(user.email)
                binding.etContactNumber.setText(user.contactNumber ?: "")

                // Set profile image if exists
                user.profileImagePath?.let {
                    profileImagePath = it
                    updateProfileImagePreview()
                }

                // Show role-specific fields
                setupRoleSpecificFields(user.role, user)
            }
        }

        viewModel.updateResult.observe(this) { result ->
            when (result) {
                is ProfileViewModel.UpdateResult.Success -> {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
                is ProfileViewModel.UpdateResult.Error -> {
                    Toast.makeText(this, "Failed to update profile: ${result.message}", Toast.LENGTH_SHORT).show()
                }
                null -> {
                    // Initial state, do nothing
                }
            }
        }

        viewModel.passwordChangeResult.observe(this) { result ->
            when (result) {
                is ProfileViewModel.PasswordChangeResult.Success -> {
                    Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                    // Clear password fields
                    binding.etCurrentPassword.text?.clear()
                    binding.etNewPassword.text?.clear()
                    binding.etConfirmPassword.text?.clear()
                }
                is ProfileViewModel.PasswordChangeResult.Error -> {
                    Toast.makeText(this, "Failed to change password: ${result.message}", Toast.LENGTH_SHORT).show()
                }
                null -> {
                    // Initial state, do nothing
                }
            }
        }
    }

    private fun setupRoleSpecificFields(role: UserRole, user: User) {
        val container = binding.layoutRoleSpecificFields
        container.removeAllViews()

        // Inflate role-specific fields based on the user's role
        when (role) {
            UserRole.DOCTOR -> {
                val doctorFields = layoutInflater.inflate(
                    R.layout.profile_doctor_fields,
                    container,
                    false
                )

                // Find and populate fields
                val etSpecialization = doctorFields.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSpecialization)
                val etLicenseNumber = doctorFields.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLicenseNumber)
                val etFacility = doctorFields.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etFacility)

                etSpecialization.setText(user.specialization ?: "")
                etLicenseNumber.setText(user.licenseNumber ?: "")
                etFacility.setText(user.associatedFacility ?: "")

                container.addView(doctorFields)
            }
            UserRole.HEALTH_WORKER -> {
                val healthWorkerFields = layoutInflater.inflate(
                    R.layout.profile_health_worker_fields,
                    container,
                    false
                )

                // Find and populate fields
                val etLicenseNumber = healthWorkerFields.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etWorkerLicenseNumber)
                val etFacility = healthWorkerFields.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etWorkerFacility)

                etLicenseNumber.setText(user.licenseNumber ?: "")
                etFacility.setText(user.associatedFacility ?: "")

                container.addView(healthWorkerFields)
            }
            UserRole.PATIENT -> {
                // Add patient-specific fields if needed
            }
            UserRole.ADMIN -> {
                // Add admin-specific fields if needed
            }
        }
    }

    private fun updateProfileImagePreview() {
        profileImagePath?.let {
            try {
                val bitmap = BitmapFactory.decodeFile(it)
                binding.ivProfileImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile image", e)
            }
        }
    }

    private fun saveProfile() {
        val name = binding.etName.text.toString().trim()
        val contactNumber = binding.etContactNumber.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            binding.etName.requestFocus()
            return
        }

        // Get role-specific fields
        var specialization: String? = null
        var licenseNumber: String? = null
        var associatedFacility: String? = null

        val user = viewModel.user.value
        if (user != null) {
            when (user.role) {
                UserRole.DOCTOR -> {
                    val doctorFields = binding.layoutRoleSpecificFields.getChildAt(0)
                    specialization = doctorFields.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSpecialization).text.toString().trim()
                    licenseNumber = doctorFields.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLicenseNumber).text.toString().trim()
                    associatedFacility = doctorFields.findViewById<com.google.android.material.textfield.TextInputEditText>(
                        R.id.etFacility).text.toString().trim()
                }
                UserRole.HEALTH_WORKER -> {
                    val healthWorkerFields = binding.layoutRoleSpecificFields.getChildAt(0)
                    licenseNumber = healthWorkerFields.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etWorkerLicenseNumber).text.toString().trim()
                    associatedFacility = healthWorkerFields.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etWorkerFacility).text.toString().trim()
                }
                else -> {
                    // No additional fields for other roles
                }
            }
        }

        // Update profile
        viewModel.updateProfile(
            userId = userId!!,
            name = name,
            contactNumber = contactNumber.ifEmpty { null },
            specialization = specialization,
            licenseNumber = licenseNumber,
            associatedFacility = associatedFacility,
            profileImagePath = profileImagePath
        )
    }

    private fun changePassword() {
        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Validation
        if (currentPassword.isEmpty()) {
            binding.etCurrentPassword.error = "Current password is required"
            binding.etCurrentPassword.requestFocus()
            return
        }

        if (newPassword.isEmpty()) {
            binding.etNewPassword.error = "New password is required"
            binding.etNewPassword.requestFocus()
            return
        }

        if (newPassword.length < 6) {
            binding.etNewPassword.error = "Password must be at least 6 characters"
            binding.etNewPassword.requestFocus()
            return
        }

        if (newPassword != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            binding.etConfirmPassword.requestFocus()
            return
        }

        // Change password
        viewModel.changePassword(userId!!, currentPassword, newPassword)
    }

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
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    imageHelper.dispatchSelectPictureIntent(selectPictureLauncher)
                } else {
                    Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 101
        private const val STORAGE_PERMISSION_REQUEST_CODE = 102
    }
}