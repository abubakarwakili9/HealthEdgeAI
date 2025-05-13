package com.example.healthedgeai.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.healthedgeai.model.Patient
import com.example.healthedgeai.model.PatientDao

class PatientRepository(private val patientDao: PatientDao) {
    private val TAG = "PatientRepository"

    val allPatients: LiveData<List<Patient>> = patientDao.getAllPatients()

    suspend fun insertPatient(patient: Patient) {
        Log.d(TAG, "Inserting patient: ${patient.patientId}")
        patientDao.insertPatient(patient)
    }

    fun getPatientById(patientId: String): LiveData<Patient> {
        Log.d(TAG, "Getting patient with ID: $patientId")
        return patientDao.getPatientById(patientId)
    }

    suspend fun getUnsyncedPatients(): List<Patient> {
        Log.d(TAG, "Getting unsynced patients")
        return patientDao.getUnsyncedPatients()
    }

    suspend fun getAllPatientsList(): List<Patient> {
        Log.d(TAG, "Getting all patients list")
        return patientDao.getAllPatientsList()
    }

    suspend fun updatePatient(patient: Patient) {
        Log.d(TAG, "Updating patient: ${patient.patientId}")
        patientDao.updatePatient(patient)
    }

    suspend fun markPatientAsSynced(patientId: String) {
        Log.d(TAG, "Marking patient as synced: $patientId")
        patientDao.markPatientAsSynced(patientId)
    }

    suspend fun deletePatient(patient: Patient) {
        Log.d(TAG, "Deleting patient: ${patient.patientId}")
        patientDao.deletePatient(patient)
    }
}