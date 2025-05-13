package com.example.healthedgeai.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.healthedgeai.model.AppDatabase
import com.example.healthedgeai.model.HealthRecord
import com.example.healthedgeai.model.Patient
import com.example.healthedgeai.repository.HealthRecordRepository
import com.example.healthedgeai.repository.PatientRepository
import com.example.healthedgeai.util.OnnxModelWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HealthAssessmentViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "HealthAssessmentVM"

    private val healthRecordRepository: HealthRecordRepository
    private val patientRepository: PatientRepository
    private val modelWrapper = OnnxModelWrapper(application)

    private val _patient = MutableLiveData<Patient>()
    val patient: LiveData<Patient> = _patient

    private val _healthRecords = MutableLiveData<List<HealthRecord>>()
    val healthRecords: LiveData<List<HealthRecord>> = _healthRecords

    private val _aiAssessment = MutableLiveData<String>()
    val aiAssessment: LiveData<String> = _aiAssessment

    private val _healthAlerts = MutableLiveData<List<String>>()
    val healthAlerts: LiveData<List<String>> = _healthAlerts

    private var currentHealthRecord: HealthRecord? = null

    init {
        Log.d(TAG, "Initializing HealthAssessmentViewModel")
        val db = AppDatabase.getDatabase(application)
        healthRecordRepository = HealthRecordRepository(db.healthRecordDao())
        patientRepository = PatientRepository(db.patientDao())
        modelWrapper.initialize()
    }

    fun loadPatient(patientId: String) {
        Log.d(TAG, "Loading patient with ID: $patientId")
        viewModelScope.launch {
            patientRepository.getPatientById(patientId).observeForever { patient ->
                _patient.value = patient
                Log.d(TAG, "Patient loaded: ${patient?.name}")
            }

            healthRecordRepository.getHealthRecordsForPatient(patientId).observeForever { records ->
                _healthRecords.value = records
                Log.d(TAG, "Health records loaded: ${records.size} records")
            }
        }
    }

    fun addHealthRecord(
        patientId: String,
        temperature: Float,
        heartRate: Int,
        bloodPressureSystolic: Int,
        bloodPressureDiastolic: Int,
        respirationRate: Int,
        oxygenSaturation: Int,
        bloodGlucose: Float,
        weight: Float,
        height: Float,
        notes: String
    ) {
        Log.d(TAG, "Adding health record for patient: $patientId")
        viewModelScope.launch {
            // Check for abnormal vital signs
            val alerts = checkVitalSigns(
                temperature,
                heartRate,
                bloodPressureSystolic,
                bloodPressureDiastolic,
                respirationRate,
                oxygenSaturation,
                bloodGlucose,
                weight,
                height
            )
            _healthAlerts.postValue(alerts)
            Log.d(TAG, "Health alerts detected: ${alerts.size}")

            // Create the health record
            val healthRecord = HealthRecord(
                patientId = patientId,
                temperature = temperature,
                heartRate = heartRate,
                bloodPressureSystolic = bloodPressureSystolic,
                bloodPressureDiastolic = bloodPressureDiastolic,
                respirationRate = respirationRate,
                oxygenSaturation = oxygenSaturation,
                bloodGlucose = bloodGlucose,
                weight = weight,
                height = height,
                notes = notes
            )

            // Generate AI assessment
            val features = floatArrayOf(
                temperature,
                heartRate.toFloat(),
                bloodPressureSystolic.toFloat(),
                bloodPressureDiastolic.toFloat(),
                respirationRate.toFloat(),
                oxygenSaturation.toFloat(),
                bloodGlucose,
                weight,
                height,
                // Add other relevant features for your model
                // The model likely expects 27 features (based on file name)
                // Fill in the rest with default or computed values
                0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f
            )

            Log.d(TAG, "Running inference with ONNX model")
            val assessment = withContext(Dispatchers.Default) {
                modelWrapper.runInference(features)
            }
            Log.d(TAG, "AI assessment result: $assessment")

            // Add the alert info to the AI assessment if there are abnormal values
            val assessmentWithAlerts = if (alerts.isNotEmpty()) {
                "$assessment\n\nAttention required for the following vital signs:\n${alerts.joinToString("\n")}"
            } else {
                assessment
            }

            // Update the health record with AI assessment
            val updatedHealthRecord = healthRecord.copy(aiAssessment = assessmentWithAlerts)
            currentHealthRecord = updatedHealthRecord

            // Update the assessment LiveData
            _aiAssessment.postValue(assessmentWithAlerts)
            Log.d(TAG, "Health record created and AI assessment generated")
        }
    }

    // Function to save the current health record to the database
    fun saveCurrentRecord() {
        currentHealthRecord?.let { record ->
            viewModelScope.launch {
                healthRecordRepository.insertHealthRecord(record)
                Log.d(TAG, "Health record saved to database: ${record.recordId}")

                // Update patient's last visit timestamp
                _patient.value?.let { patient ->
                    val updatedPatient = patient.copy(lastVisitTimestamp = System.currentTimeMillis())
                    patientRepository.updatePatient(updatedPatient)
                    Log.d(TAG, "Updated patient's last visit timestamp: ${patient.patientId}")
                }
            }
        }
    }

    private fun checkVitalSigns(
        temperature: Float,
        heartRate: Int,
        bloodPressureSystolic: Int,
        bloodPressureDiastolic: Int,
        respirationRate: Int,
        oxygenSaturation: Int,
        bloodGlucose: Float,
        weight: Float,
        height: Float
    ): List<String> {
        val alerts = mutableListOf<String>()

        // Check temperature
        if (temperature < 35.0f) {
            alerts.add("Hypothermia risk: Temperature is below normal range (${temperature}°C)")
        } else if (temperature > 38.0f) {
            alerts.add("Fever detected: Temperature is above normal range (${temperature}°C)")
        }

        // Check heart rate
        if (heartRate < 60) {
            alerts.add("Bradycardia: Heart rate is below normal range (${heartRate} bpm)")
        } else if (heartRate > 100) {
            alerts.add("Tachycardia: Heart rate is above normal range (${heartRate} bpm)")
        }

        // Check blood pressure
        if (bloodPressureSystolic > 140 || bloodPressureDiastolic > 90) {
            alerts.add("Hypertension risk: Blood pressure is above normal range (${bloodPressureSystolic}/${bloodPressureDiastolic} mmHg)")
        } else if (bloodPressureSystolic < 90 || bloodPressureDiastolic < 60) {
            alerts.add("Hypotension risk: Blood pressure is below normal range (${bloodPressureSystolic}/${bloodPressureDiastolic} mmHg)")
        }

        // Check respiratory rate
        if (respirationRate < 12) {
            alerts.add("Low respiration rate: Below normal range (${respirationRate} breaths/min)")
        } else if (respirationRate > 20) {
            alerts.add("High respiration rate: Above normal range (${respirationRate} breaths/min)")
        }

        // Check oxygen saturation
        if (oxygenSaturation < 95) {
            alerts.add("Low oxygen saturation: Below normal range (${oxygenSaturation}%)")
        }

        // Check blood glucose
        if (bloodGlucose < 70f) {
            alerts.add("Hypoglycemia risk: Blood glucose is below normal range (${bloodGlucose} mg/dL)")
        } else if (bloodGlucose > 126f) {
            alerts.add("Hyperglycemia risk: Blood glucose is above normal range (${bloodGlucose} mg/dL)")
        }

        // Calculate BMI if both weight and height are available
        if (weight > 0f && height > 0f) {
            val heightInMeters = height / 100f
            val bmi = weight / (heightInMeters * heightInMeters)

            if (bmi < 18.5f) {
                alerts.add("Underweight: BMI ${String.format("%.1f", bmi)} is below normal range")
            } else if (bmi >= 25f && bmi < 30f) {
                alerts.add("Overweight: BMI ${String.format("%.1f", bmi)} is above normal range")
            } else if (bmi >= 30f) {
                alerts.add("Obesity: BMI ${String.format("%.1f", bmi)} is well above normal range")
            }
        }

        return alerts
    }

    override fun onCleared() {
        super.onCleared()
        modelWrapper.close()
        Log.d(TAG, "ViewModel cleared, resources released")
    }
}