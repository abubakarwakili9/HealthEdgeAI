package com.example.healthedgeai.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.healthedgeai.model.AppDatabase
import com.example.healthedgeai.model.HealthRecord
import com.example.healthedgeai.model.Patient
import com.example.healthedgeai.repository.HealthRecordRepository
import com.example.healthedgeai.repository.PatientRepository

class PatientHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "PatientHistoryViewModel"

    private val patientRepository: PatientRepository
    private val healthRecordRepository: HealthRecordRepository

    private val _patient = MutableLiveData<Patient>()
    val patient: LiveData<Patient> = _patient

    private val _healthRecords = MutableLiveData<List<HealthRecord>>()
    val healthRecords: LiveData<List<HealthRecord>> = _healthRecords

    init {
        Log.d(TAG, "Initializing PatientHistoryViewModel")
        val database = AppDatabase.getDatabase(application)
        patientRepository = PatientRepository(database.patientDao())
        healthRecordRepository = HealthRecordRepository(database.healthRecordDao())
    }

    fun loadPatientData(patientId: String) {
        Log.d(TAG, "Loading data for patient ID: $patientId")

        // Load patient details
        patientRepository.getPatientById(patientId).observeForever { patient ->
            _patient.value = patient
            Log.d(TAG, "Patient data loaded: ${patient?.name}")
        }

        // Load health records
        healthRecordRepository.getHealthRecordsForPatient(patientId).observeForever { records ->
            // Sort records by timestamp in descending order (newest first)
            val sortedRecords = records.sortedByDescending { it.timestamp }
            _healthRecords.value = sortedRecords
            Log.d(TAG, "Health records loaded: ${records.size} records")
        }
    }
}