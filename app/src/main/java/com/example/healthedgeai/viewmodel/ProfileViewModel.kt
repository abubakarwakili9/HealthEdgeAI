package com.example.healthedgeai.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.example.healthedgeai.model.AppDatabase
import com.example.healthedgeai.model.User
import com.example.healthedgeai.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "ProfileViewModel"
    private val userRepository: UserRepository

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _updateResult = MutableLiveData<UpdateResult>()
    val updateResult: LiveData<UpdateResult> = _updateResult

    private val _passwordChangeResult = MutableLiveData<PasswordChangeResult>()
    val passwordChangeResult: LiveData<PasswordChangeResult> = _passwordChangeResult

    // Keep track of the current user LiveData so we can remove our observer
    private var currentUserLiveData: LiveData<User>? = null
    private val userObserver = Observer<User> { userResult ->
        _user.value = userResult
        _isLoading.value = false
    }

    init {
        val database = AppDatabase.getDatabase(application)
        val userDao = database.userDao()
        val patientDao = database.patientDao()
        userRepository = UserRepository(userDao, patientDao)
    }

    fun loadUser(userId: String) {
        _isLoading.value = true

        // Remove any existing observer to prevent memory leaks
        currentUserLiveData?.removeObserver(userObserver)

        // Get the LiveData from the repository
        val userLiveData = userRepository.getUserById(userId)

        // Observe the LiveData
        userLiveData.observeForever(userObserver)

        // Store reference to current LiveData so we can remove observer later
        currentUserLiveData = userLiveData
    }

    fun updateProfile(
        userId: String,
        name: String,
        contactNumber: String?,
        specialization: String?,
        licenseNumber: String?,
        associatedFacility: String?,
        profileImagePath: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = userRepository.updateUserProfile(
                    userId = userId,
                    name = name,
                    contactNumber = contactNumber,
                    specialization = specialization,
                    licenseNumber = licenseNumber,
                    associatedFacility = associatedFacility,
                    profileImageUrl = profileImagePath  // Changed parameter name to match repository
                )

                if (success) {
                    loadUser(userId) // Reload user data
                    _updateResult.value = UpdateResult.Success
                } else {
                    _updateResult.value = UpdateResult.Error("Failed to update profile")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile", e)
                _updateResult.value = UpdateResult.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changePassword(userId: String, currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = userRepository.changePassword(userId, currentPassword, newPassword)

                if (success) {
                    _passwordChangeResult.value = PasswordChangeResult.Success
                } else {
                    _passwordChangeResult.value = PasswordChangeResult.Error("Current password is incorrect")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error changing password", e)
                _passwordChangeResult.value = PasswordChangeResult.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Clean up when the ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        // Remove the observer to prevent memory leaks
        currentUserLiveData?.removeObserver(userObserver)
    }

    sealed class UpdateResult {
        object Success : UpdateResult()
        data class Error(val message: String) : UpdateResult()
    }

    sealed class PasswordChangeResult {
        object Success : PasswordChangeResult()
        data class Error(val message: String) : PasswordChangeResult()
    }
}