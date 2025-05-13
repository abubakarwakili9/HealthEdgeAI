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

class VitalSignsChartViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "VitalSignsChartVM"

    private val patientRepository: PatientRepository
    private val healthRecordRepository: HealthRecordRepository

    private val _patient = MutableLiveData<Patient>()
    val patient: LiveData<Patient> = _patient

    private val _healthRecords = MutableLiveData<List<HealthRecord>>()
    val healthRecords: LiveData<List<HealthRecord>> = _healthRecords

    init {
        Log.d(TAG, "Initializing VitalSignsChartViewModel")
        val database = AppDatabase.getDatabase(application)
        patientRepository = PatientRepository(database.patientDao())
        healthRecordRepository = HealthRecordRepository(database.healthRecordDao())
    }

    fun loadPatient(patientId: String) {
        Log.d(TAG, "Loading patient: $patientId")
        patientRepository.getPatientById(patientId).observeForever { patientData ->
            _patient.value = patientData

            if (patientData != null) {
                Log.d(TAG, "Patient loaded: ${patientData.name}")
            } else {
                Log.e(TAG, "Failed to load patient")
            }
        }
    }

    fun loadHealthRecords(patientId: String) {
        Log.d(TAG, "Loading health records for patient: $patientId")
        healthRecordRepository.getHealthRecordsForPatient(patientId).observeForever { records ->
            _healthRecords.value = records
            Log.d(TAG, "Loaded ${records.size} health records")
        }
    }
}