package com.example.healthedgeai.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "vital_signs_templates")
data class VitalSignsTemplate(
    @PrimaryKey val templateId: String = UUID.randomUUID().toString(),
    val name: String,
    val temperature: Float? = null,
    val heartRate: Int? = null,
    val bloodPressureSystolic: Int? = null,
    val bloodPressureDiastolic: Int? = null,
    val respirationRate: Int? = null,
    val oxygenSaturation: Int? = null,
    val bloodGlucose: Float? = null,
    val weight: Float? = null,
    val height: Float? = null
)