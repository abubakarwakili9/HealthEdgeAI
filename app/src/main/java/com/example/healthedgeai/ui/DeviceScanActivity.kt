package com.example.healthedgeai.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthedgeai.R
import com.example.healthedgeai.databinding.ActivityDeviceScanBinding
import com.example.healthedgeai.util.BluetoothDeviceManager

class DeviceScanActivity : BluetoothBaseActivity() {

    private lateinit var binding: ActivityDeviceScanBinding
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var bluetoothDeviceManager: BluetoothDeviceManager
    private var isScanning = false

    companion object {
        const val EXTRA_DEVICE_ADDRESS = "device_address"
        const val EXTRA_DEVICE_NAME = "device_name"
        const val REQUEST_ENABLE_BT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set navigation icon click listener explicitly
        binding.toolbar.setNavigationOnClickListener {
            onSupportNavigateUp()
        }

        // Initialize Bluetooth manager
        bluetoothDeviceManager = BluetoothDeviceManager(this)

        // Set up RecyclerView and adapter
        setupRecyclerView()

        // Set up button click listener
        binding.btnStartScan.setOnClickListener {
            if (isScanning) {
                stopScanning()
            } else {
                startScanning()
            }
        }

        // Check if Bluetooth is enabled
        if (!bluetoothDeviceManager.isBluetoothEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            // Auto-start scan when activity opens if Bluetooth is already enabled
            startScanning()
        }
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter { device ->
            // Connect to selected device
            connectToDevice(device)
        }

        binding.rvDevices.apply {
            layoutManager = LinearLayoutManager(this@DeviceScanActivity)
            adapter = deviceAdapter
        }
    }

    private fun startScanning() {
        // Check permissions first
        if (!ensureBluetoothPermissions()) {
            return
        }

        // Show scanning state
        binding.progressBar.visibility = View.VISIBLE
        binding.btnStartScan.isEnabled = true
        binding.btnStartScan.text = "Stop Scan"
        binding.tvScanStatus.text = "Scanning for healthcare devices..."
        isScanning = true

        // Clear previous results
        deviceAdapter.clearDevices()

        // Start scan
        bluetoothDeviceManager.startScan(object : BluetoothDeviceManager.ScanResultCallback {
            override fun onDeviceFound(device: BluetoothDevice, rssi: Int, name: String?) {
                runOnUiThread {
                    deviceAdapter.addDevice(device, rssi, name)
                }
            }

            override fun onScanFinished(deviceList: List<BluetoothDevice>) {
                runOnUiThread {
                    updateScanningUiState(false)

                    if (deviceList.isEmpty()) {
                        binding.tvScanStatus.text = "No devices found. Try again."
                    } else {
                        binding.tvScanStatus.text = "${deviceList.size} devices found."
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                runOnUiThread {
                    updateScanningUiState(false)
                    binding.tvScanStatus.text = "Scan failed. Please try again."

                    Toast.makeText(
                        this@DeviceScanActivity,
                        "Scan failed with error code: $errorCode",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun stopScanning() {
        if (isScanning) {
            bluetoothDeviceManager.stopScan()
            updateScanningUiState(false)
        }
    }

    private fun updateScanningUiState(scanning: Boolean) {
        isScanning = scanning
        binding.progressBar.visibility = if (scanning) View.VISIBLE else View.GONE
        binding.btnStartScan.text = if (scanning) "Stop Scan" else "Start Scan"
        binding.btnStartScan.isEnabled = true
    }

    private fun connectToDevice(device: BluetoothDevice) {
        // Stop scanning before connecting
        stopScanning()

        // Save device to preferences
        bluetoothDeviceManager.saveDeviceToPreferences(device)

        // Return the selected device to the calling activity
        val intent = Intent()
        intent.putExtra(EXTRA_DEVICE_ADDRESS, device.address)
        intent.putExtra(EXTRA_DEVICE_NAME, device.name ?: "Unknown Device")
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onBluetoothPermissionsGranted() {
        // Permissions granted, start scanning
        startScanning()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth enabled, check permissions and start scanning
                if (ensureBluetoothPermissions()) {
                    startScanning()
                }
            } else {
                // User declined to enable Bluetooth
                Toast.makeText(
                    this,
                    "Bluetooth is required to scan for devices",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop scanning when activity is paused
        stopScanning()
    }

    override fun onStop() {
        super.onStop()
        // Ensure scanning is stopped when activity is stopped
        stopScanning()
    }

    override fun onBackPressed() {
        // Stop scanning before closing
        stopScanning()

        // Set result canceled
        setResult(RESULT_CANCELED)

        super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        // Cancel any ongoing scan
        stopScanning()

        // Set result canceled
        setResult(RESULT_CANCELED)

        // Close the activity and return to previous screen
        finish()
        return true
    }
}