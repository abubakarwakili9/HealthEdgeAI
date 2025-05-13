package com.example.healthedgeai.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.healthedgeai.databinding.DialogAddPatientBinding
import com.example.healthedgeai.viewmodel.PatientViewModel

class AddPatientDialog : DialogFragment() {
    private val TAG = "AddPatientDialog"

    private var _binding: DialogAddPatientBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PatientViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Initializing AddPatientDialog")
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddPatientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Setting up dialog")

        viewModel = ViewModelProvider(requireActivity())[PatientViewModel::class.java]

        setupGenderDropdown()
        setupListeners()
    }

    private fun setupGenderDropdown() {
        val genders = arrayOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        binding.actvGender.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            Log.d(TAG, "Save button clicked")
            if (validateInputs()) {
                savePatient()
            }
        }

        binding.btnCancel.setOnClickListener {
            Log.d(TAG, "Cancel button clicked")
            dismiss()
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.etName.text.toString().trim()
        val ageText = binding.etAge.text.toString().trim()
        val gender = binding.actvGender.text.toString().trim()
        val contactNumber = binding.etPhone.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            return false
        }

        if (ageText.isEmpty()) {
            binding.etAge.error = "Age is required"
            return false
        }

        if (gender.isEmpty()) {
            binding.actvGender.error = "Gender is required"
            return false
        }

        if (contactNumber.isEmpty()) {
            binding.etPhone.error = "Contact number is required"
            return false
        }

        Log.d(TAG, "Inputs validated successfully")
        return true
    }

    private fun savePatient() {
        try {
            val name = binding.etName.text.toString().trim()
            val age = binding.etAge.text.toString().toInt()
            val gender = binding.actvGender.text.toString().trim()
            val contactNumber = binding.etPhone.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val medicalHistory = binding.etMedicalHistory.text.toString().trim()

            Log.d(TAG, "Adding patient: $name, Age: $age, Gender: $gender")

            viewModel.addPatient(
                name = name,
                age = age,
                gender = gender,
                contactNumber = contactNumber,
                address = address,
                medicalHistory = medicalHistory
            )

            Toast.makeText(requireContext(), "Patient added successfully", Toast.LENGTH_SHORT).show()
            dismiss()

        } catch (e: Exception) {
            Log.e(TAG, "Error saving patient", e)
            Toast.makeText(requireContext(), "Error adding patient: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}