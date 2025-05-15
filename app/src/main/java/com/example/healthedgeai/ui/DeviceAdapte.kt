package com.example.healthedgeai.ui

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthedgeai.R

class DeviceAdapter(private val onConnectClick: (BluetoothDevice) -> Unit) :
    RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    private val devices = mutableListOf<DeviceItem>()

    data class DeviceItem(
        val device: BluetoothDevice,
        val rssi: Int,
        val name: String?
    )

    // Add a device to the list
    fun addDevice(device: BluetoothDevice, rssi: Int, name: String?) {
        // Check if device is already in the list
        val existingIndex = devices.indexOfFirst { it.device.address == device.address }
        if (existingIndex >= 0) {
            // Update the device info
            devices[existingIndex] = DeviceItem(device, rssi, name)
            notifyItemChanged(existingIndex)
        } else {
            // Add new device
            devices.add(DeviceItem(device, rssi, name))
            notifyItemInserted(devices.size - 1)
        }
    }

    // Clear the device list
    fun clearDevices() {
        devices.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val deviceItem = devices[position]
        holder.bind(deviceItem)
    }

    override fun getItemCount(): Int = devices.size

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDeviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
        private val tvDeviceAddress: TextView = itemView.findViewById(R.id.tvDeviceAddress)
        private val tvSignalStrength: TextView = itemView.findViewById(R.id.tvSignalStrength)
        private val btnConnect: Button = itemView.findViewById(R.id.btnConnect)

        fun bind(deviceItem: DeviceItem) {
            // Set device name or "Unknown Device" if name is null
            val displayName = deviceItem.name ?: "Unknown Device"
            tvDeviceName.text = displayName

            // Set device address
            tvDeviceAddress.text = deviceItem.device.address

            // Set signal strength indicator
            val rssi = deviceItem.rssi
            val signalStrength = when {
                rssi > -60 -> "Excellent"
                rssi > -70 -> "Good"
                rssi > -80 -> "Fair"
                else -> "Poor"
            }
            tvSignalStrength.text = "Signal: $signalStrength ($rssi dBm)"

            // Set connect button click listener
            btnConnect.setOnClickListener {
                onConnectClick(deviceItem.device)
            }
        }
    }
}