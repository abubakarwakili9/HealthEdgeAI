package com.example.healthedgeai.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import com.example.healthedgeai.model.HealthRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfExporter(private val context: Context) {
    private val TAG = "PdfExporter"

    fun exportToPdf(record: HealthRecord): Boolean {
        try {
            // Create a new PDF document
            val document = PdfDocument()

            // Create a page description
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()

            // Start a page
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Draw content on the page
            drawContent(canvas, record)

            // Finish the page
            document.finishPage(page)

            // Save the document
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val fileName = "HealthRecord_${timestamp}.pdf"

            val filePath = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                File(docsDir, fileName)
            } else {
                File(context.filesDir, fileName)
            }

            document.writeTo(FileOutputStream(filePath))
            document.close()

            Log.d(TAG, "PDF created successfully at ${filePath.absolutePath}")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Error creating PDF", e)
            return false
        }
    }

    private fun drawContent(canvas: Canvas, record: HealthRecord) {
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
        }

        val headingPaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
        }

        val subTextPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        // Format date and time
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(record.timestamp))

        var y = 50f

        // Title
        canvas.drawText("Health Record", 50f, y, titlePaint)
        y += 30f

        // Date
        canvas.drawText("Date: $formattedDate", 50f, y, subTextPaint)
        y += 30f

        // Horizontal line
        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 20f

        // Vital Signs section
        canvas.drawText("Vital Signs", 50f, y, headingPaint)
        y += 25f

        // Temperature
        canvas.drawText("Temperature:", 50f, y, textPaint)
        canvas.drawText("${record.temperature}°C", 200f, y, textPaint)
        y += 20f

        // Heart Rate
        canvas.drawText("Heart Rate:", 50f, y, textPaint)
        canvas.drawText("${record.heartRate} bpm", 200f, y, textPaint)
        y += 20f

        // Blood Pressure
        canvas.drawText("Blood Pressure:", 50f, y, textPaint)
        canvas.drawText("${record.bloodPressureSystolic}/${record.bloodPressureDiastolic} mmHg", 200f, y, textPaint)
        y += 20f

        // Respiration Rate
        canvas.drawText("Respiration Rate:", 50f, y, textPaint)
        canvas.drawText("${record.respirationRate} breaths/min", 200f, y, textPaint)
        y += 20f

        // Oxygen Saturation
        canvas.drawText("Oxygen Saturation:", 50f, y, textPaint)
        canvas.drawText("${record.oxygenSaturation}%", 200f, y, textPaint)
        y += 20f

        // Blood Glucose
        canvas.drawText("Blood Glucose:", 50f, y, textPaint)
        canvas.drawText("${record.bloodGlucose} mg/dL", 200f, y, textPaint)
        y += 30f

        // Measurements section
        canvas.drawText("Measurements", 50f, y, headingPaint)
        y += 25f

        // Weight
        canvas.drawText("Weight:", 50f, y, textPaint)
        canvas.drawText(if (record.weight > 0) "${record.weight} kg" else "Not recorded", 200f, y, textPaint)
        y += 20f

        // Height
        canvas.drawText("Height:", 50f, y, textPaint)
        canvas.drawText(if (record.height > 0) "${record.height} cm" else "Not recorded", 200f, y, textPaint)
        y += 20f

        // BMI
        if (record.weight > 0 && record.height > 0) {
            val heightInMeters = record.height / 100
            val bmi = record.weight / (heightInMeters * heightInMeters)
            val bmiCategory = when {
                bmi < 18.5 -> "Underweight"
                bmi < 25 -> "Normal"
                bmi < 30 -> "Overweight"
                else -> "Obese"
            }

            canvas.drawText("BMI:", 50f, y, textPaint)
            canvas.drawText("${String.format("%.1f", bmi)} ($bmiCategory)", 200f, y, textPaint)
            y += 30f
        }

        // Horizontal line
        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 20f

        // Notes section
        if (record.notes.isNotEmpty()) {
            canvas.drawText("Notes", 50f, y, headingPaint)
            y += 25f

            // Split notes into multiple lines if needed
            val notes = record.notes
            val lineWidth = 495f
            val lineHeight = 18f

            var startIndex = 0
            var lastIndex = 0
            var currentLine = ""

            while (startIndex < notes.length) {
                lastIndex = startIndex + 1
                if (lastIndex <= notes.length) {
                    currentLine = notes.substring(startIndex, lastIndex)

                    while (lastIndex < notes.length &&
                        textPaint.measureText(currentLine + notes[lastIndex]) < lineWidth) {
                        lastIndex++
                        currentLine = notes.substring(startIndex, lastIndex)
                    }

                    // Check if line break or end of string
                    if (lastIndex < notes.length && notes[lastIndex - 1] != '\n') {
                        // Try to break at last space
                        val lastSpace = currentLine.lastIndexOf(' ')
                        if (lastSpace > 0) {
                            lastIndex = startIndex + lastSpace + 1
                            currentLine = notes.substring(startIndex, startIndex + lastSpace)
                        }
                    }

                    canvas.drawText(currentLine, 50f, y, textPaint)
                    y += lineHeight

                    startIndex = lastIndex
                } else {
                    break
                }
            }

            y += 12f
        }

        // Horizontal line
        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 20f

        // AI Assessment section
        if (record.aiAssessment.isNotEmpty()) {
            canvas.drawText("AI Assessment", 50f, y, headingPaint)
            y += 25f

            // Get assessment result
            val assessmentResult = when {
                record.aiAssessment.contains("Healthy") -> "Healthy"
                record.aiAssessment.contains("Moderate") -> "Moderate Concern"
                record.aiAssessment.contains("Critical") -> "Critical"
                else -> "Assessment Completed"
            }

            // Draw assessment result
            val assessmentPaint = Paint().apply {
                textSize = 16f
                isFakeBoldText = true
                color = when {
                    record.aiAssessment.contains("Healthy") -> Color.rgb(76, 175, 80) // Green
                    record.aiAssessment.contains("Moderate") -> Color.rgb(255, 152, 0) // Orange
                    record.aiAssessment.contains("Critical") -> Color.rgb(244, 67, 54) // Red
                    else -> Color.rgb(96, 125, 139) // Gray
                }
            }

            canvas.drawText("Assessment Result: $assessmentResult", 50f, y, assessmentPaint)
            y += 25f

            // Draw assessment details
            val lineWidth = 495f
            val lineHeight = 18f

            var startIndex = 0
            var lastIndex = 0
            var currentLine = ""

            while (startIndex < record.aiAssessment.length) {
                lastIndex = startIndex + 1
                if (lastIndex <= record.aiAssessment.length) {
                    currentLine = record.aiAssessment.substring(startIndex, lastIndex)

                    while (lastIndex < record.aiAssessment.length &&
                        textPaint.measureText(currentLine + record.aiAssessment[lastIndex]) < lineWidth) {
                        lastIndex++
                        currentLine = record.aiAssessment.substring(startIndex, lastIndex)
                    }

                    // Check if line break or end of string
                    if (lastIndex < record.aiAssessment.length && record.aiAssessment[lastIndex - 1] != '\n') {
                        // Try to break at last space
                        val lastSpace = currentLine.lastIndexOf(' ')
                        if (lastSpace > 0) {
                            lastIndex = startIndex + lastSpace + 1
                            currentLine = record.aiAssessment.substring(startIndex, startIndex + lastSpace)
                        }
                    }

                    canvas.drawText(currentLine, 50f, y, textPaint)
                    y += lineHeight

                    startIndex = lastIndex
                } else {
                    break
                }
            }
        }

        // Footer
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
        }

        canvas.drawText("Generated by HealthEdge AI on ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())}", 50f, 800f, footerPaint)
    }
}