package com.example.healthedgeai.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val context: Context) {

    companion object {
        const val REQUEST_BLUETOOTH_PERMISSIONS = 100
        const val REQUEST_LOCATION_PERMISSION = 101

        // Helper method to check if we're on Android 12 or higher
        fun isAndroid12OrHigher(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    // Get required permissions based on Android version
    fun getRequiredBluetoothPermissions(): Array<String> {
        return if (isAndroid12OrHigher()) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    // Check if all permissions are granted
    fun hasRequiredPermissions(): Boolean {
        return getRequiredBluetoothPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Request permissions
    fun requestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            getRequiredBluetoothPermissions(),
            REQUEST_BLUETOOTH_PERMISSIONS
        )
    }

    // Check location permission specifically (needed for BLE scanning on older Android versions)
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request location permission only
    fun requestLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }
}