package com.example.healthedgeai.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Utility class for saving and loading vital signs templates.
 * This allows healthcare workers to save common sets of vital signs for quick entry.
 */
class VitalSignsTemplates(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vital_templates", Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Save a template with the given name
     * @param name The name to save the template under
     * @param template The vital signs template to save
     */
    fun saveTemplate(name: String, template: VitalSignsTemplate) {
        val json = gson.toJson(template)
        prefs.edit().putString(name, json).apply()
    }

    /**
     * Get a template by its name
     * @param name The name of the template to retrieve
     * @return The template, or null if no template with that name exists
     */
    fun getTemplate(name: String): VitalSignsTemplate? {
        val json = prefs.getString(name, null) ?: return null
        return gson.fromJson(json, VitalSignsTemplate::class.java)
    }

    /**
     * Get all saved template names
     * @return A list of all saved template names
     */
    fun getAllTemplateNames(): List<String> {
        return prefs.all.keys.toList()
    }

    /**
     * Delete a template by its name
     * @param name The name of the template to delete
     */
    fun deleteTemplate(name: String) {
        prefs.edit().remove(name).apply()
    }

    /**
     * Data class representing a set of vital signs that can be reused
     * Default values are provided for normal vital signs
     */
    data class VitalSignsTemplate(
        val temperature: Float = 37.0f,
        val heartRate: Int = 72,
        val bloodPressureSystolic: Int = 120,
        val bloodPressureDiastolic: Int = 80,
        val respirationRate: Int = 16,
        val oxygenSaturation: Int = 98,
        val bloodGlucose: Float = 100f,
        val weight: Float? = null,
        val height: Float? = null
    )
}