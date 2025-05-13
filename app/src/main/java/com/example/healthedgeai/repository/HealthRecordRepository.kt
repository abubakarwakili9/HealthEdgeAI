package com.example.healthedgeai.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.healthedgeai.model.HealthRecord
import com.example.healthedgeai.model.HealthRecordDao

class HealthRecordRepository(private val healthRecordDao: HealthRecordDao) {
    private val TAG = "HealthRecordRepository"

    suspend fun insertHealthRecord(healthRecord: HealthRecord) {
        Log.d(TAG, "Inserting health record: ${healthRecord.recordId}")
        healthRecordDao.insertHealthRecord(healthRecord)
    }

    fun getHealthRecordsForPatient(patientId: String): LiveData<List<HealthRecord>> {
        Log.d(TAG, "Getting health records for patient: $patientId")
        return healthRecordDao.getHealthRecordsForPatient(patientId)
    }

    fun getHealthRecordById(recordId: String): LiveData<HealthRecord> {
        Log.d(TAG, "Getting health record by ID: $recordId")
        return healthRecordDao.getHealthRecordById(recordId)
    }

    // New synchronous method for direct access
    suspend fun getHealthRecordByIdSync(recordId: String): HealthRecord? {
        Log.d(TAG, "Getting health record by ID synchronously: $recordId")
        return healthRecordDao.getHealthRecordByIdSync(recordId)
    }

    suspend fun getUnsyncedHealthRecords(): List<HealthRecord> {
        Log.d(TAG, "Getting unsynced health records")
        return healthRecordDao.getUnsyncedHealthRecords()
    }

    suspend fun updateHealthRecord(healthRecord: HealthRecord) {
        Log.d(TAG, "Updating health record: ${healthRecord.recordId}")
        healthRecordDao.updateHealthRecord(healthRecord)
    }

    suspend fun markHealthRecordAsSynced(recordId: String) {
        Log.d(TAG, "Marking health record as synced: $recordId")
        healthRecordDao.markHealthRecordAsSynced(recordId)
    }

    suspend fun deleteHealthRecord(healthRecord: HealthRecord) {
        Log.d(TAG, "Deleting health record: ${healthRecord.recordId}")
        healthRecordDao.deleteHealthRecord(healthRecord)
    }
}