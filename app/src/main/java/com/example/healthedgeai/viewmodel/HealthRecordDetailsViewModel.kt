package com.example.healthedgeai.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.healthedgeai.model.AppDatabase
import com.example.healthedgeai.model.HealthRecord
import com.example.healthedgeai.repository.HealthRecordRepository

class HealthRecordDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "HealthRecordDetailsVM"

    private val repository: HealthRecordRepository

    private val _healthRecord = MutableLiveData<HealthRecord>()
    val healthRecord: LiveData<HealthRecord> = _healthRecord

    init {
        Log.d(TAG, "Initializing HealthRecordDetailsViewModel")
        val healthRecordDao = AppDatabase.getDatabase(application).healthRecordDao()
        repository = HealthRecordRepository(healthRecordDao)
    }

    fun loadHealthRecord(recordId: String) {
        Log.d(TAG, "Loading health record with ID: $recordId")
        repository.getHealthRecordById(recordId).observeForever { record ->
            _healthRecord.value = record
            if (record != null) {
                Log.d(TAG, "Health record loaded successfully")
            } else {
                Log.e(TAG, "Failed to load health record")
            }
        }
    }

    suspend fun getHealthRecordById(recordId: String): HealthRecord? {
        return repository.getHealthRecordByIdSync(recordId)
    }
}