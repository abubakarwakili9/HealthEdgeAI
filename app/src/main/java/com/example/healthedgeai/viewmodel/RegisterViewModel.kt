package com.example.healthedgeai.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.healthedgeai.model.AppDatabase
import com.example.healthedgeai.model.User
import com.example.healthedgeai.model.UserRole
import com.example.healthedgeai.repository.UserRepository
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * ViewModel for handling user registration
 */
class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "RegisterViewModel"
    private val userRepository: UserRepository

    private val _registrationStatus = MutableLiveData<RegistrationStatus>()
    val registrationStatus: LiveData<RegistrationStatus> = _registrationStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        val database = AppDatabase.getDatabase(application)
        val userDao = database.userDao()
        val patientDao = database.patientDao() // Get patientDao for patient creation
        userRepository = UserRepository(userDao, patientDao)
    }

    /**
     * Register a new user with all possible profile information
     */
    fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole,
        contactNumber: String? = null,
        specialization: String? = null,
        licenseNumber: String? = null,
        associatedFacility: String? = null,
        age: Int? = null,
        gender: String? = null,
        medicalHistory: String? = null,
        profileImagePath: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d(TAG, "Starting registration process for $email with role $role")
                if (profileImagePath != null) {
                    Log.d(TAG, "User has selected a profile image: $profileImagePath")
                }

                // Check if email is already taken
                if (userRepository.isEmailTaken(email)) {
                    Log.d(TAG, "Email $email is already taken")
                    _registrationStatus.value = RegistrationStatus.EmailTaken
                    _isLoading.value = false
                    return@launch
                }

                // Validate based on role
                if (!validateRoleFields(role, specialization, licenseNumber, age, gender)) {
                    Log.d(TAG, "Validation failed for role $role")
                    _registrationStatus.value = RegistrationStatus.ValidationFailed
                    _isLoading.value = false
                    return@launch
                }

                // Register the user - notice we don't pass profileImagePath as UserRepository doesn't accept it
                Log.d(TAG, "Creating user record with role $role")
                val user = userRepository.registerUser(
                    name = name,
                    email = email,
                    password = password,
                    role = role,
                    contactNumber = contactNumber,
                    specialization = specialization,
                    licenseNumber = licenseNumber,
                    associatedFacility = associatedFacility,
                    age = age,
                    gender = gender,
                    medicalHistory = medicalHistory
                )

                // If profile image was provided, update the user profile with it
                if (profileImagePath != null) {
                    userRepository.updateUserProfile(
                        userId = user.userId,
                        profileImageUrl = profileImagePath
                    )
                }

                Log.d(TAG, "Registration successful for user ID: ${user.userId}")
                _registrationStatus.value = RegistrationStatus.Success(user)
            } catch (e: Exception) {
                Log.e(TAG, "Registration error: ${e.message}", e)
                _registrationStatus.value = RegistrationStatus.Error(e.message ?: "Registration failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Validate required fields based on the selected role
     */
    private fun validateRoleFields(
        role: UserRole,
        specialization: String?,
        licenseNumber: String?,
        age: Int?,
        gender: String?
    ): Boolean {
        return when (role) {
            UserRole.DOCTOR -> {
                // Doctors need specialization and license number
                !specialization.isNullOrBlank() && !licenseNumber.isNullOrBlank()
            }
            UserRole.HEALTH_WORKER -> {
                // Health workers have no mandatory specialized fields
                true
            }
            UserRole.PATIENT -> {
                // Patients need age and gender
                age != null && age > 0 && !gender.isNullOrBlank()
            }
            UserRole.ADMIN -> {
                // No special validation for admins
                true
            }
        }
    }

    /**
     * Check if an email is already in use
     */
    fun checkEmailAvailability(email: String) {
        viewModelScope.launch {
            try {
                val isTaken = userRepository.isEmailTaken(email)
                if (isTaken) {
                    _registrationStatus.value = RegistrationStatus.EmailTaken
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking email availability", e)
            }
        }
    }

    /**
     * Clear the registration status
     */
    fun clearRegistrationStatus() {
        _registrationStatus.value = null
    }

    /**
     * Define registration status sealed class for different outcomes
     */
    sealed class RegistrationStatus {
        data class Success(val user: User) : RegistrationStatus()
        object EmailTaken : RegistrationStatus()
        object ValidationFailed : RegistrationStatus()
        data class Error(val message: String) : RegistrationStatus()
    }
}