package com.example.healthedgeai.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface VitalSignsTemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: VitalSignsTemplate)

    @Query("SELECT * FROM vital_signs_templates ORDER BY name ASC")
    fun getAllTemplates(): LiveData<List<VitalSignsTemplate>>

    @Query("SELECT * FROM vital_signs_templates ORDER BY name ASC")
    suspend fun getAllTemplatesList(): List<VitalSignsTemplate>

    @Query("SELECT * FROM vital_signs_templates WHERE templateId = :templateId")
    suspend fun getTemplateById(templateId: String): VitalSignsTemplate?

    @Query("DELETE FROM vital_signs_templates WHERE templateId = :templateId")
    suspend fun deleteTemplate(templateId: String)
}