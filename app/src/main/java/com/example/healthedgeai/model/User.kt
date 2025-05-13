package com.example.healthedgeai.model


import androidx.room.*
import androidx.lifecycle.LiveData

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val role: String,
    val password: String, // Note: In a real app, this should be stored securely
    val lastSyncTimestamp: Long = 0
)