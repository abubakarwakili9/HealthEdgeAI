package com.example.healthedgeai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.healthedgeai.model.AppDatabase
import com.example.healthedgeai.model.HealthRecord
import com.example.healthedgeai.model.Patient
import com.example.healthedgeai.repository.HealthRecordRepository
import com.example.healthedgeai.repository.PatientRepository
import kotlinx.coroutines.launch

class VitalTrendsViewModel(application: Application) : AndroidViewModel(application) {

    private val healthRecordRepository: HealthRecordRepository
    private val patientRepository: PatientRepository

    private val _patient = MutableLiveData<Patient>()
    val patient: LiveData<Patient> = _patient

    private val _healthRecords = MutableLiveData<List<HealthRecord>>()
    val healthRecords: LiveData<List<HealthRecord>> = _healthRecords

    init {
        val db = AppDatabase.getDatabase(application)
        healthRecordRepository = HealthRecordRepository(db.healthRecordDao())
        patientRepository = PatientRepository(db.patientDao())
    }

    fun loadPatient(patientId: String) {
        viewModelScope.launch {
            patientRepository.getPatientById(patientId).observeForever { patient ->
                _patient.value = patient
            }

            healthRecordRepository.getHealthRecordsForPatient(patientId).observeForever { records ->
                // Sort records by timestamp (oldest to newest)
                _healthRecords.value = records.sortedBy { it.timestamp }
            }
        }
    }
}