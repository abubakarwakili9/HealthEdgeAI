package com.example.healthedgeai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.healthedgeai.model.AppDatabase
import com.example.healthedgeai.model.VitalSignsTemplate
import com.example.healthedgeai.repository.VitalSignsTemplateRepository
import kotlinx.coroutines.launch

class VitalSignsTemplateViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VitalSignsTemplateRepository
    val allTemplates: LiveData<List<VitalSignsTemplate>>

    init {
        val dao = AppDatabase.getDatabase(application).vitalSignsTemplateDao()
        repository = VitalSignsTemplateRepository(dao)
        allTemplates = repository.allTemplates
    }

    fun saveTemplate(
        name: String,
        temperature: Float? = null,
        heartRate: Int? = null,
        bloodPressureSystolic: Int? = null,
        bloodPressureDiastolic: Int? = null,
        respirationRate: Int? = null,
        oxygenSaturation: Int? = null,
        bloodGlucose: Float? = null,
        weight: Float? = null,
        height: Float? = null
    ) {
        val template = VitalSignsTemplate(
            name = name,
            temperature = temperature,
            heartRate = heartRate,
            bloodPressureSystolic = bloodPressureSystolic,
            bloodPressureDiastolic = bloodPressureDiastolic,
            respirationRate = respirationRate,
            oxygenSaturation = oxygenSaturation,
            bloodGlucose = bloodGlucose,
            weight = weight,
            height = height
        )

        viewModelScope.launch {
            repository.insertTemplate(template)
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            repository.deleteTemplate(templateId)
        }
    }

    fun getTemplateById(templateId: String, callback: (VitalSignsTemplate?) -> Unit) {
        viewModelScope.launch {
            val template = repository.getTemplateById(templateId)
            callback(template)
        }
    }
}