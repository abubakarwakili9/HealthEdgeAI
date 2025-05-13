package com.example.healthedgeai.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PatientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient)

    @Query("SELECT * FROM patients ORDER BY name ASC")
    fun getAllPatients(): LiveData<List<Patient>>

    @Query("SELECT * FROM patients ORDER BY name ASC")
    suspend fun getAllPatientsList(): List<Patient>

    @Query("SELECT * FROM patients WHERE patientId = :patientId")
    fun getPatientById(patientId: String): LiveData<Patient>

    @Query("SELECT * FROM patients WHERE isSynced = 0")
    suspend fun getUnsyncedPatients(): List<Patient>

    @Update
    suspend fun updatePatient(patient: Patient)

    @Query("UPDATE patients SET isSynced = 1 WHERE patientId = :patientId")
    suspend fun markPatientAsSynced(patientId: String)

    @Delete
    suspend fun deletePatient(patient: Patient)
}