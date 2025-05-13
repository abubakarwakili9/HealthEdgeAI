package com.example.healthedgeai.repository

import androidx.lifecycle.LiveData
import com.example.healthedgeai.model.VitalSignsTemplate
import com.example.healthedgeai.model.VitalSignsTemplateDao

class VitalSignsTemplateRepository(private val vitalSignsTemplateDao: VitalSignsTemplateDao) {

    val allTemplates: LiveData<List<VitalSignsTemplate>> = vitalSignsTemplateDao.getAllTemplates()

    suspend fun insertTemplate(template: VitalSignsTemplate) {
        vitalSignsTemplateDao.insertTemplate(template)
    }

    suspend fun getAllTemplatesList(): List<VitalSignsTemplate> {
        return vitalSignsTemplateDao.getAllTemplatesList()
    }

    suspend fun getTemplateById(templateId: String): VitalSignsTemplate? {
        return vitalSignsTemplateDao.getTemplateById(templateId)
    }

    suspend fun deleteTemplate(templateId: String) {
        vitalSignsTemplateDao.deleteTemplate(templateId)
    }
}