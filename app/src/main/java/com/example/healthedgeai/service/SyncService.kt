package com.example.healthedgeai.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.healthedgeai.model.AppDatabase
import com.example.healthedgeai.repository.HealthRecordRepository
import com.example.healthedgeai.repository.PatientRepository
import kotlinx.coroutines.*

class SyncService : Service() {

    private val TAG = "SyncService"
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var patientRepository: PatientRepository
    private lateinit var healthRecordRepository: HealthRecordRepository

    companion object {
        private val _syncStatus = MutableLiveData<SyncStatus>()
        val syncStatus: LiveData<SyncStatus> = _syncStatus

        enum class SyncStatus {
            IDLE,
            SYNCING,
            SUCCESS,
            ERROR
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        // Initialize repositories
        val database = AppDatabase.getDatabase(this)
        patientRepository = PatientRepository(database.patientDao())
        healthRecordRepository = HealthRecordRepository(database.healthRecordDao())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        // Start the sync process
        _syncStatus.postValue(SyncStatus.SYNCING)

        serviceScope.launch {
            try {
                syncData()
                _syncStatus.postValue(SyncStatus.SUCCESS)
            } catch (e: Exception) {
                Log.e(TAG, "Error during sync", e)
                _syncStatus.postValue(SyncStatus.ERROR)
            } finally {
                // Stop the service when sync is complete
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun syncData() {
        Log.d(TAG, "Starting data sync")

        // Simulate network delay
        delay(2000)

        // 1. Sync unsynced patients
        val patients = patientRepository.getUnsyncedPatients()
        Log.d(TAG, "Found ${patients.size} unsynced patients")

        patients.forEach { patient ->
            // Simulate sending patient data to server
            simulateNetworkRequest(patient.patientId)

            // Mark patient as synced
            patientRepository.markPatientAsSynced(patient.patientId)
            Log.d(TAG, "Patient synced: ${patient.name}")
        }

        // 2. Sync unsynced health records
        val healthRecords = healthRecordRepository.getUnsyncedHealthRecords()
        Log.d(TAG, "Found ${healthRecords.size} unsynced health records")

        healthRecords.forEach { record ->
            // Simulate sending health record data to server
            simulateNetworkRequest(record.recordId)

            // Mark health record as synced
            healthRecordRepository.markHealthRecordAsSynced(record.recordId)
            Log.d(TAG, "Health record synced: ${record.recordId}")
        }

        Log.d(TAG, "Data sync completed")
    }

    private suspend fun simulateNetworkRequest(id: String) {
        // Simulate a network request
        Log.d(TAG, "Sending data for ID: $id")
        delay(500) // Simulate network delay
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        // Cancel all coroutines when the service is destroyed
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")

        // Reset sync status to idle
        _syncStatus.postValue(SyncStatus.IDLE)
    }
}