package com.example.healthedgeai.repository

import androidx.lifecycle.LiveData
import com.example.healthedgeai.model.VitalSignsTemplate
import com.example.healthedgeai.model.VitalSignsTemplateDao
import kotlinx.coroutines.flow.Flow

class VitalSignsTemplateRepository(private val templateDao: VitalSignsTemplateDao) {

    // Get all templates as LiveData
    val allTemplates: LiveData<List<VitalSignsTemplate>> = templateDao.getAllTemplates()

    // Get template by ID - returns LiveData
    fun getTemplateById(templateId: String): LiveData<VitalSignsTemplate> {
        return templateDao.getTemplateById(templateId)
    }

    // Get template by ID as a suspend function (not LiveData) if needed
    suspend fun getTemplateByIdSync(templateId: String): VitalSignsTemplate? {
        return templateDao.getTemplateById(templateId).value
    }

    // Insert or update a template
    suspend fun insertTemplate(template: VitalSignsTemplate) {
        templateDao.insertTemplate(template)
    }

    // Update an existing template
    suspend fun updateTemplate(template: VitalSignsTemplate) {
        templateDao.updateTemplate(template)
    }

    // Delete a template by ID
    suspend fun deleteTemplate(templateId: String) {
        // We need to handle this differently since the DAO expects a VitalSignsTemplate object
        val template = templateDao.getTemplateById(templateId).value
        if (template != null) {
            templateDao.deleteTemplate(template)
        } else {
            // If the template doesn't exist, we can't delete it
            // You might want to add logging here
        }
    }

    // Alternative delete method if you added the deleteTemplateById method to the DAO
    suspend fun deleteTemplateDirectly(templateId: String) {
        templateDao.deleteTemplateById(templateId)
    }
}