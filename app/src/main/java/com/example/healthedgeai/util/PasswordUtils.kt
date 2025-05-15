package com.example.healthedgeai.util

import java.security.MessageDigest
import android.util.Log

object PasswordUtils {
    private const val TAG = "PasswordUtils"

    fun hashPassword(password: String): String {
        try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(password.toByteArray())
            val hexString = StringBuilder()
            for (byte in digest) {
                val hex = Integer.toHexString(0xff and byte.toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            return hexString.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error hashing password", e)
            // Fallback for testing only (not secure)
            return password
        }
    }

    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
        return hashPassword(plainPassword) == hashedPassword
    }
}