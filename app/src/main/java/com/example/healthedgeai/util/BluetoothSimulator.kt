package com.example.healthedgeai.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.Random
import java.util.UUID

/**
 * This class simulates Bluetooth health data for testing
 * without requiring actual Bluetooth devices
 */
class BluetoothSimulator(private val callback: (UUID, UUID, ByteArray) -> Unit) {
    private val TAG = "BluetoothSimulator"

    companion object {
        private const val SIMULATION_INTERVAL = 3000L // 3 seconds

        // Characteristic UUIDs
        private val HEART_RATE_CHAR_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        private val BLOOD_PRESSURE_CHAR_UUID = UUID.fromString("00002a35-0000-1000-8000-00805f9b34fb")
        private val TEMPERATURE_CHAR_UUID = UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb")
        private val GLUCOSE_CHAR_UUID = UUID.fromString("00002a18-0000-1000-8000-00805f9b34fb")
        private val SPO2_CHAR_UUID = UUID.fromString("00002a5e-0000-1000-8000-00805f9b34fb")
    }

    private val handler = Handler(Looper.getMainLooper())
    private val random = Random()
    private var running = false

    private val simulationRunnable = object : Runnable {
        override fun run() {
            if (running) {
                // Randomly select a data type to simulate
                val dataType = random.nextInt(5)

                when (dataType) {
                    0 -> simulateHeartRate()
                    1 -> simulateBloodPressure()
                    2 -> simulateTemperature()
                    3 -> simulateGlucose()
                    4 -> simulateSpO2()
                }

                // Schedule next simulation
                handler.postDelayed(this, SIMULATION_INTERVAL)
            }
        }
    }

    // Start simulation
    fun start() {
        if (!running) {
            Log.d(TAG, "Starting Bluetooth simulation")
            running = true

            // Send initial data for each type immediately
            simulateHeartRate()
            simulateBloodPressure()
            simulateTemperature()
            simulateGlucose()
            simulateSpO2()

            // Start periodic simulation
            handler.postDelayed(simulationRunnable, SIMULATION_INTERVAL)
        }
    }

    // Stop simulation
    fun stop() {
        Log.d(TAG, "Stopping Bluetooth simulation")
        running = false
        handler.removeCallbacks(simulationRunnable)
    }

    // Simulate heart rate data
    private fun simulateHeartRate() {
        val heartRate = 60 + random.nextInt(30) // 60-89 BPM

        val data = ByteArray(2)
        data[0] = 0 // Heart Rate Value Format is UINT8
        data[1] = heartRate.toByte()

        Log.d(TAG, "Simulating heart rate: $heartRate bpm")
        callback(
            BluetoothDeviceManager.HEART_RATE_SERVICE_UUID,
            HEART_RATE_CHAR_UUID,
            data
        )
    }

    // Simulate blood pressure data
    private fun simulateBloodPressure() {
        // Generate random blood pressure within normal ranges
        val systolic = 110 + random.nextInt(30) // 110-139 mmHg
        val diastolic = 70 + random.nextInt(15) // 70-84 mmHg

        // Ensure diastolic is always lower than systolic
        val finalDiastolic = kotlin.math.min(diastolic, systolic - 20)

        Log.d(TAG, "Simulating blood pressure: $systolic/$finalDiastolic mmHg")

        // Convert to IEEE-11073 16-bit SFLOAT values
        // For our simulation, we'll use mantissa with exponent 0 (x10^0)
        val systolicSfloat = systolic
        val diastolicSfloat = finalDiastolic

        // Format as per BLE Blood Pressure Measurement characteristic
        val data = ByteArray(7)
        data[0] = 0 // Flags (mmHg unit, no timestamp)

        // Systolic value (SFLOAT)
        data[1] = (systolicSfloat and 0xFF).toByte()
        data[2] = ((systolicSfloat shr 8) and 0xFF).toByte()

        // Diastolic value (SFLOAT)
        data[3] = (diastolicSfloat and 0xFF).toByte()
        data[4] = ((diastolicSfloat shr 8) and 0xFF).toByte()

        // MAP (Mean Arterial Pressure) - also SFLOAT
        val map = ((2 * finalDiastolic) + systolic) / 3
        data[5] = (map and 0xFF).toByte()
        data[6] = ((map shr 8) and 0xFF).toByte()

        callback(
            BluetoothDeviceManager.BLOOD_PRESSURE_SERVICE_UUID,
            BLOOD_PRESSURE_CHAR_UUID,
            data
        )
    }

    // Simulate temperature data
    private fun simulateTemperature() {
        // Generate temperature between 36.5 and 37.5 °C
        val temp = 3650 + random.nextInt(100) // 36.50-37.49 °C (stored as integer × 100)
        Log.d(TAG, "Simulating temperature: ${temp/100.0f} °C")

        // Format as per BLE Temperature Measurement characteristic
        val data = ByteArray(5)
        data[0] = 0 // Flags (Celsius, no timestamp)

        // Temperature value (Float)
        data[1] = (temp and 0xFF).toByte()
        data[2] = ((temp shr 8) and 0xFF).toByte()
        data[3] = ((temp shr 16) and 0xFF).toByte()
        data[4] = ((temp shr 24) and 0xFF).toByte()

        callback(
            BluetoothDeviceManager.THERMOMETER_SERVICE_UUID,
            TEMPERATURE_CHAR_UUID,
            data
        )
    }

    // Simulate glucose data
    private fun simulateGlucose() {
        // Generate glucose value between 80 and 120 mg/dL
        val glucose = 80 + random.nextInt(40) // 80-119 mg/dL

        // Convert to SFLOAT - use the same format as blood pressure (mantissa only)
        val glucoseSfloat = glucose

        Log.d(TAG, "Simulating glucose: $glucose mg/dL")

        // Format as per BLE Glucose Measurement characteristic
        val data = ByteArray(10)
        data[0] = 0 // Flags

        // Time data (all zeros for simplicity)
        for (i in 1..7) {
            data[i] = 0
        }

        // Glucose value (SFLOAT)
        data[8] = (glucoseSfloat and 0xFF).toByte()
        data[9] = ((glucoseSfloat shr 8) and 0xFF).toByte()

        callback(
            BluetoothDeviceManager.GLUCOSE_SERVICE_UUID,
            GLUCOSE_CHAR_UUID,
            data
        )
    }

    // Simulate SpO2 data
    private fun simulateSpO2() {
        // Generate SpO2 value between 95 and 99%
        val spo2 = 95 + random.nextInt(5) // 95-99%

        // Generate pulse rate between 60 and 90 BPM
        val pulseRate = 60 + random.nextInt(30) // 60-89 BPM

        Log.d(TAG, "Simulating SpO2: $spo2%, Pulse: $pulseRate bpm")

        // Format as per BLE SpO2 Measurement characteristic
        val data = ByteArray(5)
        data[0] = 0 // Flags
        data[1] = spo2.toByte() // SpO2 value
        data[2] = pulseRate.toByte() // Pulse rate (moved to correct position)
        data[3] = 0 // Reserved
        data[4] = 0 // Reserved

        callback(
            BluetoothDeviceManager.PULSE_OXIMETER_SERVICE_UUID,
            SPO2_CHAR_UUID,
            data
        )
    }
}