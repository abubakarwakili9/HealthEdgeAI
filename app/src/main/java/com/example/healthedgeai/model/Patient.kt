package com.example.healthedgeai.model

import androidx.room.*
import androidx.lifecycle.LiveData
import java.util.*

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey val patientId: String = UUID.randomUUID().toString(),
    val name: String,
    val age: Int,
    val gender: String,
    val contactNumber: String,
    val address: String,
    val medicalHistory: String,
    val lastVisitTimestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)