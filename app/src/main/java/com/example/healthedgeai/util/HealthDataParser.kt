package com.example.healthedgeai.util

import android.util.Log
import java.util.UUID

object HealthDataParser {

    private const val TAG = "HealthDataParser"

    // Heart Rate Measurement parser
    fun parseHeartRate(data: ByteArray): Int? {
        if (data.isEmpty()) return null

        try {
            // Check the Heart Rate Value Format bit (bit 0)
            val format = data[0].toInt() and 0x01  // Changed this line from "and" to "and"

            return if (format == 0) {
                // Format is UINT8
                data[1].toInt() and 0xFF
            } else {
                // Format is UINT16
                (data[1].toInt() and 0xFF) + ((data[2].toInt() and 0xFF) shl 8)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing heart rate data", e)
            return null
        }
    }

    // Blood Pressure Measurement parser
    fun parseBloodPressure(data: ByteArray): Pair<Float, Float>? {
        if (data.size < 7) return null

        try {
            // Extract systolic and diastolic (they are IEEE-11073 16-bit SFLOAT values)
            val systolic = sfloatToFloat(
                (data[1].toInt() and 0xFF) + ((data[2].toInt() and 0xFF) shl 8)
            )
            val diastolic = sfloatToFloat(
                (data[3].toInt() and 0xFF) + ((data[4].toInt() and 0xFF) shl 8)
            )

            return Pair(systolic, diastolic)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing blood pressure data", e)
            return null
        }
    }

    // Temperature Measurement parser
    fun parseTemperature(data: ByteArray): Float? {
        if (data.size < 5) return null

        try {
            // Extract temperature value (IEEE-11073 FLOAT value)
            val tempValue = (data[1].toInt() and 0xFF) +
                    ((data[2].toInt() and 0xFF) shl 8) +
                    ((data[3].toInt() and 0xFF) shl 16) +
                    ((data[4].toInt() and 0xFF) shl 24)

            return tempValue.toFloat() / 100f
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing temperature data", e)
            return null
        }
    }

    // Glucose Measurement parser
    fun parseGlucose(data: ByteArray): Float? {
        if (data.size < 10) return null

        try {
            // Extract glucose concentration (IEEE-11073 16-bit SFLOAT value)
            val glucoseValue = sfloatToFloat(
                (data[8].toInt() and 0xFF) + ((data[9].toInt() and 0xFF) shl 8)
            )

            return glucoseValue
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing glucose data", e)
            return null
        }
    }

    // SpO2 Measurement parser
    fun parseSpO2(data: ByteArray): Pair<Float, Int>? {
        if (data.size < 5) return null

        try {
            // Extract SpO2 value (percentage)
            val spo2Value = data[1].toInt() and 0xFF

            // Extract pulse rate value (in BPM)
            val pulseRate = data[2].toInt() and 0xFF

            return Pair(spo2Value.toFloat(), pulseRate)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing SpO2 data", e)
            return null
        }
    }

    // Convert SFLOAT to Float (IEEE-11073 16-bit SFLOAT)
    private fun sfloatToFloat(sfloat: Int): Float {
        val mantissa = sfloat and 0x0FFF
        val exponent = (sfloat and 0xF000) shr 12

        // Handle negative mantissa
        val signedMantissa = if ((mantissa and 0x0800) != 0) {
            mantissa - 0x1000
        } else {
            mantissa
        }

        return signedMantissa * 10f.pow(exponent)
    }

    // Power function for floats
    private fun Float.pow(exponent: Int): Float {
        var result = 1f
        val base = this
        var exp = exponent

        if (exp < 0) {
            exp = -exp
            while (exp > 0) {
                if (exp % 2 == 1) {
                    result /= base
                }
                exp /= 2
                if (exp > 0) {
                    result /= (base * base)
                }
            }
        } else {
            while (exp > 0) {
                if (exp % 2 == 1) {
                    result *= base
                }
                exp /= 2
                if (exp > 0) {
                    result *= (base * base)
                }
            }
        }

        return result
    }

    // Identify data type based on service and characteristic UUIDs
    fun identifyDataType(serviceUuid: UUID, characteristicUuid: UUID): DataType {
        return when {
            serviceUuid == BluetoothDeviceManager.HEART_RATE_SERVICE_UUID &&
                    characteristicUuid == UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb") -> {
                DataType.HEART_RATE
            }
            serviceUuid == BluetoothDeviceManager.BLOOD_PRESSURE_SERVICE_UUID &&
                    characteristicUuid == UUID.fromString("00002a35-0000-1000-8000-00805f9b34fb") -> {
                DataType.BLOOD_PRESSURE
            }
            serviceUuid == BluetoothDeviceManager.THERMOMETER_SERVICE_UUID &&
                    characteristicUuid == UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb") -> {
                DataType.TEMPERATURE
            }
            serviceUuid == BluetoothDeviceManager.GLUCOSE_SERVICE_UUID &&
                    characteristicUuid == UUID.fromString("00002a18-0000-1000-8000-00805f9b34fb") -> {
                DataType.GLUCOSE
            }
            serviceUuid == BluetoothDeviceManager.PULSE_OXIMETER_SERVICE_UUID &&
                    characteristicUuid == UUID.fromString("00002a5e-0000-1000-8000-00805f9b34fb") -> {
                DataType.SPO2
            }
            else -> DataType.UNKNOWN
        }
    }

    // Data types
    enum class DataType {
        HEART_RATE,
        BLOOD_PRESSURE,
        TEMPERATURE,
        GLUCOSE,
        SPO2,
        UNKNOWN
    }
}