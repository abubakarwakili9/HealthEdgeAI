package com.example.healthedgeai.util

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Manager class for handling Bluetooth LE connections to healthcare devices.
 * Handles connection state management, GATT operations, and data reception.
 */
class BluetoothConnectionManager(private val context: Context) {

    companion object {
        private const val TAG = "BluetoothConnManager"

        // Client Characteristic Configuration Descriptor
        private val CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        // Reconnection parameters
        private const val MAX_RECONNECT_ATTEMPTS = 3
        private const val BASE_RECONNECT_DELAY_MS = 1000L

        // Connection timeout
        private const val CONNECTION_TIMEOUT_MS = 10000L // 10 seconds
    }

    // Connection states
    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    }

    // Interface for connection events
    interface ConnectionCallback {
        fun onConnectionStateChange(device: BluetoothDevice, state: ConnectionState)
        fun onServicesDiscovered(gatt: BluetoothGatt, status: Int)
        fun onDataReceived(serviceUuid: UUID, characteristicUuid: UUID, data: ByteArray)
    }

    // Current state
    private var connectionState = ConnectionState.DISCONNECTED
    private var bluetoothGatt: BluetoothGatt? = null
    private var connectionCallback: ConnectionCallback? = null
    private var reconnectAttempts = 0
    private val handler = Handler(Looper.getMainLooper())
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Connection timeout reference
    private var connectionTimeoutRunnable: Runnable? = null

    // Store enabled notifications to restore them on reconnection
    private val enabledNotifications = ConcurrentHashMap<UUID, MutableSet<UUID>>()

    /**
     * Connect to a Bluetooth device.
     * This operation is performed on a background thread to prevent UI freezing.
     *
     * @param device The Bluetooth device to connect to
     * @param callback The callback to receive connection events
     * @return true if connection process started, false otherwise
     */
    fun connect(device: BluetoothDevice, callback: ConnectionCallback): Boolean {
        Log.d(TAG, "Connecting to device: ${device.address}")
        connectionCallback = callback

        // If already connected to this device, return success
        if (connectionState == ConnectionState.CONNECTED &&
            bluetoothGatt?.device?.address == device.address) {
            Log.d(TAG, "Already connected to this device")
            return true
        }

        // If we have an existing connection, close it first
        if (bluetoothGatt != null) {
            Log.d(TAG, "Closing existing connection before connecting to new device")
            close()
        }

        // Update state immediately
        connectionState = ConnectionState.CONNECTING
        connectionCallback?.onConnectionStateChange(device, connectionState)

        // Set a timeout for connection
        setConnectionTimeout(device)

        // Connect on a background thread
        coroutineScope.launch {
            try {
                Log.d(TAG, "Establishing connection to ${device.address}")
                // Reset reconnect attempts
                reconnectAttempts = 0

                withContext(Dispatchers.Main) {
                    // connectGatt must be called on main thread in some Android versions
                    bluetoothGatt = device.connectGatt(context, false, gattCallback)
                }

                if (bluetoothGatt == null) {
                    Log.e(TAG, "Failed to connect - connectGatt returned null")
                    // Update state on failure
                    connectionState = ConnectionState.DISCONNECTED
                    withContext(Dispatchers.Main) {
                        connectionCallback?.onConnectionStateChange(device, connectionState)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to device", e)
                // Set state back to disconnected on error
                connectionState = ConnectionState.DISCONNECTED
                withContext(Dispatchers.Main) {
                    connectionCallback?.onConnectionStateChange(device, connectionState)
                }
            }
        }

        return true
    }

    /**
     * Set a timeout for the connection attempt
     */
    private fun setConnectionTimeout(device: BluetoothDevice) {
        // Cancel any existing timeout
        connectionTimeoutRunnable?.let { handler.removeCallbacks(it) }

        // Create new timeout
        connectionTimeoutRunnable = Runnable {
            if (connectionState == ConnectionState.CONNECTING) {
                Log.e(TAG, "Connection attempt timed out after ${CONNECTION_TIMEOUT_MS}ms")
                // Force disconnect
                connectionState = ConnectionState.DISCONNECTED
                close()
                // Notify callback
                connectionCallback?.onConnectionStateChange(device, connectionState)
            }
        }

        // Schedule timeout
        connectionTimeoutRunnable?.let {
            handler.postDelayed(it, CONNECTION_TIMEOUT_MS)
        }
    }

    /**
     * Cancel the connection timeout
     */
    private fun cancelConnectionTimeout() {
        connectionTimeoutRunnable?.let { handler.removeCallbacks(it) }
        connectionTimeoutRunnable = null
    }

    /**
     * Disconnect from current device
     */
    fun disconnect() {
        if ((connectionState == ConnectionState.CONNECTED ||
                    connectionState == ConnectionState.CONNECTING) &&
            bluetoothGatt != null) {

            Log.d(TAG, "Disconnecting from device")
            connectionState = ConnectionState.DISCONNECTING
            val device = bluetoothGatt?.device
            if (device != null) {
                connectionCallback?.onConnectionStateChange(device, connectionState)
            }

            // Cancel any pending reconnection
            handler.removeCallbacksAndMessages(null)

            coroutineScope.launch {
                try {
                    withContext(Dispatchers.Main) {
                        bluetoothGatt?.disconnect()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during disconnect", e)
                    // Force close in case of error
                    close()
                }
            }
        } else {
            Log.d(TAG, "Disconnect called but not in a connected state")
        }
    }

    /**
     * Close GATT client completely
     */
    fun close() {
        Log.d(TAG, "Closing GATT connection")
        // Cancel timeout and reconnection attempts
        cancelConnectionTimeout()
        handler.removeCallbacksAndMessages(null)

        coroutineScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    bluetoothGatt?.close()
                }
                bluetoothGatt = null
                connectionState = ConnectionState.DISCONNECTED
                Log.d(TAG, "GATT connection closed")
            } catch (e: Exception) {
                Log.e(TAG, "Error closing GATT connection", e)
            }
        }
    }

    /**
     * GATT callback for handling Bluetooth events
     */
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val device = gatt.device
            Log.d(TAG, "onConnectionStateChange: status=$status, newState=$newState for ${device.address}")

            // Cancel connection timeout since we got a response
            cancelConnectionTimeout()

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        connectionState = ConnectionState.CONNECTED
                        Log.i(TAG, "Connected to GATT server: ${device.address}")

                        // Reset reconnect attempts
                        reconnectAttempts = 0

                        // Discover services after a short delay to ensure connection stability
                        handler.postDelayed({
                            try {
                                if (connectionState == ConnectionState.CONNECTED && bluetoothGatt != null) {
                                    Log.d(TAG, "Discovering services...")
                                    bluetoothGatt?.discoverServices()
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error discovering services", e)
                                // Update state on error
                                connectionState = ConnectionState.DISCONNECTED
                                handler.post {
                                    connectionCallback?.onConnectionStateChange(device, connectionState)
                                }
                            }
                        }, 600) // 600ms delay before service discovery

                        // Notify callback immediately about connection
                        handler.post {
                            connectionCallback?.onConnectionStateChange(device, connectionState)
                        }
                    } else {
                        Log.e(TAG, "Connection failed with status: $status")
                        connectionState = ConnectionState.DISCONNECTED

                        // Notify callback about failure
                        handler.post {
                            connectionCallback?.onConnectionStateChange(device, connectionState)
                        }

                        // Close the GATT connection
                        try {
                            gatt.close()
                            bluetoothGatt = null
                        } catch (e: Exception) {
                            Log.e(TAG, "Error closing GATT after connection failure", e)
                        }
                    }
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from GATT server: ${device.address}")

                    // Check if this was an intentional disconnect
                    val wasIntentional = connectionState == ConnectionState.DISCONNECTING

                    // Update state
                    connectionState = ConnectionState.DISCONNECTED

                    // Notify callback immediately about disconnection
                    handler.post {
                        connectionCallback?.onConnectionStateChange(device, connectionState)
                    }

                    // Only attempt reconnection if not explicitly disconnecting
                    if (!wasIntentional && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                        reconnectAttempts++
                        Log.d(TAG, "Scheduling reconnection attempt $reconnectAttempts")

                        // Calculate delay with exponential backoff
                        val delay = BASE_RECONNECT_DELAY_MS * (1L shl (reconnectAttempts - 1))

                        handler.postDelayed({
                            if (connectionState == ConnectionState.DISCONNECTED) {
                                Log.d(TAG, "Attempting reconnection to ${device.address}")
                                try {
                                    connectionState = ConnectionState.CONNECTING
                                    handler.post {
                                        connectionCallback?.onConnectionStateChange(device, connectionState)
                                    }
                                    bluetoothGatt = device.connectGatt(context, false, this)

                                    // Set a timeout for the reconnection attempt
                                    setConnectionTimeout(device)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error during reconnection attempt", e)
                                    connectionState = ConnectionState.DISCONNECTED
                                    handler.post {
                                        connectionCallback?.onConnectionStateChange(device, connectionState)
                                    }
                                }
                            }
                        }, delay)
                    } else if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                        Log.d(TAG, "Max reconnect attempts reached")
                        reconnectAttempts = 0

                        // Close GATT client
                        close()
                    } else {
                        Log.d(TAG, "Intentional disconnect, not attempting reconnection")
                        // Close GATT resources
                        try {
                            gatt.close()
                            bluetoothGatt = null
                        } catch (e: Exception) {
                            Log.e(TAG, "Error closing GATT after disconnect", e)
                        }
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Services discovered on ${gatt.device.address}")

                // Log discovered services for debugging
                gatt.services.forEach { service ->
                    Log.d(TAG, "Service discovered: ${service.uuid}")
                    service.characteristics.forEach { characteristic ->
                        Log.d(TAG, "  Characteristic: ${characteristic.uuid}")
                    }
                }

                // Notify callback on main thread
                handler.post {
                    connectionCallback?.onServicesDiscovered(gatt, status)
                }

                // Restore enabled notifications
                restoreEnabledNotifications(gatt)
            } else {
                Log.w(TAG, "Service discovery failed with status: $status")

                // Notify callback on main thread
                handler.post {
                    connectionCallback?.onServicesDiscovered(gatt, status)
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val serviceUuid = characteristic.service.uuid
            val characteristicUuid = characteristic.uuid
            val data = characteristic.value

            Log.d(TAG, "Characteristic changed: ${characteristicUuid.toString().take(8)}...")

            // Notify callback on main thread
            handler.post {
                connectionCallback?.onDataReceived(serviceUuid, characteristicUuid, data)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val serviceUuid = characteristic.service.uuid
                val characteristicUuid = characteristic.uuid
                val data = characteristic.value

                Log.d(TAG, "Characteristic read successful: ${characteristicUuid.toString().take(8)}...")

                // Notify callback on main thread
                handler.post {
                    connectionCallback?.onDataReceived(serviceUuid, characteristicUuid, data)
                }
            } else {
                Log.e(TAG, "Characteristic read failed with status: $status")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            val characteristic = descriptor.characteristic
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Descriptor write successful for characteristic: ${characteristic.uuid}")

                // If this was a notification descriptor, add it to our stored set
                if (descriptor.uuid == CLIENT_CHARACTERISTIC_CONFIG) {
                    val serviceUuid = characteristic.service.uuid
                    val charUuid = characteristic.uuid

                    val descriptorValue = descriptor.value
                    if (descriptorValue.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) ||
                        descriptorValue.contentEquals(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)) {

                        // Store for reconnection
                        val characteristics = enabledNotifications.getOrPut(serviceUuid) { mutableSetOf() }
                        characteristics.add(charUuid)

                        Log.d(TAG, "Added notification for ${charUuid} to reconnection store")
                    }
                }
            } else {
                Log.w(TAG, "Descriptor write failed with status: $status for characteristic: ${characteristic.uuid}")
            }
        }
    }

    /**
     * Enable notifications for a characteristic
     */
    fun enableNotifications(serviceUuid: UUID, characteristicUuid: UUID): Boolean {
        val gatt = bluetoothGatt ?: return false

        if (connectionState != ConnectionState.CONNECTED) {
            Log.e(TAG, "Cannot enable notifications when not connected")
            return false
        }

        try {
            Log.d(TAG, "Enabling notifications for service: $serviceUuid, char: $characteristicUuid")

            val service = gatt.getService(serviceUuid)
            if (service == null) {
                Log.e(TAG, "Service not found: $serviceUuid")
                return false
            }

            val characteristic = service.getCharacteristic(characteristicUuid)
            if (characteristic == null) {
                Log.e(TAG, "Characteristic not found: $characteristicUuid")
                return false
            }

            // Enable local notifications
            if (!gatt.setCharacteristicNotification(characteristic, true)) {
                Log.e(TAG, "Failed to enable local notifications")
                return false
            }

            // Get the descriptor
            val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
            if (descriptor == null) {
                Log.e(TAG, "Client Config Descriptor not found")
                return false
            }

            // Determine if characteristic supports notifications or indications
            val properties = characteristic.properties
            val supportsNotification = (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
            val supportsIndication = (properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0

            // Write descriptor
            val value = when {
                supportsIndication -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                supportsNotification -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                else -> {
                    Log.e(TAG, "Characteristic doesn't support notifications or indications")
                    return false
                }
            }

            descriptor.value = value
            val success = gatt.writeDescriptor(descriptor)

            Log.d(TAG, "Write descriptor result: $success")
            return success
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling notifications", e)
            return false
        }
    }

    /**
     * Read a characteristic
     */
    fun readCharacteristic(serviceUuid: UUID, characteristicUuid: UUID): Boolean {
        val gatt = bluetoothGatt ?: return false

        if (connectionState != ConnectionState.CONNECTED) {
            Log.e(TAG, "Cannot read characteristic when not connected")
            return false
        }

        try {
            val service = gatt.getService(serviceUuid)
            if (service == null) {
                Log.e(TAG, "Service not found: $serviceUuid")
                return false
            }

            val characteristic = service.getCharacteristic(characteristicUuid)
            if (characteristic == null) {
                Log.e(TAG, "Characteristic not found: $characteristicUuid")
                return false
            }

            return gatt.readCharacteristic(characteristic)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading characteristic", e)
            return false
        }
    }

    /**
     * Restore previously enabled notifications after reconnection
     */
    private fun restoreEnabledNotifications(gatt: BluetoothGatt) {
        Log.d(TAG, "Restoring enabled notifications...")

        if (enabledNotifications.isEmpty()) {
            Log.d(TAG, "No notifications to restore")
            return
        }

        for ((serviceUuid, characteristicUuids) in enabledNotifications) {
            val service = gatt.getService(serviceUuid)
            if (service == null) {
                Log.w(TAG, "Service $serviceUuid not found for notification restoration")
                continue
            }

            for (characteristicUuid in characteristicUuids) {
                val characteristic = service.getCharacteristic(characteristicUuid)
                if (characteristic == null) {
                    Log.w(TAG, "Characteristic $characteristicUuid not found for notification restoration")
                    continue
                }

                // Re-enable notification
                try {
                    if (!gatt.setCharacteristicNotification(characteristic, true)) {
                        Log.e(TAG, "Failed to re-enable local notifications for $characteristicUuid")
                        continue
                    }

                    // Get the descriptor
                    val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
                    if (descriptor == null) {
                        Log.e(TAG, "Client Config Descriptor not found for $characteristicUuid")
                        continue
                    }

                    // Write descriptor
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)

                    Log.d(TAG, "Restored notification for $characteristicUuid")
                } catch (e: Exception) {
                    Log.e(TAG, "Error restoring notification for $characteristicUuid", e)
                }
            }
        }
    }

    /**
     * Get current connection state
     */
    fun getConnectionState(): ConnectionState {
        return connectionState
    }

    /**
     * Get connected device
     */
    fun getConnectedDevice(): BluetoothDevice? {
        return bluetoothGatt?.device
    }

    /**
     * Get list of available services
     */
    fun getAvailableServices(): List<BluetoothGattService>? {
        return bluetoothGatt?.services
    }
}