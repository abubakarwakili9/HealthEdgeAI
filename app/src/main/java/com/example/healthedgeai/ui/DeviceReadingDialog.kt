package com.example.healthedgeai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.healthedgeai.databinding.DialogDeviceReadingBinding
import com.example.healthedgeai.util.BiometricDeviceManager
import java.text.DecimalFormat

class DeviceReadingDialog : DialogFragment(), BiometricDeviceManager.ReadingCallback {

    private var _binding: DialogDeviceReadingBinding? = null
    private val binding get() = _binding!!

    private lateinit var deviceManager: BiometricDeviceManager
    private lateinit var deviceType: BiometricDeviceManager.DeviceType

    private var value: Float = 0f
    private var secondaryValue: Float? = null

    // Callback interface for receiving reading values
    interface DeviceReadingListener {
        fun onReadingConfirmed(deviceType: BiometricDeviceManager.DeviceType, value: Float, secondaryValue: Float? = null)
    }

    private var listener: DeviceReadingListener? = null

    companion object {
        private const val ARG_DEVICE_TYPE = "deviceType"

        fun newInstance(deviceType: BiometricDeviceManager.DeviceType): DeviceReadingDialog {
            val fragment = DeviceReadingDialog()
            val args = Bundle()
            args.putSerializable(ARG_DEVICE_TYPE, deviceType)
            fragment.arguments = args
            return fragment
        }
    }

    fun setDeviceReadingListener(listener: DeviceReadingListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_MinWidth)

        deviceType = arguments?.getSerializable(ARG_DEVICE_TYPE) as BiometricDeviceManager.DeviceType
        deviceManager = BiometricDeviceManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDeviceReadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        startDeviceReading()
    }

    private fun setupUI() {
        // Set title based on device type
        val title = when (deviceType) {
            BiometricDeviceManager.DeviceType.THERMOMETER -> "Reading Temperature"
            BiometricDeviceManager.DeviceType.HEART_RATE_MONITOR -> "Reading Heart Rate"
            BiometricDeviceManager.DeviceType.BLOOD_PRESSURE_MONITOR -> "Reading Blood Pressure"
            BiometricDeviceManager.DeviceType.OXYGEN_SATURATION_MONITOR -> "Reading Oxygen Saturation"
            BiometricDeviceManager.DeviceType.GLUCOSE_METER -> "Reading Blood Glucose"
        }
        binding.tvTitle.text = title

        // Set up buttons
        binding.btnUseReading.setOnClickListener {
            listener?.onReadingConfirmed(deviceType, value, secondaryValue)
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun startDeviceReading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvStatus.text = "Connecting to device..."
        binding.layoutResult.visibility = View.GONE
        binding.btnUseReading.visibility = View.GONE

        deviceManager.startReading(deviceType, this)
    }

    override fun onReadingReceived(
        deviceType: BiometricDeviceManager.DeviceType,
        value: Float,
        secondaryValue: Float?
    ) {
        this.value = value
        this.secondaryValue = secondaryValue

        activity?.runOnUiThread {
            binding.progressBar.visibility = View.GONE
            binding.layoutResult.visibility = View.VISIBLE
            binding.btnUseReading.visibility = View.VISIBLE

            val df = DecimalFormat("#.#")

            when (deviceType) {
                BiometricDeviceManager.DeviceType.THERMOMETER -> {
                    binding.tvResultLabel.text = "Temperature:"
                    binding.tvResultValue.text = "${df.format(value)}°C"
                }
                BiometricDeviceManager.DeviceType.HEART_RATE_MONITOR -> {
                    binding.tvResultLabel.text = "Heart Rate:"
                    binding.tvResultValue.text = "${df.format(value)} bpm"
                }
                BiometricDeviceManager.DeviceType.BLOOD_PRESSURE_MONITOR -> {
                    binding.tvResultLabel.text = "Blood Pressure:"
                    binding.tvResultValue.text = "${df.format(value)} / ${df.format(secondaryValue ?: 0f)} mmHg"
                    // Hide secondary value text since we're displaying both in the primary value
                    binding.tvResultSecondary.visibility = View.GONE
                }
                BiometricDeviceManager.DeviceType.OXYGEN_SATURATION_MONITOR -> {
                    binding.tvResultLabel.text = "Oxygen Saturation:"
                    binding.tvResultValue.text = "${df.format(value)}%"
                }
                BiometricDeviceManager.DeviceType.GLUCOSE_METER -> {
                    binding.tvResultLabel.text = "Blood Glucose:"
                    binding.tvResultValue.text = "${df.format(value)} mg/dL"
                }
            }

            binding.tvStatus.text = "Reading successful!"
        }
    }

    override fun onError(deviceType: BiometricDeviceManager.DeviceType, message: String) {
        activity?.runOnUiThread {
            binding.progressBar.visibility = View.GONE
            binding.tvStatus.text = "Error: $message"

            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}