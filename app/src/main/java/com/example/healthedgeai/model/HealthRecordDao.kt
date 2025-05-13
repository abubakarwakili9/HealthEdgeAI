package com.example.healthedgeai.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface HealthRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecord(healthRecord: HealthRecord)

    @Query("SELECT * FROM health_records WHERE patientId = :patientId ORDER BY timestamp DESC")
    fun getHealthRecordsForPatient(patientId: String): LiveData<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE recordId = :recordId")
    fun getHealthRecordById(recordId: String): LiveData<HealthRecord>

    @Query("SELECT * FROM health_records WHERE recordId = :recordId")
    suspend fun getHealthRecordByIdSync(recordId: String): HealthRecord?

    @Query("SELECT * FROM health_records WHERE isSynced = 0")
    suspend fun getUnsyncedHealthRecords(): List<HealthRecord>

    @Update
    suspend fun updateHealthRecord(healthRecord: HealthRecord)

    @Query("UPDATE health_records SET isSynced = 1 WHERE recordId = :recordId")
    suspend fun markHealthRecordAsSynced(recordId: String)

    @Delete
    suspend fun deleteHealthRecord(healthRecord: HealthRecord)
}