package com.example.healthedgeai.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.healthedgeai.model.AppDatabase
import com.example.healthedgeai.model.User
import com.example.healthedgeai.repository.UserRepository
import com.example.healthedgeai.util.PasswordUtils
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "LoginViewModel"
    private val userRepository: UserRepository

    private val _loginResult = MutableLiveData<User?>()
    val loginResult: LiveData<User?> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        val database = AppDatabase.getDatabase(application)
        val userDao = database.userDao()
        val patientDao = database.patientDao()
        userRepository = UserRepository(userDao, patientDao)
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                Log.d(TAG, "Attempting login with: $email")

                val user = userRepository.login(email, password)

                if (user != null) {
                    Log.d(TAG, "Login successful for: ${user.email}")
                    _loginResult.value = user
                } else {
                    Log.d(TAG, "Login failed for: $email")
                    _error.value = "Invalid email or password"
                    _loginResult.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during login", e)
                _error.value = "Error: ${e.message}"
                _loginResult.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Add this method to delegate to the repository
    fun addSampleUserIfEmpty() {
        viewModelScope.launch {
            try {
                userRepository.addSampleUserIfEmpty()
                Log.d(TAG, "Sample user added if database was empty")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding sample user", e)
            }
        }
    }

    // Optional: Add a method to create a test user with known credentials
    fun addTestUser() {
        viewModelScope.launch {
            try {
                // Delegate to UserRepository if it has this method
                // Otherwise implement it here
                val plainPassword = "test123"
                val hashedPassword = PasswordUtils.hashPassword(plainPassword)

                Log.d(TAG, "Adding test user: test@example.com / $plainPassword")

                // Create and insert a test user
                // Implementation depends on your User class and repository methods
            } catch (e: Exception) {
                Log.e(TAG, "Error adding test user", e)
            }
        }
    }

    fun clearLoginResult() {
        _loginResult.value = null
        _error.value = null
    }
}