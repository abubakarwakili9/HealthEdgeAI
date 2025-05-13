package com.example.healthedgeai.util

/**
 * Utility class for health-related calculations and validations
 */
class HealthUtils {

    companion object {

        /**
         * Checks if a temperature value is abnormal
         * @param temperature Temperature in Celsius
         * @return Pair of (isAbnormal, message)
         */
        fun checkTemperature(temperature: Float): Pair<Boolean, String?> {
            return when {
                temperature < 35.0f -> Pair(true, "Hypothermia risk (${temperature}°C)")
                temperature > 38.0f -> Pair(true, "Fever detected (${temperature}°C)")
                else -> Pair(false, null)
            }
        }

        /**
         * Checks if a heart rate value is abnormal
         * @param heartRate Heart rate in beats per minute
         * @param age Age of the patient in years
         * @return Pair of (isAbnormal, message)
         */
        fun checkHeartRate(heartRate: Int, age: Int): Pair<Boolean, String?> {
            // Age-based heart rate ranges
            val maxNormal = when {
                age < 1 -> 160
                age < 3 -> 150
                age < 5 -> 140
                age < 12 -> 120
                age < 18 -> 100
                else -> 100
            }

            val minNormal = when {
                age < 1 -> 100
                age < 3 -> 90
                age < 5 -> 80
                age < 12 -> 70
                age < 18 -> 60
                else -> 60
            }

            return when {
                heartRate < minNormal -> Pair(true, "Low heart rate (${heartRate} bpm)")
                heartRate > maxNormal -> Pair(true, "Elevated heart rate (${heartRate} bpm)")
                else -> Pair(false, null)
            }
        }

        /**
         * Checks if blood pressure values are abnormal
         * @param systolic Systolic blood pressure in mmHg
         * @param diastolic Diastolic blood pressure in mmHg
         * @return Pair of (isAbnormal, message)
         */
        fun checkBloodPressure(systolic: Int, diastolic: Int): Pair<Boolean, String?> {
            return when {
                systolic > 140 || diastolic > 90 -> Pair(true, "Hypertension risk (${systolic}/${diastolic} mmHg)")
                systolic < 90 || diastolic < 60 -> Pair(true, "Hypotension risk (${systolic}/${diastolic} mmHg)")
                else -> Pair(false, null)
            }
        }

        /**
         * Checks if oxygen saturation is abnormal
         * @param oxygenSaturation Oxygen saturation percentage
         * @return Pair of (isAbnormal, message)
         */
        fun checkOxygenSaturation(oxygenSaturation: Int): Pair<Boolean, String?> {
            return when {
                oxygenSaturation < 95 -> Pair(true, "Low oxygen saturation (${oxygenSaturation}%)")
                else -> Pair(false, null)
            }
        }

        /**
         * Checks if blood glucose level is abnormal
         * @param bloodGlucose Blood glucose in mg/dL
         * @param isFasting Whether the reading was taken while fasting
         * @return Pair of (isAbnormal, message)
         */
        fun checkBloodGlucose(bloodGlucose: Float, isFasting: Boolean = true): Pair<Boolean, String?> {
            return when {
                isFasting && bloodGlucose > 126f -> Pair(true, "Elevated fasting blood glucose (${bloodGlucose} mg/dL)")
                !isFasting && bloodGlucose > 200f -> Pair(true, "Elevated blood glucose (${bloodGlucose} mg/dL)")
                bloodGlucose < 70f -> Pair(true, "Low blood glucose (${bloodGlucose} mg/dL)")
                else -> Pair(false, null)
            }
        }

        /**
         * Checks if BMI is abnormal
         * @param weightKg Weight in kilograms
         * @param heightCm Height in centimeters
         * @return Pair of (isAbnormal, message)
         */
        fun checkBMI(weightKg: Float, heightCm: Float): Pair<Boolean, String?> {
            val heightM = heightCm / 100f
            val bmi = weightKg / (heightM * heightM)

            return when {
                bmi < 18.5f -> Pair(true, "Underweight (BMI: ${String.format("%.1f", bmi)})")
                bmi > 30f -> Pair(true, "Obesity (BMI: ${String.format("%.1f", bmi)})")
                bmi > 25f -> Pair(true, "Overweight (BMI: ${String.format("%.1f", bmi)})")
                else -> Pair(false, null)
            }
        }

        /**
         * Checks if all vital signs are normal or abnormal
         * @param vitals Map of vital sign names to their abnormality status
         * @return Overall status message
         */
        fun getOverallStatus(vitals: Map<String, Pair<Boolean, String?>>): String {
            val abnormalVitals = vitals.filter { it.value.first }

            return when {
                abnormalVitals.isEmpty() -> "All vital signs within normal range."
                abnormalVitals.size == 1 -> {
                    val vital = abnormalVitals.entries.first()
                    "Abnormal: ${vital.value.second}"
                }
                else -> {
                    val messages = abnormalVitals.map { it.value.second }
                    "Multiple abnormal vital signs detected: ${messages.joinToString(", ")}"
                }
            }
        }
    }
}