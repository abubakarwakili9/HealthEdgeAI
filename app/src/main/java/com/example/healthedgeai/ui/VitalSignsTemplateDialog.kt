package com.example.healthedgeai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.healthedgeai.databinding.DialogVitalSignsTemplateBinding
import com.example.healthedgeai.model.VitalSignsTemplate
import com.example.healthedgeai.viewmodel.VitalSignsTemplateViewModel

class VitalSignsTemplateDialog : DialogFragment() {

    private var _binding: DialogVitalSignsTemplateBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: VitalSignsTemplateViewModel

    // Current values to save or used to populate fields
    private var currentTemperature: Float? = null
    private var currentHeartRate: Int? = null
    private var currentSystolic: Int? = null
    private var currentDiastolic: Int? = null
    private var currentRespirationRate: Int? = null
    private var currentOxygenSaturation: Int? = null
    private var currentBloodGlucose: Float? = null
    private var currentWeight: Float? = null
    private var currentHeight: Float? = null

    private var templates: List<VitalSignsTemplate> = emptyList()
    private var selectedTemplatePosition = -1

    // Callback interface
    interface VitalSignsTemplateListener {
        fun onTemplateSelected(template: VitalSignsTemplate)
    }

    private var listener: VitalSignsTemplateListener? = null

    fun setVitalSignsTemplateListener(listener: VitalSignsTemplateListener) {
        this.listener = listener
    }

    fun setCurrentValues(
        temperature: Float? = null,
        heartRate: Int? = null,
        systolic: Int? = null,
        diastolic: Int? = null,
        respirationRate: Int? = null,
        oxygenSaturation: Int? = null,
        bloodGlucose: Float? = null,
        weight: Float? = null,
        height: Float? = null
    ) {
        currentTemperature = temperature
        currentHeartRate = heartRate
        currentSystolic = systolic
        currentDiastolic = diastolic
        currentRespirationRate = respirationRate
        currentOxygenSaturation = oxygenSaturation
        currentBloodGlucose = bloodGlucose
        currentWeight = weight
        currentHeight = height
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogVitalSignsTemplateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[VitalSignsTemplateViewModel::class.java]

        setupViews()
        setupListeners()
        observeViewModel()
    }

    private fun setupViews() {
        // Initially hide save or load layouts based on user preference
        binding.layoutSave.visibility = View.VISIBLE
        binding.layoutLoad.visibility = View.VISIBLE
    }

    private fun setupListeners() {
        // Save template button
        binding.btnSaveTemplate.setOnClickListener {
            saveTemplate()
        }

        // Spinner selection listener
        binding.spinnerTemplates.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedTemplatePosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedTemplatePosition = -1
            }
        }

        // Load template button
        binding.btnLoad.setOnClickListener {
            loadSelectedTemplate()
        }

        // Delete template button
        binding.btnDelete.setOnClickListener {
            deleteSelectedTemplate()
        }

        // Cancel button
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun observeViewModel() {
        viewModel.allTemplates.observe(viewLifecycleOwner) { templateList ->
            templates = templateList
            updateTemplateSpinner()
        }
    }

    private fun updateTemplateSpinner() {
        if (templates.isEmpty()) {
            binding.tvNoTemplates.visibility = View.VISIBLE
            binding.spinnerTemplates.visibility = View.GONE
            binding.btnLoad.isEnabled = false
            binding.btnDelete.isEnabled = false
        } else {
            binding.tvNoTemplates.visibility = View.GONE
            binding.spinnerTemplates.visibility = View.VISIBLE
            binding.btnLoad.isEnabled = true
            binding.btnDelete.isEnabled = true

            // Create adapter for spinner
            val templateNames = templates.map { it.name }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                templateNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerTemplates.adapter = adapter
        }
    }

    private fun saveTemplate() {
        val templateName = binding.etTemplateName.text.toString().trim()

        if (templateName.isEmpty()) {
            binding.etTemplateName.error = "Template name is required"
            return
        }

        viewModel.saveTemplate(
            name = templateName,
            temperature = currentTemperature,
            heartRate = currentHeartRate,
            bloodPressureSystolic = currentSystolic,
            bloodPressureDiastolic = currentDiastolic,
            respirationRate = currentRespirationRate,
            oxygenSaturation = currentOxygenSaturation,
            bloodGlucose = currentBloodGlucose,
            weight = currentWeight,
            height = currentHeight
        )

        Toast.makeText(requireContext(), "Template saved", Toast.LENGTH_SHORT).show()
        binding.etTemplateName.text?.clear()
    }

    private fun loadSelectedTemplate() {
        if (selectedTemplatePosition >= 0 && selectedTemplatePosition < templates.size) {
            val selectedTemplate = templates[selectedTemplatePosition]
            listener?.onTemplateSelected(selectedTemplate)
            dismiss()
        }
    }

    private fun deleteSelectedTemplate() {
        if (selectedTemplatePosition >= 0 && selectedTemplatePosition < templates.size) {
            val selectedTemplate = templates[selectedTemplatePosition]
            viewModel.deleteTemplate(selectedTemplate.templateId)
            Toast.makeText(requireContext(), "Template deleted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}