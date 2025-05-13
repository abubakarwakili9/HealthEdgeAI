package com.example.healthedgeai.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.healthedgeai.model.AppDatabase
import com.example.healthedgeai.model.User
import com.example.healthedgeai.repository.UserRepository
import kotlinx.coroutines.launch



class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository

    private val _loginResult = MutableLiveData<User?>()
    val loginResult: LiveData<User?> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = userRepository.login(email, password)
            _loginResult.value = user
            _isLoading.value = false
        }
    }

    fun addSampleUserIfEmpty() {
        viewModelScope.launch {
            // Add a sample user for testing purposes
            val sampleUser = User(
                userId = "sample_user_id",
                name = "Healthcare Worker",
                email = "worker@example.com",
                role = "healthcare_worker",
                password = "password123" // In a real app, this should be hashed
            )
            userRepository.insertUser(sampleUser)
        }
    }
}