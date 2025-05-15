package com.example.healthedgeai.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogHelper {
    private const val TAG = "HealthEdgeAI"
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    fun v(message: String, tag: String = TAG) {
        Log.v(tag, formatMessage(message))
    }

    fun d(message: String, tag: String = TAG) {
        Log.d(tag, formatMessage(message))
    }

    fun i(message: String, tag: String = TAG) {
        Log.i(tag, formatMessage(message))
    }

    fun w(message: String, tag: String = TAG) {
        Log.w(tag, formatMessage(message))
    }

    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        Log.e(tag, formatMessage(message), throwable)
    }

    fun hexDump(data: ByteArray, tag: String = TAG) {
        val hexString = data.joinToString("") { "%02X".format(it) }
        Log.d(tag, formatMessage("Hex dump: $hexString"))
    }

    private fun formatMessage(message: String): String {
        val timestamp = dateFormat.format(Date())
        return "[$timestamp] $message"
    }
}