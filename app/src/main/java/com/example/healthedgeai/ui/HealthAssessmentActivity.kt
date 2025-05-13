package com.example.healthedgeai.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.healthedgeai.R
import com.example.healthedgeai.databinding.ActivityHealthAssessmentBinding
import com.example.healthedgeai.viewmodel.HealthAssessmentViewModel
import android.view.Menu
import android.view.MenuItem
import com.example.healthedgeai.model.VitalSignsTemplate
import com.example.healthedgeai.util.BiometricDeviceManager
import com.example.healthedgeai.viewmodel.VitalSignsTemplateViewModel
import com.google.android.material.button.MaterialButton

class HealthAssessmentActivity : AppCompatActivity(), VitalSignsTemplateDialog.VitalSignsTemplateListener {
    private val TAG = "HealthAssessmentActivity"

    private lateinit var binding: ActivityHealthAssessmentBinding
    private lateinit var viewModel: HealthAssessmentViewModel
    private lateinit var templateViewModel: VitalSignsTemplateViewModel
    private lateinit var biometricManager: BiometricDeviceManager

    private var patientId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Initializing HealthAssessmentActivity")

        binding = ActivityHealthAssessmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        patientId = intent.getStringExtra("PATIENT_ID")
        if (patientId == null) {
            Log.e(TAG, "No patient ID provided")
            Toast.makeText(this, "Patient ID is required", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d(TAG, "Patient ID: $patientId")

        viewModel = ViewModelProvider(this)[HealthAssessmentViewModel::class.java]
        templateViewModel = ViewModelProvider(this)[VitalSignsTemplateViewModel::class.java]
        biometricManager = BiometricDeviceManager(this)

        setupListeners()
        setupDeviceButtons()
        observeViewModel()

        // Load patient data
        viewModel.loadPatient(patientId!!)
        Log.d(TAG, "Loading patient data for ID: $patientId")
    }

    private fun setupDeviceButtons() {
        Log.d(TAG, "Setting up device connection buttons")

        // Use view binding for included layout
        val deviceButtons = binding.layoutDeviceButtons.root

        // Find the buttons within the included layout
        val btnThermometer = deviceButtons.findViewById<MaterialButton>(R.id.btnConnectThermometer)
        val btnHeartRate = deviceButtons.findViewById<MaterialButton>(R.id.btnConnectHeartRate)
        val btnBloodPressure = deviceButtons.findViewById<MaterialButton>(R.id.btnConnectBloodPressure)
        val btnOxygen = deviceButtons.findViewById<MaterialButton>(R.id.btnConnectOxygen)
        val btnGlucose = deviceButtons.findViewById<MaterialButton>(R.id.btnConnectGlucose)

        btnThermometer?.setOnClickListener {
            Log.d(TAG, "Thermometer button clicked")
            readFromBiometricDevice(BiometricDeviceManager.DeviceType.THERMOMETER)
        }

        btnHeartRate?.setOnClickListener {
            Log.d(TAG, "Heart rate button clicked")
            readFromBiometricDevice(BiometricDeviceManager.DeviceType.HEART_RATE_MONITOR)
        }

        btnBloodPressure?.setOnClickListener {
            Log.d(TAG, "Blood pressure button clicked")
            readFromBiometricDevice(BiometricDeviceManager.DeviceType.BLOOD_PRESSURE_MONITOR)
        }

        btnOxygen?.setOnClickListener {
            Log.d(TAG, "Oxygen saturation button clicked")
            readFromBiometricDevice(BiometricDeviceManager.DeviceType.OXYGEN_SATURATION_MONITOR)
        }

        btnGlucose?.setOnClickListener {
            Log.d(TAG, "Glucose meter button clicked")
            readFromBiometricDevice(BiometricDeviceManager.DeviceType.GLUCOSE_METER)
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Back button clicked, finishing activity")
            finish()
        }

        binding.btnRunAssessment.setOnClickListener {
            Log.d(TAG, "Run Assessment button clicked")
            if (validateInputs()) {
                Log.d(TAG, "Inputs validated, running assessment")
                runAssessment()
            } else {
                Log.d(TAG, "Input validation failed")
            }
        }

        binding.btnSave.setOnClickListener {
            Log.d(TAG, "Save button clicked, saving assessment")
            viewModel.saveCurrentRecord()
            Toast.makeText(this, "Assessment saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun observeViewModel() {
        Log.d(TAG, "Setting up ViewModel observers")

        viewModel.patient.observe(this) { patient ->
            if (patient != null) {
                Log.d(TAG, "Patient data received: ${patient.name}")
                binding.tvPatientName.text = patient.name
                binding.tvPatientDetails.text = "Age: ${patient.age} | Gender: ${patient.gender}"
            } else {
                Log.e(TAG, "Null patient data received")
            }
        }

        viewModel.aiAssessment.observe(this) { assessment ->
            Log.d(TAG, "Assessment result received: $assessment")

            // Hide progress and show result card
            binding.progressBar.visibility = View.GONE
            binding.cardAssessment.visibility = View.VISIBLE
            binding.btnSave.isEnabled = true

            // Show assessment result
            binding.tvAssessmentResult.text = "Assessment Result: $assessment"

            // Generate details based on assessment
            val details = when {
                assessment.contains("Healthy") -> "Patient's vital signs are within normal ranges. Continue regular monitoring."
                assessment.contains("Moderate") -> "Patient shows some concerning vital signs. Consider follow-up within 1-2 weeks."
                assessment.contains("Critical") -> "Patient's condition requires immediate medical attention. Consider urgent referral."
                else -> "Assessment completed. Please review the vital signs manually."
            }

            binding.tvAssessmentDetails.text = details

            // Use an assessment-based color for visual indication
            val cardColor = when {
                assessment.contains("Healthy") -> android.graphics.Color.parseColor("#E8F5E9") // Light green
                assessment.contains("Moderate") -> android.graphics.Color.parseColor("#FFF8E1") // Light amber
                assessment.contains("Critical") -> android.graphics.Color.parseColor("#FFEBEE") // Light red
                else -> android.graphics.Color.parseColor("#F5F5F5") // Light gray
            }
            binding.cardAssessment.setCardBackgroundColor(cardColor)

            // Adjust text color for better readability if needed
            val textColor = android.graphics.Color.parseColor("#212121") // Dark text for all backgrounds
            binding.tvAssessmentResult.setTextColor(textColor)
            binding.tvAssessmentDetails.setTextColor(textColor)

            Log.d(TAG, "Updated UI with assessment: $assessment, card color set accordingly")
        }

        // Observe health alerts
        viewModel.healthAlerts.observe(this) { alerts ->
            Log.d(TAG, "Health alerts received: ${alerts.size} alerts")

            if (alerts.isNotEmpty()) {
                binding.cardAlerts.visibility = View.VISIBLE
                binding.tvAlerts.text = alerts.joinToString("\n\n")
                Log.d(TAG, "Showing health alerts: ${alerts.joinToString(", ")}")
            } else {
                binding.cardAlerts.visibility = View.GONE
                Log.d(TAG, "No health alerts to show")
            }
        }
    }

    private fun validateInputs(): Boolean {
        Log.d(TAG, "Validating input fields")

        val temperatureText = binding.etTemperature.text.toString().trim()
        val heartRateText = binding.etHeartRate.text.toString().trim()
        val bpSystolicText = binding.etBloodPressureSystolic.text.toString().trim()
        val bpDiastolicText = binding.etBloodPressureDiastolic.text.toString().trim()

        if (temperatureText.isEmpty()) {
            Log.d(TAG, "Temperature field is empty")
            binding.etTemperature.error = "Required"
            binding.etTemperature.requestFocus()
            return false
        }

        if (heartRateText.isEmpty()) {
            Log.d(TAG, "Heart rate field is empty")
            binding.etHeartRate.error = "Required"
            binding.etHeartRate.requestFocus()
            return false
        }

        if (bpSystolicText.isEmpty()) {
            Log.d(TAG, "BP Systolic field is empty")
            binding.etBloodPressureSystolic.error = "Required"
            binding.etBloodPressureSystolic.requestFocus()
            return false
        }

        if (bpDiastolicText.isEmpty()) {
            Log.d(TAG, "BP Diastolic field is empty")
            binding.etBloodPressureDiastolic.error = "Required"
            binding.etBloodPressureDiastolic.requestFocus()
            return false
        }

        // Additional validations for reasonable value ranges
        try {
            val temperature = temperatureText.toFloat()
            if (temperature < 30f || temperature > 45f) {
                binding.etTemperature.error = "Invalid range (30-45°C)"
                binding.etTemperature.requestFocus()
                return false
            }

            val heartRate = heartRateText.toInt()
            if (heartRate < 30 || heartRate > 220) {
                binding.etHeartRate.error = "Invalid range (30-220 bpm)"
                binding.etHeartRate.requestFocus()
                return false
            }

            val systolic = bpSystolicText.toInt()
            if (systolic < 70 || systolic > 250) {
                binding.etBloodPressureSystolic.error = "Invalid range (70-250 mmHg)"
                binding.etBloodPressureSystolic.requestFocus()
                return false
            }

            val diastolic = bpDiastolicText.toInt()
            if (diastolic < 40 || diastolic > 150) {
                binding.etBloodPressureDiastolic.error = "Invalid range (40-150 mmHg)"
                binding.etBloodPressureDiastolic.requestFocus()
                return false
            }

            // Check if systolic is greater than diastolic
            if (systolic <= diastolic) {
                binding.etBloodPressureSystolic.error = "Must be greater than diastolic"
                binding.etBloodPressureSystolic.requestFocus()
                return false
            }

        } catch (e: NumberFormatException) {
            Log.e(TAG, "Invalid number format in input fields", e)
            Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show()
            return false
        }

        Log.d(TAG, "All required fields validated successfully")
        return true
    }

    private fun runAssessment() {
        Log.d(TAG, "Running health assessment")

        // Show loading state
        binding.progressBar.visibility = View.VISIBLE
        binding.cardAssessment.visibility = View.GONE
        binding.btnSave.isEnabled = false
        binding.cardAlerts.visibility = View.GONE

        // Read input values
        val temperature = binding.etTemperature.text.toString().toFloatOrNull() ?: 37.0f
        val heartRate = binding.etHeartRate.text.toString().toIntOrNull() ?: 70
        val bloodPressureSystolic = binding.etBloodPressureSystolic.text.toString().toIntOrNull() ?: 120
        val bloodPressureDiastolic = binding.etBloodPressureDiastolic.text.toString().toIntOrNull() ?: 80
        val respirationRate = binding.etRespirationRate.text.toString().toIntOrNull() ?: 16
        val oxygenSaturation = binding.etOxygenSaturation.text.toString().toIntOrNull() ?: 98
        val bloodGlucose = binding.etBloodGlucose.text.toString().toFloatOrNull() ?: 100f
        val weight = binding.etWeight.text.toString().toFloatOrNull() ?: 70f
        val height = binding.etHeight.text.toString().toFloatOrNull() ?: 170f
        val notes = binding.etNotes.text.toString().trim()

        Log.d(TAG, "Input values: temp=$temperature, HR=$heartRate, BP=$bloodPressureSystolic/$bloodPressureDiastolic")

        // Run assessment
        viewModel.addHealthRecord(
            patientId = patientId!!,
            temperature = temperature,
            heartRate = heartRate,
            bloodPressureSystolic = bloodPressureSystolic,
            bloodPressureDiastolic = bloodPressureDiastolic,
            respirationRate = respirationRate,
            oxygenSaturation = oxygenSaturation,
            bloodGlucose = bloodGlucose,
            weight = weight,
            height = height,
            notes = notes
        )

        Log.d(TAG, "Assessment request sent to ViewModel")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity visible to user")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: Activity no longer visible")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, "Creating options menu")
        menuInflater.inflate(R.menu.menu_health_assessment, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_templates -> {
                Log.d(TAG, "Templates menu item selected")
                showTemplatesDialog()
                true
            }
            R.id.action_connect_thermometer -> {
                Log.d(TAG, "Connect thermometer menu item selected")
                readFromBiometricDevice(BiometricDeviceManager.DeviceType.THERMOMETER)
                true
            }
            R.id.action_connect_heart_rate -> {
                Log.d(TAG, "Connect heart rate menu item selected")
                readFromBiometricDevice(BiometricDeviceManager.DeviceType.HEART_RATE_MONITOR)
                true
            }
            R.id.action_connect_blood_pressure -> {
                Log.d(TAG, "Connect blood pressure menu item selected")
                readFromBiometricDevice(BiometricDeviceManager.DeviceType.BLOOD_PRESSURE_MONITOR)
                true
            }
            R.id.action_connect_oxygen -> {
                Log.d(TAG, "Connect oxygen saturation menu item selected")
                readFromBiometricDevice(BiometricDeviceManager.DeviceType.OXYGEN_SATURATION_MONITOR)
                true
            }
            R.id.action_connect_glucose -> {
                Log.d(TAG, "Connect glucose meter menu item selected")
                readFromBiometricDevice(BiometricDeviceManager.DeviceType.GLUCOSE_METER)
                true
            }
            android.R.id.home -> {
                Log.d(TAG, "Home/Up button selected, finishing activity")
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Shows the template dialog for both saving and loading templates
     */
    private fun showTemplatesDialog() {
        Log.d(TAG, "Showing templates dialog")
        val dialog = VitalSignsTemplateDialog()
        dialog.setVitalSignsTemplateListener(this)

        // Set current values from the form
        dialog.setCurrentValues(
            temperature = binding.etTemperature.text.toString().toFloatOrNull(),
            heartRate = binding.etHeartRate.text.toString().toIntOrNull(),
            systolic = binding.etBloodPressureSystolic.text.toString().toIntOrNull(),
            diastolic = binding.etBloodPressureDiastolic.text.toString().toIntOrNull(),
            respirationRate = binding.etRespirationRate.text.toString().toIntOrNull(),
            oxygenSaturation = binding.etOxygenSaturation.text.toString().toIntOrNull(),
            bloodGlucose = binding.etBloodGlucose.text.toString().toFloatOrNull(),
            weight = binding.etWeight.text.toString().toFloatOrNull(),
            height = binding.etHeight.text.toString().toFloatOrNull()
        )

        dialog.show(supportFragmentManager, "VitalSignsTemplateDialog")
    }

    /**
     * Callback implementation from VitalSignsTemplateDialog.VitalSignsTemplateListener
     */
    override fun onTemplateSelected(template: VitalSignsTemplate) {
        Log.d(TAG, "Template selected: ${template.name}")

        // Populate form with template values
        template.temperature?.let {
            binding.etTemperature.setText(it.toString())
            Log.d(TAG, "Setting temperature from template: $it")
        }
        template.heartRate?.let {
            binding.etHeartRate.setText(it.toString())
            Log.d(TAG, "Setting heart rate from template: $it")
        }
        template.bloodPressureSystolic?.let {
            binding.etBloodPressureSystolic.setText(it.toString())
            Log.d(TAG, "Setting systolic BP from template: $it")
        }
        template.bloodPressureDiastolic?.let {
            binding.etBloodPressureDiastolic.setText(it.toString())
            Log.d(TAG, "Setting diastolic BP from template: $it")
        }
        template.respirationRate?.let {
            binding.etRespirationRate.setText(it.toString())
            Log.d(TAG, "Setting respiration rate from template: $it")
        }
        template.oxygenSaturation?.let {
            binding.etOxygenSaturation.setText(it.toString())
            Log.d(TAG, "Setting oxygen saturation from template: $it")
        }
        template.bloodGlucose?.let {
            binding.etBloodGlucose.setText(it.toString())
            Log.d(TAG, "Setting blood glucose from template: $it")
        }
        template.weight?.let {
            binding.etWeight.setText(it.toString())
            Log.d(TAG, "Setting weight from template: $it")
        }
        template.height?.let {
            binding.etHeight.setText(it.toString())
            Log.d(TAG, "Setting height from template: $it")
        }

        Toast.makeText(this, "Template '${template.name}' loaded", Toast.LENGTH_SHORT).show()
    }

    /**
     * Reads data from a biometric device
     */
    private fun readFromBiometricDevice(deviceType: BiometricDeviceManager.DeviceType) {
        Log.d(TAG, "Reading from biometric device: ${deviceType.name}")
        Toast.makeText(this, "Connecting to ${deviceType.name}...", Toast.LENGTH_SHORT).show()

        biometricManager.startReading(deviceType, object : BiometricDeviceManager.ReadingCallback {
            override fun onReadingReceived(deviceType: BiometricDeviceManager.DeviceType, value: Float, secondaryValue: Float?) {
                runOnUiThread {
                    when (deviceType) {
                        BiometricDeviceManager.DeviceType.THERMOMETER -> {
                            binding.etTemperature.setText(String.format("%.1f", value))
                            Log.d(TAG, "Temperature reading received: $value°C")
                            Toast.makeText(this@HealthAssessmentActivity, "Temperature reading: ${String.format("%.1f", value)}°C", Toast.LENGTH_SHORT).show()
                        }
                        BiometricDeviceManager.DeviceType.HEART_RATE_MONITOR -> {
                            binding.etHeartRate.setText(value.toInt().toString())
                            Log.d(TAG, "Heart rate reading received: ${value.toInt()} bpm")
                            Toast.makeText(this@HealthAssessmentActivity, "Heart rate reading: ${value.toInt()} bpm", Toast.LENGTH_SHORT).show()
                        }
                        BiometricDeviceManager.DeviceType.BLOOD_PRESSURE_MONITOR -> {
                            if (secondaryValue != null) {
                                binding.etBloodPressureSystolic.setText(value.toInt().toString())
                                binding.etBloodPressureDiastolic.setText(secondaryValue.toInt().toString())
                                Log.d(TAG, "Blood pressure reading received: ${value.toInt()}/${secondaryValue.toInt()} mmHg")
                                Toast.makeText(this@HealthAssessmentActivity, "Blood pressure reading: ${value.toInt()}/${secondaryValue.toInt()} mmHg", Toast.LENGTH_SHORT).show()
                            }
                        }
                        BiometricDeviceManager.DeviceType.OXYGEN_SATURATION_MONITOR -> {
                            binding.etOxygenSaturation.setText(value.toInt().toString())
                            Log.d(TAG, "Oxygen saturation reading received: ${value.toInt()}%")
                            Toast.makeText(this@HealthAssessmentActivity, "Oxygen saturation reading: ${value.toInt()}%", Toast.LENGTH_SHORT).show()
                        }
                        BiometricDeviceManager.DeviceType.GLUCOSE_METER -> {
                            binding.etBloodGlucose.setText(String.format("%.1f", value))
                            Log.d(TAG, "Blood glucose reading received: $value mg/dL")
                            Toast.makeText(this@HealthAssessmentActivity, "Blood glucose reading: ${String.format("%.1f", value)} mg/dL", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onError(deviceType: BiometricDeviceManager.DeviceType, message: String) {
                Log.e(TAG, "Error reading from device: $message")
                runOnUiThread {
                    Toast.makeText(this@HealthAssessmentActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}