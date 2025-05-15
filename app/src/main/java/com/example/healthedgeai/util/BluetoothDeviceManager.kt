package com.example.healthedgeai.util

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

class BluetoothDeviceManager(private val context: Context) {

    companion object {
        private const val TAG = "BluetoothDeviceManager"
        private const val SCAN_PERIOD = 10000L // 10 seconds

        // Standard Healthcare Service UUIDs
        val HEART_RATE_SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val BLOOD_PRESSURE_SERVICE_UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb")
        val THERMOMETER_SERVICE_UUID = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb")
        val GLUCOSE_SERVICE_UUID = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb")
        val PULSE_OXIMETER_SERVICE_UUID = UUID.fromString("00001822-0000-1000-8000-00805f9b34fb")
    }

    // Callback for scan results
    interface ScanResultCallback {
        fun onDeviceFound(device: BluetoothDevice, rssi: Int, name: String?)
        fun onScanFinished(deviceList: List<BluetoothDevice>)
        fun onScanFailed(errorCode: Int)
    }

    // Bluetooth components
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())

    // Store scan results
    private val scannedDevices = mutableListOf<BluetoothDevice>()
    private var scanCallback: ScanResultCallback? = null

    // Check if Bluetooth is supported and enabled
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    // Start scanning for BLE devices
    fun startScan(callback: ScanResultCallback) {
        if (!isBluetoothEnabled()) {
            Log.e(TAG, "Bluetooth not enabled")
            callback.onScanFailed(ScanCallback.SCAN_FAILED_INTERNAL_ERROR)
            return
        }

        this.scanCallback = callback
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

        if (scanning) {
            stopScan()
        }

        scannedDevices.clear()

        try {
            // Scan filters for healthcare devices
            val filters = createHealthcareDeviceFilters()

            // Scan settings
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            // Start scanning
            scanning = true
            bluetoothLeScanner?.startScan(filters, settings, leScanCallback)

            // Stop scan after delay
            handler.postDelayed({
                stopScan()
            }, SCAN_PERIOD)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting scan", e)
            callback.onScanFailed(ScanCallback.SCAN_FAILED_INTERNAL_ERROR)
        }
    }

    // Stop scanning
    fun stopScan() {
        if (scanning && bluetoothAdapter?.isEnabled == true) {
            scanning = false
            bluetoothLeScanner?.stopScan(leScanCallback)

            scanCallback?.onScanFinished(scannedDevices.toList())
        }
    }

    // Create filters for healthcare devices
    private fun createHealthcareDeviceFilters(): List<ScanFilter> {
        val filters = mutableListOf<ScanFilter>()

        // Add filters for healthcare service UUIDs
        val serviceUuids = listOf(
            HEART_RATE_SERVICE_UUID,
            BLOOD_PRESSURE_SERVICE_UUID,
            THERMOMETER_SERVICE_UUID,
            GLUCOSE_SERVICE_UUID,
            PULSE_OXIMETER_SERVICE_UUID
        )

        // Create a filter for each service UUID
        serviceUuids.forEach { uuid ->
            val filter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(uuid))
                .build()
            filters.add(filter)
        }

        return filters
    }

    // Scan callback
    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device !in scannedDevices) {
                scannedDevices.add(device)
                scanCallback?.onDeviceFound(device, result.rssi, device.name)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            for (result in results) {
                val device = result.device
                if (device !in scannedDevices) {
                    scannedDevices.add(device)
                    scanCallback?.onDeviceFound(device, result.rssi, device.name)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error: $errorCode")
            scanCallback?.onScanFailed(errorCode)
        }
    }

    // Get scanned devices
    fun getScannedDevices(): List<BluetoothDevice> {
        return scannedDevices.toList()
    }

    // Save device to preferences
    fun saveDeviceToPreferences(device: BluetoothDevice) {
        val sharedPreferences = context.getSharedPreferences("bluetooth_devices", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("last_device_address", device.address)
            putString("last_device_name", device.name ?: "Unknown Device")
            apply()
        }
    }

    // Get last connected device
    fun getLastConnectedDevice(): Pair<String, String>? {
        val sharedPreferences = context.getSharedPreferences("bluetooth_devices", Context.MODE_PRIVATE)
        val address = sharedPreferences.getString("last_device_address", null)
        val name = sharedPreferences.getString("last_device_name", "Unknown Device")

        return if (address != null) {
            Pair(address, name ?: "Unknown Device")
        } else {
            null
        }
    }
}