package com.example.healthedgeai.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val password: String, // Note: In production, store hashed password
    val role: UserRole,
    val specialization: String? = null, // For doctors
    val licenseNumber: String? = null, // For health professionals
    val associatedFacility: String? = null,
    val profileImageUrl: String? = null,
    val contactNumber: String? = null,
    val profileImagePath: String? = null,
    val isEmailVerified: Boolean = false,
    val verificationToken: String? = null,
    val verificationTokenExpiry: Long? = null,
    val lastSyncTimestamp: Long = 0

)