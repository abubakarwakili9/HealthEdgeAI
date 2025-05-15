package com.example.healthedgeai.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.healthedgeai.util.PermissionManager

abstract class BluetoothBaseActivity : AppCompatActivity() {

    protected lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = PermissionManager(this)
    }

    // Check and request permissions if needed
    protected fun ensureBluetoothPermissions(): Boolean {
        if (!permissionManager.hasRequiredPermissions()) {
            permissionManager.requestPermissions(this)
            return false
        }
        return true
    }

    // Handle permission results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PermissionManager.REQUEST_BLUETOOTH_PERMISSIONS -> {
                if (grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // All permissions granted
                    onBluetoothPermissionsGranted()
                } else {
                    // Permission denied
                    onBluetoothPermissionsDenied()
                }
            }
            PermissionManager.REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permission granted
                    onLocationPermissionGranted()
                } else {
                    // Location permission denied
                    onLocationPermissionDenied()
                }
            }
        }
    }

    // Override these in subclasses to handle permission states
    protected open fun onBluetoothPermissionsGranted() {}

    protected open fun onBluetoothPermissionsDenied() {
        Toast.makeText(
            this,
            "Bluetooth permissions are required for device connectivity",
            Toast.LENGTH_LONG
        ).show()
    }

    protected open fun onLocationPermissionGranted() {}

    protected open fun onLocationPermissionDenied() {
        Toast.makeText(
            this,
            "Location permission is required for Bluetooth scanning",
            Toast.LENGTH_LONG
        ).show()
    }
}