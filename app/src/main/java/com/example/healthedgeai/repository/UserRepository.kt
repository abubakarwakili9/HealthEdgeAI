package com.example.healthedgeai.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.healthedgeai.model.Patient
import com.example.healthedgeai.model.PatientDao
import com.example.healthedgeai.model.User
import com.example.healthedgeai.model.UserDao
import com.example.healthedgeai.model.UserRole
import com.example.healthedgeai.util.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Repository for handling user-related operations
 */
class UserRepository(
    private val userDao: UserDao,
    private val patientDao: PatientDao? = null // Optional PatientDao for patient role
) {
    private val TAG = "UserRepository"

    /**
     * Insert a user into the database
     */
    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    /**
     * Attempt to login with email and password
     * @return User if credentials are valid, null otherwise
     */
    suspend fun login(email: String, password: String): User? {
        try {
            // Get user by email only
            val user = userDao.getUserByEmail(email)

            if (user == null) {
                Log.d(TAG, "Login failed: No user found with email $email")
                return null
            }

            // For debugging
            Log.d(TAG, "Found user: $email, attempting password verification")

            // Verify password
            val isPasswordValid = PasswordUtils.verifyPassword(password, user.password)

            // For debugging
            Log.d(TAG, "Password verification result: $isPasswordValid")

            return if (isPasswordValid) user else null
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            return null
        }
    }

    /**
     * Get user by ID as LiveData
     */
    fun getUserById(userId: String): LiveData<User> {
        return userDao.getUserById(userId)
    }

    /**
     * Get user by ID synchronously
     */
    suspend fun getUserByIdSync(userId: String): User? {
        return userDao.getUserByIdSync(userId)
    }

    /**
     * Update an existing user
     */
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    /**
     * Check if email is already registered
     * @return true if email is taken, false otherwise
     */
    suspend fun isEmailTaken(email: String): Boolean {
        return userDao.isEmailTaken(email)
    }

    /**
     * Register a new user with detailed profile information
     * @return the created User object
     */
    suspend fun registerUser(
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
        medicalHistory: String? = null
    ): User {
        // Create a new user ID
        val userId = UUID.randomUUID().toString()

        // Hash the password
        val hashedPassword = PasswordUtils.hashPassword(password)

        // For debugging
        Log.d(TAG, "Registering user: $email with hashed password")

        // Create the user entity with hashed password
        val user = User(
            userId = userId,
            name = name,
            email = email,
            password = hashedPassword, // Store hashed password
            role = role,
            specialization = specialization,
            licenseNumber = licenseNumber,
            associatedFacility = associatedFacility,
            contactNumber = contactNumber,
            profileImageUrl = null,
            lastSyncTimestamp = System.currentTimeMillis()
        )

        // Insert the user
        insertUser(user)

        // If the user is a patient and we have a PatientDao, create a patient record
        if (role == UserRole.PATIENT && patientDao != null && age != null) {
            withContext(Dispatchers.IO) {
                val patient = Patient(
                    patientId = userId, // Use the same ID for easy linking
                    name = name,
                    age = age,
                    gender = gender ?: "Not specified",
                    contactNumber = contactNumber ?: "",
                    address = "",
                    medicalHistory = medicalHistory ?: "",
                    lastVisitTimestamp = System.currentTimeMillis(),
                    isSynced = false
                )
                patientDao.insertPatient(patient)
            }
        }

        return user
    }

    /**
     * Delete a user
     */
    suspend fun deleteUser(userId: String) {
        val user = userDao.getUserByIdSync(userId)
        user?.let {
            userDao.deleteUser(it)
        }
    }

    /**
     * Update user profile information
     */
    suspend fun updateUserProfile(
        userId: String,
        name: String? = null,
        contactNumber: String? = null,
        specialization: String? = null,
        licenseNumber: String? = null,
        associatedFacility: String? = null,
        profileImageUrl: String? = null
    ): Boolean {
        val user = userDao.getUserByIdSync(userId) ?: return false

        val updatedUser = user.copy(
            name = name ?: user.name,
            contactNumber = contactNumber ?: user.contactNumber,
            specialization = specialization ?: user.specialization,
            licenseNumber = licenseNumber ?: user.licenseNumber,
            associatedFacility = associatedFacility ?: user.associatedFacility,
            profileImageUrl = profileImageUrl ?: user.profileImageUrl
        )

        userDao.updateUser(updatedUser)
        return true
    }

    /**
     * Change user password
     */
    suspend fun changePassword(userId: String, currentPassword: String, newPassword: String): Boolean {
        val user = userDao.getUserByIdSync(userId) ?: return false

        // Verify current password
        if (!PasswordUtils.verifyPassword(currentPassword, user.password)) {
            return false
        }

        // Hash the new password
        val hashedNewPassword = PasswordUtils.hashPassword(newPassword)

        // Update with new password
        val updatedUser = user.copy(password = hashedNewPassword)
        userDao.updateUser(updatedUser)
        return true
    }

    /**
     * Add a sample user for testing if the database is empty
     */
    suspend fun addSampleUserIfEmpty() {
        val userCount = userDao.getUserCount()

        if (userCount == 0) {
            // Plain password for logging
            val plainPassword = "password123"

            // Hash the password
            val hashedPassword = PasswordUtils.hashPassword(plainPassword)

            // For debugging
            Log.d(TAG, "Adding sample user with email: worker@example.com and password: $plainPassword (hashed: $hashedPassword)")

            // Add a sample user for testing purposes
            val sampleUser = User(
                userId = "sample_user_id",
                name = "Healthcare Worker",
                email = "worker@example.com",
                role = UserRole.HEALTH_WORKER,
                password = hashedPassword, // Store hashed password
                contactNumber = "1234567890",
                associatedFacility = "Sample Clinic",
                lastSyncTimestamp = System.currentTimeMillis()
            )
            userDao.insertUser(sampleUser)
        }
    }

    /**
     * Debug method to add a test user with known credentials
     * (Only for development, remove in production)
     */
    suspend fun addTestUser() {
        val plainPassword = "test123"
        val hashedPassword = PasswordUtils.hashPassword(plainPassword)

        Log.d(TAG, "Adding test user: test@example.com / $plainPassword (hashed: $hashedPassword)")

        val testUser = User(
            userId = UUID.randomUUID().toString(),
            name = "Test User",
            email = "test@example.com",
            role = UserRole.ADMIN,
            password = hashedPassword,
            contactNumber = "1234567890",
            lastSyncTimestamp = System.currentTimeMillis()
        )

        userDao.insertUser(testUser)
    }
}