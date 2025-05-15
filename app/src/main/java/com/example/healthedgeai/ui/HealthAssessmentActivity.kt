package com.example.healthedgeai.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.healthedgeai.R
import com.example.healthedgeai.databinding.ActivityHealthAssessmentBinding
import com.example.healthedgeai.model.VitalSignsTemplate
import com.example.healthedgeai.util.BiometricDeviceManager
import com.example.healthedgeai.util.BluetoothConnectionManager
import com.example.healthedgeai.util.BluetoothDeviceManager
import com.example.healthedgeai.util.BluetoothSimulator
import com.example.healthedgeai.util.HealthDataParser
import com.example.healthedgeai.viewmodel.HealthAssessmentViewModel
import com.example.healthedgeai.viewmodel.VitalSignsTemplateViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class HealthAssessmentActivity : AppCompatActivity(), VitalSignsTemplateDialog.VitalSignsTemplateListener {
    private val TAG = "HealthAssessmentActivity"

    private lateinit var binding: ActivityHealthAssessmentBinding
    private lateinit var viewModel: HealthAssessmentViewModel
    private lateinit var templateViewModel: VitalSignsTemplateViewModel
    private lateinit var biometricManager: BiometricDeviceManager

    // Bluetooth related properties
    private lateinit var bluetoothDeviceManager: BluetoothDeviceManager
    private lateinit var bluetoothConnectionManager: BluetoothConnectionManager
    private var connectedDeviceAddress: String? = null
    private val REQUEST_SELECT_DEVICE = 1001
    private val REQUEST_ENABLE_BT = 1002

    // Bluetooth simulator
    private lateinit var bluetoothSimulator: BluetoothSimulator
    private var simulationActive = false

    // Connection timeout handler
    private val connectionTimeoutHandler = Handler(Looper.getMainLooper())
    private var connectionTimeoutRunnable: Runnable? = null
    private val CONNECTION_TIMEOUT = 15000L // 15 seconds

    // Add debug flag for testing
    private val isDebugMode = true

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

        // Initialize Bluetooth components
        setupBluetoothManagers()

        // Setup other UI components
        setupListeners()
        setupDeviceButtons()
        observeViewModel()

        // Load patient data
        viewModel.loadPatient(patientId!!)
        Log.d(TAG, "Loading patient data for ID: $patientId")

        // Test Bluetooth setup if in debug mode
        if (isDebugMode) {
            lifecycleScope.launch {
                delay(1000) // Wait for UI to be ready
                testBluetoothSetup()
            }
        }
    }

    private fun setupBluetoothManagers() {
        Log.d(TAG, "Setting up Bluetooth managers")

        try {
            // Initialize Bluetooth managers
            bluetoothDeviceManager = BluetoothDeviceManager(this)
            bluetoothConnectionManager = BluetoothConnectionManager(this)

            // Set up simulator
            bluetoothSimulator = BluetoothSimulator { serviceUuid, characteristicUuid, data ->
                processReceivedData(serviceUuid, characteristicUuid, data)
            }

            // Set up connection status UI
            updateConnectionUi(BluetoothConnectionManager.ConnectionState.DISCONNECTED)

            // Set up connect button
            binding.btnConnect.setOnClickListener {
                Log.d(TAG, "Connect button clicked")

                if (bluetoothConnectionManager.getConnectionState() == BluetoothConnectionManager.ConnectionState.CONNECTED) {
                    // Disconnect if already connected
                    updateDebugInfo("Initiating disconnect...")
                    bluetoothConnectionManager.disconnect()
                } else {
                    // Check Bluetooth state before proceeding
                    if (!checkBluetoothBeforeConnection()) {
                        return@setOnClickListener
                    }

                    // Show the progress indicator
                    binding.progressConnect.visibility = View.VISIBLE
                    binding.btnConnect.isEnabled = false
                    updateDebugInfo("Preparing for device scan...")

                    // Use a coroutine to prevent UI thread blocking
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            // Prepare for scan on background thread
                            Log.d(TAG, "Preparing to launch device scan activity")

                            // Switch to main thread to start the activity
                            withContext(Dispatchers.Main) {
                                val intent = Intent(this@HealthAssessmentActivity, DeviceScanActivity::class.java)
                                startActivityForResult(intent, REQUEST_SELECT_DEVICE)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error launching scan activity", e)
                            withContext(Dispatchers.Main) {
                                binding.progressConnect.visibility = View.GONE
                                binding.btnConnect.isEnabled = true
                                updateDebugInfo("Error: ${e.message}")
                                Toast.makeText(
                                    this@HealthAssessmentActivity,
                                    "Error launching scan: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }

            // Set up simulation button
            binding.btnSimulate.setOnClickListener {
                if (simulationActive) {
                    // Stop simulation
                    bluetoothSimulator.stop()
                    simulationActive = false
                    binding.btnSimulate.text = "Simulate"
                    updateDebugInfo("Simulation stopped")
                    Toast.makeText(this, "Simulation stopped", Toast.LENGTH_SHORT).show()
                } else {
                    // Start simulation
                    bluetoothSimulator.start()
                    simulationActive = true
                    binding.btnSimulate.text = "Stop Sim"
                    updateDebugInfo("Simulation started")
                    Toast.makeText(this, "Simulation started", Toast.LENGTH_SHORT).show()
                }
            }

            // Check for previously connected device
            val lastDevice = bluetoothDeviceManager.getLastConnectedDevice()
            if (lastDevice != null) {
                val (address, name) = lastDevice
                binding.tvConnectionStatus.text = "Last device: $name"
                updateDebugInfo("Last device: $name")
            }

            Log.d(TAG, "Bluetooth managers setup complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up Bluetooth managers", e)
            updateDebugInfo("Error: ${e.message}")
            Toast.makeText(this, "Error setting up Bluetooth: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Check Bluetooth state and permissions before connection
     */
    private fun checkBluetoothBeforeConnection(): Boolean {
        try {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter

            if (bluetoothAdapter == null) {
                updateDebugInfo("Device does not support Bluetooth")
                Toast.makeText(this, "This device does not support Bluetooth", Toast.LENGTH_SHORT).show()
                return false
            }

            if (!bluetoothAdapter.isEnabled) {
                updateDebugInfo("Bluetooth is disabled, requesting enable")
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                return false
            }

            // Check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) !=
                    PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) !=
                    PackageManager.PERMISSION_GRANTED) {

                    updateDebugInfo("Missing Bluetooth permissions")
                    Toast.makeText(this, "Bluetooth permissions required", Toast.LENGTH_SHORT).show()
                    return false
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {

                    updateDebugInfo("Missing location permission for Bluetooth")
                    Toast.makeText(this, "Location permission required for Bluetooth", Toast.LENGTH_SHORT).show()
                    return false
                }
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Bluetooth state", e)
            updateDebugInfo("Error checking Bluetooth: ${e.message}")
            Toast.makeText(this, "Error checking Bluetooth: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    private fun updateConnectionUi(state: BluetoothConnectionManager.ConnectionState) {
        Log.d(TAG, "Updating connection UI to state: $state")

        binding.connectionStatusView.connectionState = state

        when (state) {
            BluetoothConnectionManager.ConnectionState.CONNECTED -> {
                val device = bluetoothConnectionManager.getConnectedDevice()
                val deviceName = device?.name ?: "Unknown Device"
                binding.tvConnectionStatus.text = "Connected to $deviceName"
                binding.btnConnect.text = "Disconnect"
                binding.btnConnect.isEnabled = true
                binding.progressConnect.visibility = View.GONE
                updateDebugInfo("Connected to $deviceName")
            }
            BluetoothConnectionManager.ConnectionState.CONNECTING -> {
                binding.tvConnectionStatus.text = "Connecting..."
                binding.btnConnect.isEnabled = false
                binding.progressConnect.visibility = View.VISIBLE
                updateDebugInfo("Connecting...")
            }
            BluetoothConnectionManager.ConnectionState.DISCONNECTING -> {
                binding.tvConnectionStatus.text = "Disconnecting..."
                binding.btnConnect.isEnabled = false
                binding.progressConnect.visibility = View.VISIBLE
                updateDebugInfo("Disconnecting...")
            }
            BluetoothConnectionManager.ConnectionState.DISCONNECTED -> {
                binding.tvConnectionStatus.text = "Disconnected"
                binding.btnConnect.text = "Connect"
                binding.btnConnect.isEnabled = true
                binding.progressConnect.visibility = View.GONE
                updateDebugInfo("Disconnected")

                // Cancel any pending connection timeouts
                cancelConnectionTimeout()
            }
        }
    }

    private fun updateDebugInfo(message: String) {
        Log.d(TAG, "Debug: $message")
        try {
            // Use Handler to ensure UI updates happen on the main thread
            Handler(Looper.getMainLooper()).post {
                binding.tvDebugInfo.text = "Debug: $message"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating debug info", e)
        }
    }

    private fun setupDeviceButtons() {
        Log.d(TAG, "Setting up device connection buttons")

        try {
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
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up device buttons", e)
            updateDebugInfo("Error setting up device buttons: ${e.message}")
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

        try {
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
        } catch (e: Exception) {
            Log.e(TAG, "Error running assessment", e)
            binding.progressBar.visibility = View.GONE
            updateDebugInfo("Assessment error: ${e.message}")
            Toast.makeText(this, "Error running assessment: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Set connection timeout
    private fun setConnectionTimeout(device: BluetoothDevice) {
        // Cancel any existing timeout
        cancelConnectionTimeout()

        // Create new timeout runnable
        connectionTimeoutRunnable = Runnable {
            Log.e(TAG, "Connection to ${device.address} timed out after ${CONNECTION_TIMEOUT}ms")

            // If still in connecting state
            if (bluetoothConnectionManager.getConnectionState() == BluetoothConnectionManager.ConnectionState.CONNECTING) {
                // Force disconnect
                bluetoothConnectionManager.close()

                // Update UI
                runOnUiThread {
                    Toast.makeText(this@HealthAssessmentActivity,
                        "Connection timed out",
                        Toast.LENGTH_SHORT).show()
                    updateDebugInfo("Connection timed out")
                    updateConnectionUi(BluetoothConnectionManager.ConnectionState.DISCONNECTED)
                }
            }
        }

        // Schedule the timeout
        connectionTimeoutRunnable?.let {
            connectionTimeoutHandler.postDelayed(it, CONNECTION_TIMEOUT)
        }

        Log.d(TAG, "Connection timeout set for ${CONNECTION_TIMEOUT}ms")
    }

    // Cancel connection timeout
    private fun cancelConnectionTimeout() {
        connectionTimeoutRunnable?.let {
            connectionTimeoutHandler.removeCallbacks(it)
            connectionTimeoutRunnable = null
            Log.d(TAG, "Connection timeout canceled")
        }
    }

    // Handle device connection
    private fun connectToDevice(address: String) {
        updateDebugInfo("Connecting to $address...")

        // Show connecting state immediately
        updateConnectionUi(BluetoothConnectionManager.ConnectionState.CONNECTING)

        // Launch the connection on a background thread to prevent UI freezing
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val bluetoothAdapter = bluetoothManager.adapter

                if (bluetoothAdapter == null) {
                    withContext(Dispatchers.Main) {
                        updateDebugInfo("Bluetooth not available")
                        Toast.makeText(this@HealthAssessmentActivity,
                            "Bluetooth not available",
                            Toast.LENGTH_SHORT).show()
                        updateConnectionUi(BluetoothConnectionManager.ConnectionState.DISCONNECTED)
                    }
                    return@launch
                }

                if (!bluetoothAdapter.isEnabled) {
                    withContext(Dispatchers.Main) {
                        updateDebugInfo("Bluetooth is disabled")
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                        updateConnectionUi(BluetoothConnectionManager.ConnectionState.DISCONNECTED)
                    }
                    return@launch
                }

                try {
                    val device = bluetoothAdapter.getRemoteDevice(address)
                    Log.d(TAG, "Initiating connection to ${device.address}")

                    // Set connection timeout
                    withContext(Dispatchers.Main) {
                        setConnectionTimeout(device)
                    }

                    // Connect to the device
                    val success = bluetoothConnectionManager.connect(device, object : BluetoothConnectionManager.ConnectionCallback {
                        override fun onConnectionStateChange(device: BluetoothDevice, state: BluetoothConnectionManager.ConnectionState) {
                            Log.d(TAG, "Connection state changed to: $state")

                            // Cancel timeout on successful connection or disconnect
                            if (state == BluetoothConnectionManager.ConnectionState.CONNECTED ||
                                state == BluetoothConnectionManager.ConnectionState.DISCONNECTED) {
                                cancelConnectionTimeout()
                            }

                            runOnUiThread {
                                updateConnectionUi(state)

                                // Show toast for state changes
                                when (state) {
                                    BluetoothConnectionManager.ConnectionState.CONNECTED ->
                                        Toast.makeText(this@HealthAssessmentActivity,
                                            "Connected to ${device.name ?: device.address}",
                                            Toast.LENGTH_SHORT).show()

                                    BluetoothConnectionManager.ConnectionState.DISCONNECTED ->
                                        Toast.makeText(this@HealthAssessmentActivity,
                                            "Disconnected from device",
                                            Toast.LENGTH_SHORT).show()

                                    else -> { /* No toast for intermediate states */ }
                                }
                            }
                        }

                        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                            Log.d(TAG, "Services discovered, status: $status")

                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                updateDebugInfo("Services discovered, enabling notifications...")
                                // Enable notifications for healthcare services
                                setupHealthNotifications(gatt)
                            } else {
                                updateDebugInfo("Service discovery failed: $status")
                            }
                        }

                        override fun onDataReceived(serviceUuid: UUID, characteristicUuid: UUID, data: ByteArray) {
                            Log.d(TAG, "Data received from service: $serviceUuid")
                            updateDebugInfo("Data received: ${data.size} bytes")
                            // Process received data
                            processReceivedData(serviceUuid, characteristicUuid, data)
                        }
                    })

                    if (!success) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@HealthAssessmentActivity,
                                "Failed to start connection process",
                                Toast.LENGTH_SHORT).show()
                            updateConnectionUi(BluetoothConnectionManager.ConnectionState.DISCONNECTED)
                        }
                    }
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Invalid Bluetooth address format", e)
                    withContext(Dispatchers.Main) {
                        updateDebugInfo("Invalid device address format")
                        Toast.makeText(this@HealthAssessmentActivity,
                            "Invalid device address format",
                            Toast.LENGTH_SHORT).show()
                        updateConnectionUi(BluetoothConnectionManager.ConnectionState.DISCONNECTED)
                    }
                }
            } catch (securityException: SecurityException) {
                Log.e(TAG, "Bluetooth permission denied", securityException)
                withContext(Dispatchers.Main) {
                    updateDebugInfo("Permission denied: ${securityException.message}")
                    Toast.makeText(this@HealthAssessmentActivity,
                        "Bluetooth permission denied",
                        Toast.LENGTH_SHORT).show()
                    updateConnectionUi(BluetoothConnectionManager.ConnectionState.DISCONNECTED)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to device", e)
                withContext(Dispatchers.Main) {
                    updateDebugInfo("Connection error: ${e.message}")
                    Toast.makeText(this@HealthAssessmentActivity,
                        "Connection error: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                    updateConnectionUi(BluetoothConnectionManager.ConnectionState.DISCONNECTED)
                }
            }
        }
    }

    // Set up notifications for health characteristics
    private fun setupHealthNotifications(gatt: BluetoothGatt) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Setting up health notifications")

                // Heart Rate
                val hrSuccess = bluetoothConnectionManager.enableNotifications(
                    BluetoothDeviceManager.HEART_RATE_SERVICE_UUID,
                    UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
                )
                Log.d(TAG, "Heart rate notifications enabled: $hrSuccess")

                // Blood Pressure
                val bpSuccess = bluetoothConnectionManager.enableNotifications(
                    BluetoothDeviceManager.BLOOD_PRESSURE_SERVICE_UUID,
                    UUID.fromString("00002a35-0000-1000-8000-00805f9b34fb")
                )
                Log.d(TAG, "Blood pressure notifications enabled: $bpSuccess")

                // Temperature
                val tempSuccess = bluetoothConnectionManager.enableNotifications(
                    BluetoothDeviceManager.THERMOMETER_SERVICE_UUID,
                    UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb")
                )
                Log.d(TAG, "Temperature notifications enabled: $tempSuccess")

                // Glucose
                val glucoseSuccess = bluetoothConnectionManager.enableNotifications(
                    BluetoothDeviceManager.GLUCOSE_SERVICE_UUID,
                    UUID.fromString("00002a18-0000-1000-8000-00805f9b34fb")
                )
                Log.d(TAG, "Glucose notifications enabled: $glucoseSuccess")

                // SpO2
                val spo2Success = bluetoothConnectionManager.enableNotifications(
                    BluetoothDeviceManager.PULSE_OXIMETER_SERVICE_UUID,
                    UUID.fromString("00002a5e-0000-1000-8000-00805f9b34fb")
                )
                Log.d(TAG, "SpO2 notifications enabled: $spo2Success")

                withContext(Dispatchers.Main) {
                    updateDebugInfo("Notifications setup complete")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up notifications", e)
                withContext(Dispatchers.Main) {
                    updateDebugInfo("Notification setup error: ${e.message}")
                }
            }
        }
    }

    // Process data from healthcare device
    private fun processReceivedData(serviceUuid: UUID, characteristicUuid: UUID, data: ByteArray) {
        try {
            val dataType = HealthDataParser.identifyDataType(serviceUuid, characteristicUuid)
            Log.d(TAG, "Processing data of type: $dataType")

            runOnUiThread {
                when (dataType) {
                    HealthDataParser.DataType.HEART_RATE -> {
                        val heartRate = HealthDataParser.parseHeartRate(data)
                        if (heartRate != null) {
                            Log.d(TAG, "Heart rate data received: $heartRate bpm")
                            binding.etHeartRate.setText(heartRate.toString())
                            showReceivedDataSnackbar("Heart rate: $heartRate bpm")
                        }
                    }
                    HealthDataParser.DataType.BLOOD_PRESSURE -> {
                        val bloodPressure = HealthDataParser.parseBloodPressure(data)
                        if (bloodPressure != null) {
                            val (systolic, diastolic) = bloodPressure
                            Log.d(TAG, "Blood pressure data received: ${systolic.toInt()}/${diastolic.toInt()} mmHg")
                            binding.etBloodPressureSystolic.setText(systolic.toInt().toString())
                            binding.etBloodPressureDiastolic.setText(diastolic.toInt().toString())
                            showReceivedDataSnackbar("Blood pressure: ${systolic.toInt()}/${diastolic.toInt()} mmHg")
                        }
                    }
                    HealthDataParser.DataType.TEMPERATURE -> {
                        val temperature = HealthDataParser.parseTemperature(data)
                        if (temperature != null) {
                            Log.d(TAG, "Temperature data received: $temperature °C")
                            binding.etTemperature.setText(temperature.toString())
                            showReceivedDataSnackbar("Temperature: $temperature °C")
                        }
                    }
                    HealthDataParser.DataType.GLUCOSE -> {
                        val glucose = HealthDataParser.parseGlucose(data)
                        if (glucose != null) {
                            Log.d(TAG, "Glucose data received: $glucose mg/dL")
                            binding.etBloodGlucose.setText(glucose.toString())
                            showReceivedDataSnackbar("Glucose: $glucose mg/dL")
                        }
                    }
                    HealthDataParser.DataType.SPO2 -> {
                        val spo2Data = HealthDataParser.parseSpO2(data)
                        if (spo2Data != null) {
                            val (spo2, pulseRate) = spo2Data
                            Log.d(TAG, "SpO2 data received: ${spo2.toInt()}%, pulse: $pulseRate bpm")
                            binding.etOxygenSaturation.setText(spo2.toInt().toString())
                            // If heart rate not already set, use pulse rate from SpO2
                            if (binding.etHeartRate.text.isNullOrEmpty()) {
                                binding.etHeartRate.setText(pulseRate.toString())
                            }
                            showReceivedDataSnackbar("SpO2: ${spo2.toInt()}%, Pulse: $pulseRate bpm")
                        }
                    }
                    HealthDataParser.DataType.UNKNOWN -> {
                        Log.d(TAG, "Received unknown data type")
                        updateDebugInfo("Unknown data received")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing received data", e)
            updateDebugInfo("Data processing error: ${e.message}")
        }
    }

    // Show a snackbar for received data
    private fun showReceivedDataSnackbar(message: String) {
        try {
            Snackbar.make(binding.root, "Received: $message", Snackbar.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing snackbar", e)
        }
    }

    // Test Bluetooth setup for debugging
    private fun testBluetoothSetup() {
        updateDebugInfo("Testing Bluetooth setup...")

        try {
            // Check if Bluetooth is enabled
            val bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
            if (bluetoothAdapter == null) {
                updateDebugInfo("Device doesn't support Bluetooth")
                return
            }

            if (!bluetoothAdapter.isEnabled) {
                updateDebugInfo("Bluetooth is disabled")
                return
            }

            // Check permissions
            val permissionsOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) ==
                        PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) ==
                        PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED
            }

            if (!permissionsOk) {
                updateDebugInfo("Missing Bluetooth permissions")
                return
            }

            // Test scanner creation
            try {
                val scanner = bluetoothAdapter.bluetoothLeScanner
                if (scanner == null) {
                    updateDebugInfo("Failed to get BLE scanner")
                    return
                }
                updateDebugInfo("Bluetooth setup OK")
            } catch (e: Exception) {
                updateDebugInfo("Error creating scanner: ${e.message}")
                return
            }
        } catch (e: Exception) {
            updateDebugInfo("Bluetooth test error: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity visible to user")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: Activity no longer visible")

        // Cancel any pending connection timeouts
        cancelConnectionTimeout()
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
        updateDebugInfo("Connecting to ${deviceType.name}...")

        try {
            biometricManager.startReading(deviceType, object : BiometricDeviceManager.ReadingCallback {
                override fun onReadingReceived(deviceType: BiometricDeviceManager.DeviceType, value: Float, secondaryValue: Float?) {
                    runOnUiThread {
                        when (deviceType) {
                            BiometricDeviceManager.DeviceType.THERMOMETER -> {
                                binding.etTemperature.setText(String.format("%.1f", value))
                                Log.d(TAG, "Temperature reading received: $value°C")
                                Toast.makeText(this@HealthAssessmentActivity, "Temperature reading: ${String.format("%.1f", value)}°C", Toast.LENGTH_SHORT).show()
                                updateDebugInfo("Temperature reading: ${String.format("%.1f", value)}°C")
                            }
                            BiometricDeviceManager.DeviceType.HEART_RATE_MONITOR -> {
                                binding.etHeartRate.setText(value.toInt().toString())
                                Log.d(TAG, "Heart rate reading received: ${value.toInt()} bpm")
                                Toast.makeText(this@HealthAssessmentActivity, "Heart rate reading: ${value.toInt()} bpm", Toast.LENGTH_SHORT).show()
                                updateDebugInfo("Heart rate reading: ${value.toInt()} bpm")
                            }
                            BiometricDeviceManager.DeviceType.BLOOD_PRESSURE_MONITOR -> {
                                if (secondaryValue != null) {
                                    binding.etBloodPressureSystolic.setText(value.toInt().toString())
                                    binding.etBloodPressureDiastolic.setText(secondaryValue.toInt().toString())
                                    Log.d(TAG, "Blood pressure reading received: ${value.toInt()}/${secondaryValue.toInt()} mmHg")
                                    Toast.makeText(this@HealthAssessmentActivity, "Blood pressure reading: ${value.toInt()}/${secondaryValue.toInt()} mmHg", Toast.LENGTH_SHORT).show()
                                    updateDebugInfo("BP reading: ${value.toInt()}/${secondaryValue.toInt()} mmHg")
                                }
                            }
                            BiometricDeviceManager.DeviceType.OXYGEN_SATURATION_MONITOR -> {
                                binding.etOxygenSaturation.setText(value.toInt().toString())
                                Log.d(TAG, "Oxygen saturation reading received: ${value.toInt()}%")
                                Toast.makeText(this@HealthAssessmentActivity, "Oxygen saturation reading: ${value.toInt()}%", Toast.LENGTH_SHORT).show()
                                updateDebugInfo("SpO2 reading: ${value.toInt()}%")
                            }
                            BiometricDeviceManager.DeviceType.GLUCOSE_METER -> {
                                binding.etBloodGlucose.setText(String.format("%.1f", value))
                                Log.d(TAG, "Blood glucose reading received: $value mg/dL")
                                Toast.makeText(this@HealthAssessmentActivity, "Blood glucose reading: ${String.format("%.1f", value)} mg/dL", Toast.LENGTH_SHORT).show()
                                updateDebugInfo("Glucose reading: ${String.format("%.1f", value)} mg/dL")
                            }
                        }
                    }
                }

                override fun onError(deviceType: BiometricDeviceManager.DeviceType, message: String) {
                    Log.e(TAG, "Error reading from device: $message")
                    runOnUiThread {
                        Toast.makeText(this@HealthAssessmentActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                        updateDebugInfo("Device error: $message")
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error starting device reading", e)
            updateDebugInfo("Error starting device: ${e.message}")
            Toast.makeText(this, "Error starting device: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Override onActivityResult to handle device selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "onActivityResult: request=$requestCode, result=$resultCode")

        // Always ensure connect button is enabled
        binding.btnConnect.isEnabled = true

        when (requestCode) {
            REQUEST_SELECT_DEVICE -> {
                // Hide the progress indicator
                binding.progressConnect.visibility = View.GONE

                if (resultCode == RESULT_OK) {
                    val deviceAddress = data?.getStringExtra(DeviceScanActivity.EXTRA_DEVICE_ADDRESS)
                    if (deviceAddress != null) {
                        Log.d(TAG, "Device selected: $deviceAddress")
                        updateDebugInfo("Device selected: $deviceAddress")
                        connectedDeviceAddress = deviceAddress
                        connectToDevice(deviceAddress)
                    } else {
                        Log.e(TAG, "No device address returned")
                        updateDebugInfo("No device address returned")
                        Toast.makeText(this, "Error: No device selected", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // User canceled device selection
                    Log.d(TAG, "Device selection canceled or failed")
                    updateDebugInfo("Device selection canceled")
                    if (resultCode != RESULT_CANCELED) {
                        Toast.makeText(this, "Device selection failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            REQUEST_ENABLE_BT -> {
                if (resultCode == RESULT_OK) {
                    // Bluetooth was enabled
                    Log.d(TAG, "Bluetooth enabled by user")
                    updateDebugInfo("Bluetooth enabled")

                    // Retry the action that required Bluetooth
                    if (connectedDeviceAddress != null) {
                        Log.d(TAG, "Retrying connection to previously selected device")
                        connectToDevice(connectedDeviceAddress!!)
                    } else {
                        Toast.makeText(this, "Bluetooth enabled, you can now connect", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // User denied enabling Bluetooth
                    Log.d(TAG, "User declined to enable Bluetooth")
                    updateDebugInfo("Bluetooth remains disabled")
                    Toast.makeText(this, "Bluetooth is required for device connection", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Cleaning up resources")

        // Cancel any pending connection timeouts
        cancelConnectionTimeout()

        // Clean up Bluetooth connection
        try {
            bluetoothConnectionManager.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing Bluetooth connection", e)
        }

        // Stop simulation if active
        if (simulationActive) {
            try {
                bluetoothSimulator.stop()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping simulator", e)
            }
        }
    }

    // Helper method for coroutine delay
    private suspend fun delay(timeMillis: Long) {
        kotlinx.coroutines.delay(timeMillis)
    }
}