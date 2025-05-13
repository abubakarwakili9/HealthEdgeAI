package com.example.healthedgeai.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "health_records",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["patientId"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patientId")]
)
data class HealthRecord(
    @PrimaryKey val recordId: String = UUID.randomUUID().toString(),
    val patientId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val temperature: Float,
    val heartRate: Int,
    val bloodPressureSystolic: Int,
    val bloodPressureDiastolic: Int,
    val respirationRate: Int,
    val oxygenSaturation: Int,
    val bloodGlucose: Float,
    val weight: Float,
    val height: Float,
    val notes: String,
    val aiAssessment: String = "",
    val isSynced: Boolean = false
)