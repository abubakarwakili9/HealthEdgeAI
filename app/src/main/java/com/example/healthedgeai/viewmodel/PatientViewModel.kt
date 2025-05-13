package com.example.healthedgeai.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.healthedgeai.model.AppDatabase
import com.example.healthedgeai.model.Patient
import com.example.healthedgeai.repository.PatientRepository
import kotlinx.coroutines.launch

class PatientViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "PatientViewModel"

    private val repository: PatientRepository
    val allPatients: LiveData<List<Patient>>

    init {
        Log.d(TAG, "Initializing PatientViewModel")
        val patientDao = AppDatabase.getDatabase(application).patientDao()
        repository = PatientRepository(patientDao)
        allPatients = repository.allPatients

        // Debug: Log the initial patient count
        viewModelScope.launch {
            val patients = repository.getAllPatientsList()
            Log.d(TAG, "Initial patient count: ${patients.size}")
            for (patient in patients) {
                Log.d(TAG, "Patient: ${patient.name}, ID: ${patient.patientId}")
            }
        }
    }

    fun addPatient(
        name: String,
        age: Int,
        gender: String,
        contactNumber: String,
        address: String,
        medicalHistory: String
    ) {
        Log.d(TAG, "Adding new patient: $name")
        val patient = Patient(
            name = name,
            age = age,
            gender = gender,
            contactNumber = contactNumber,
            address = address,
            medicalHistory = medicalHistory
        )
        viewModelScope.launch {
            repository.insertPatient(patient)
            Log.d(TAG, "Patient added: ${patient.patientId}")

            // Debug: Log the updated patient count
            val patients = repository.getAllPatientsList()
            Log.d(TAG, "Updated patient count: ${patients.size}")
            for (pat in patients) {
                Log.d(TAG, "Patient: ${pat.name}, ID: ${pat.patientId}")
            }
        }
    }

    fun getPatientById(patientId: String): LiveData<Patient> {
        Log.d(TAG, "Getting patient with ID: $patientId")
        return repository.getPatientById(patientId)
    }

    fun updatePatient(patient: Patient) {
        Log.d(TAG, "Updating patient: ${patient.patientId}")
        viewModelScope.launch {
            repository.updatePatient(patient)
            Log.d(TAG, "Patient updated: ${patient.patientId}")
        }
    }

    fun deletePatient(patient: Patient) {
        Log.d(TAG, "Deleting patient: ${patient.patientId}")
        viewModelScope.launch {
            repository.deletePatient(patient)
            Log.d(TAG, "Patient deleted: ${patient.patientId}")
        }
    }

    // This method doesn't actually do anything different since we're using LiveData
    // which automatically updates when the database changes. It's included here
    // for clarity in the UI code where this method is called.
    fun refreshPatients() {
        Log.d(TAG, "Refreshing patient list")
        // The LiveData will automatically update when the database changes

        // Debug: Log the current patient count
        viewModelScope.launch {
            val patients = repository.getAllPatientsList()
            Log.d(TAG, "Current patient count: ${patients.size}")
            for (patient in patients) {
                Log.d(TAG, "Patient: ${patient.name}, ID: ${patient.patientId}")
            }
        }
    }
}