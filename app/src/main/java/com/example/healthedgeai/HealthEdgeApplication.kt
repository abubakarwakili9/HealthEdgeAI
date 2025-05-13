package com.example.healthedgeai

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.room.Room
import com.example.healthedgeai.model.AppDatabase
import com.example.healthedgeai.util.OnnxModelWrapper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class HealthEdgeApplication : Application() {

    // Database instance
    lateinit var database: AppDatabase
        private set

    // AI Model wrapper
    lateinit var modelWrapper: OnnxModelWrapper
        private set

    // Application scope for coroutines
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Network status
    private var _isNetworkAvailable = false
    val isNetworkAvailable: Boolean
        get() = _isNetworkAvailable

    // Gson instance for JSON parsing
    val gson = Gson()

    // Main thread handler
    val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "HealthEdgeApp"

        private lateinit var instance: HealthEdgeApplication

        fun getInstance(): HealthEdgeApplication {
            return instance
        }
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        // Log application start
        Log.d(TAG, "Application initialized")

        // Set up global exception handler for debugging
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", exception)
        }

        // Initialize application-wide components
        initializeComponents()

        // Setup network monitoring
        setupNetworkMonitoring()

        // Schedule periodic data sync
        schedulePeriodicSync()
    }

    private fun initializeComponents() {
        try {
            // Initialize Room database
            database = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "health_edge_ai_database"
            ).fallbackToDestructiveMigration() // During development only
                .build()

            // Initialize AI model wrapper
            modelWrapper = OnnxModelWrapper(applicationContext)

            // Pre-initialize the model in a background coroutine
            applicationScope.launch {
                try {
                    modelWrapper.initialize()
                    Log.d(TAG, "AI model initialized successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing AI model", e)
                }
            }

            Log.d(TAG, "Components initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing components", e)
        }
    }

    private fun setupNetworkMonitoring() {
        try {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            // Check initial network state
            updateNetworkStatus(connectivityManager)

            // Register network callback for changes - using API level compatible method
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // For API 24+
                connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: android.net.Network) {
                        _isNetworkAvailable = true
                        Log.d(TAG, "Network connection available")

                        // Attempt to sync data when network becomes available
                        if (_isNetworkAvailable) {
                            applicationScope.launch {
                                syncDataWithServer()
                            }
                        }
                    }

                    override fun onLost(network: android.net.Network) {
                        _isNetworkAvailable = false
                        Log.d(TAG, "Network connection lost")
                    }
                })
            } else {
                // Just check once for API 23
                _isNetworkAvailable = isNetworkConnected(connectivityManager)
                Log.d(TAG, "Network status (API 23): ${if (_isNetworkAvailable) "Connected" else "Disconnected"}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up network monitoring", e)
        }
    }

    private fun updateNetworkStatus(connectivityManager: ConnectivityManager) {
        _isNetworkAvailable = isNetworkConnected(connectivityManager)
        Log.d(TAG, "Initial network status: ${if (_isNetworkAvailable) "Connected" else "Disconnected"}")
    }

    // Helper method for checking network connectivity that works on all API levels
    private fun isNetworkConnected(connectivityManager: ConnectivityManager): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } else {
            // For API level 23
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }

    private fun schedulePeriodicSync() {
        // Schedule periodic sync using Handler
        // This is a simple approach - in a real app, consider using WorkManager
        val syncRunnable = object : Runnable {
            override fun run() {
                if (_isNetworkAvailable) {
                    applicationScope.launch {
                        syncDataWithServer()
                    }
                }
                // Schedule next sync after 30 minutes
                mainHandler.postDelayed(this, TimeUnit.MINUTES.toMillis(30))
            }
        }

        // Start the periodic sync
        mainHandler.post(syncRunnable)
    }

    private suspend fun syncDataWithServer() {
        try {
            Log.d(TAG, "Attempting to sync data with server")
            // Implement actual sync logic here
            // For example:
            // 1. Get unsynced records from database
            // 2. Send them to server
            // 3. Mark as synced in local database
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing data with server", e)
        }
    }

    // Helper methods for UI components

    /**
     * Shows a message on the main thread after a delay
     */
    fun showMessageDelayed(runnable: Runnable, delayMillis: Long) {
        mainHandler.postDelayed(runnable, delayMillis)
    }

    /**
     * Checks if it's the app's first run
     */
    fun isFirstRun(): Boolean {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("is_first_run", true)

        if (isFirstRun) {
            prefs.edit().putBoolean("is_first_run", false).apply()
        }

        return isFirstRun
    }

    /**
     * Runs database operations safely in a coroutine
     */
    fun runDatabaseOperation(operation: suspend () -> Unit) {
        applicationScope.launch {
            try {
                operation()
            } catch (e: Exception) {
                Log.e(TAG, "Database operation failed", e)
            }
        }
    }

    override fun onTerminate() {
        // Clean up resources
        Log.d(TAG, "Application terminated")

        // Close the database if it's initialized
        if (this::database.isInitialized) {
            database.close()
        }

        // Close the model wrapper if it's initialized
        if (this::modelWrapper.isInitialized) {
            modelWrapper.close()
        }

        super.onTerminate()
    }
}