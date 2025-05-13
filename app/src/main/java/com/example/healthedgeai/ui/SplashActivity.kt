package com.example.healthedgeai.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.healthedgeai.databinding.ActivitySplashBinding
import com.example.healthedgeai.R
import android.util.Log

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivitySplashBinding.inflate(layoutInflater)
            setContentView(binding.root)

            Log.d(TAG, "SplashActivity created, waiting to navigate to LoginActivity")

            // Navigate to LoginActivity after a delay
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    Log.d(TAG, "Navigating to LoginActivity")
                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to LoginActivity", e)
                }
            }, 3000) // 3 seconds delay

        } catch (e: Exception) {
            Log.e(TAG, "Error in SplashActivity onCreate", e)
        }
    }
}

