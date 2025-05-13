package com.example.healthedgeai.util

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.FloatBuffer
import java.nio.LongBuffer

class OnnxModelWrapper(private val context: Context) {

    private val TAG = "OnnxModelWrapper"
    private val MODEL_NAME = "random_forest_model_27f_v8_forced.onnx"
    private val LABEL_FILE = "label_classes.json"
    private val SCALER_FILE = "scaler.pkl.json"

    private lateinit var env: OrtEnvironment
    private var session: OrtSession? = null
    private lateinit var labelClasses: List<String>
    private var scalerParams: JsonObject? = null
    private var modelInitialized = false

    fun initialize() {
        try {
            // Load ONNX Runtime environment
            Log.d(TAG, "Initializing ONNX Runtime environment...")
            env = OrtEnvironment.getEnvironment()
            Log.d(TAG, "ONNX Runtime environment initialized successfully")

            // Check for model file in assets
            val assetsList = context.assets.list("")
            Log.d(TAG, "Assets directory contents: ${assetsList?.joinToString(", ")}")

            try {
                // Load model from assets
                Log.d(TAG, "Attempting to load model: $MODEL_NAME")
                val modelBytes = context.assets.open(MODEL_NAME).readBytes()
                Log.d(TAG, "Model file loaded successfully, size: ${modelBytes.size} bytes")

                try {
                    // Create session from model bytes
                    Log.d(TAG, "Creating ONNX session...")
                    session = env.createSession(modelBytes)
                    modelInitialized = true
                    Log.d(TAG, "ONNX session created successfully!")

                    // Show detailed model information
                    Log.d(TAG, "Model input count: ${session?.inputNames?.size}")
                    Log.d(TAG, "Model input names: ${session?.inputNames}")
                    Log.d(TAG, "Model output count: ${session?.outputNames?.size}")
                    Log.d(TAG, "Model output names: ${session?.outputNames}")

                } catch (e: Exception) {
                    Log.e(TAG, "Error creating ONNX session: ${e.message}")
                    Log.e(TAG, "Stack trace: ", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error opening model file from assets: ${e.message}")
                Log.e(TAG, "Stack trace: ", e)
            }

            // Load label classes (proceed even if model loading failed)
            try {
                Log.d(TAG, "Loading label classes from $LABEL_FILE...")
                loadLabelClasses()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading label classes", e)
            }

            // Load scaler parameters (proceed even if model loading failed)
            try {
                Log.d(TAG, "Loading scaler parameters from $SCALER_FILE...")
                loadScalerParams()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading scaler parameters", e)
            }

            if (modelInitialized) {
                Log.d(TAG, "Model initialization complete - ready for inference")
            } else {
                Log.w(TAG, "Model initialization incomplete - will use fallback for predictions")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in model initialization process", e)
        }
    }

    private fun loadLabelClasses() {
        try {
            // Check if file exists
            val labelFileExists = try {
                context.assets.open(LABEL_FILE).close()
                true
            } catch (e: Exception) {
                false
            }

            Log.d(TAG, "Label file exists: $labelFileExists")

            if (labelFileExists) {
                val jsonString = context.assets.open(LABEL_FILE).bufferedReader().use { it.readText() }
                Log.d(TAG, "Label file contents: $jsonString")

                val type = object : TypeToken<List<String>>() {}.type
                val loadedClasses = Gson().fromJson<List<String>>(jsonString, type)

                if (loadedClasses != null && loadedClasses.isNotEmpty()) {
                    labelClasses = loadedClasses
                    Log.d(TAG, "Loaded label classes successfully: $labelClasses")
                } else {
                    Log.w(TAG, "Label classes parsed as empty or null, using default values")
                    labelClasses = listOf("Critical", "Healthy", "Moderate")
                }
            } else {
                Log.w(TAG, "Label file not found, using default values")
                labelClasses = listOf("Critical", "Healthy", "Moderate")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading label classes: ${e.message}")
            // Use default classes as fallback
            labelClasses = listOf("Critical", "Healthy", "Moderate")
            Log.d(TAG, "Using default label classes: $labelClasses")
        }
    }

    private fun loadScalerParams() {
        try {
            // Check if file exists
            val scalerFileExists = try {
                context.assets.open(SCALER_FILE).close()
                true
            } catch (e: Exception) {
                false
            }

            Log.d(TAG, "Scaler file exists: $scalerFileExists")

            if (scalerFileExists) {
                val jsonString = context.assets.open(SCALER_FILE).bufferedReader().use { it.readText() }
                Log.d(TAG, "Scaler file content length: ${jsonString.length}")

                scalerParams = Gson().fromJson(jsonString, JsonObject::class.java)

                if (scalerParams != null) {
                    val hasScaleParams = scalerParams?.has("mean") == true && scalerParams?.has("scale") == true
                    Log.d(TAG, "Loaded scaler parameters successfully. Has scale params: $hasScaleParams")

                    if (hasScaleParams) {
                        // Fixed the error here, using size() instead of size
                        val meanSize = scalerParams?.getAsJsonArray("mean")?.size() ?: 0
                        val scaleSize = scalerParams?.getAsJsonArray("scale")?.size() ?: 0
                        Log.d(TAG, "Mean array size: $meanSize, Scale array size: $scaleSize")
                    }
                } else {
                    Log.w(TAG, "Scaler parameters parsed as null")
                }
            } else {
                Log.w(TAG, "Scaler file not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading scaler parameters: ${e.message}")
        }
    }

    fun runInference(features: FloatArray): String {
        if (!modelInitialized || session == null) {
            Log.w(TAG, "Model not initialized, using fallback prediction")
            return getFallbackPrediction(features)
        }

        try {
            Log.d(TAG, "Running inference with input features: ${features.joinToString(", ")}")

            // Preprocess input features
            val processedFeatures = applyScaling(features.clone())
            Log.d(TAG, "Processed features: ${processedFeatures.joinToString(", ")}")

            // Create input tensor
            val shape = longArrayOf(1, processedFeatures.size.toLong())
            Log.d(TAG, "Creating input tensor with shape: ${shape.joinToString(", ")}")

            val inputTensor = OnnxTensor.createTensor(
                env,
                FloatBuffer.wrap(processedFeatures),
                shape
            )
            Log.d(TAG, "Input tensor created successfully")

            // Run inference
            val inputs = mapOf("input" to inputTensor)
            Log.d(TAG, "Running inference with input names: ${inputs.keys}")

            val output = session!!.run(inputs)
            Log.d(TAG, "Inference completed successfully. Output count: ${output.size()}")

            // Get output tensor
            val outputTensor = output.get(0)
            Log.d(TAG, "Retrieved output tensor")

            // Process output tensor to get prediction
            val prediction = try {
                when (val value = outputTensor.value) {
                    is LongBuffer -> {
                        Log.d(TAG, "Output is LongBuffer")
                        val classIndex = value.get(0).toInt()
                        Log.d(TAG, "Predicted class index: $classIndex")

                        val result = if (classIndex >= 0 && classIndex < labelClasses.size) {
                            labelClasses[classIndex]
                        } else {
                            Log.w(TAG, "Class index out of bounds: $classIndex, classes size: ${labelClasses.size}")
                            getFallbackPrediction(features)
                        }
                        result
                    }
                    is FloatBuffer -> {
                        Log.d(TAG, "Output is FloatBuffer")
                        val probabilities = FloatArray(value.capacity())
                        value.get(probabilities)

                        Log.d(TAG, "Probabilities: ${probabilities.joinToString(", ")}")

                        // Get index of highest probability
                        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
                        Log.d(TAG, "Max probability index: $maxIndex")

                        val result = if (maxIndex >= 0 && maxIndex < labelClasses.size) {
                            labelClasses[maxIndex]
                        } else {
                            Log.w(TAG, "Max index out of bounds: $maxIndex, classes size: ${labelClasses.size}")
                            getFallbackPrediction(features)
                        }
                        result
                    }
                    else -> {
                        Log.e(TAG, "Unexpected output tensor value type: ${value.javaClass.name}")
                        getFallbackPrediction(features)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing output tensor", e)
                getFallbackPrediction(features)
            }

            Log.d(TAG, "Final prediction: $prediction")
            return prediction
        } catch (e: Exception) {
            Log.e(TAG, "Error running inference", e)
            return getFallbackPrediction(features)
        }
    }

    private fun getFallbackPrediction(features: FloatArray): String {
        Log.d(TAG, "Using fallback prediction logic")
        // Simple rules based on health metrics
        try {
            if (features.size < 2) {
                Log.w(TAG, "Not enough features for fallback prediction, using default 'Healthy'")
                return "Healthy"
            }

            val temperature = features[0]  // Assuming first feature is temperature
            val heartRate = features[1]    // Assuming second feature is heart rate

            Log.d(TAG, "Fallback using temperature: $temperature, heart rate: $heartRate")

            return when {
                temperature > 38.0f || heartRate > 100 -> {
                    Log.d(TAG, "Fallback prediction: Critical (high temp/HR)")
                    "Critical"
                }
                temperature > 37.5f || heartRate > 90 -> {
                    Log.d(TAG, "Fallback prediction: Moderate (elevated temp/HR)")
                    "Moderate"
                }
                else -> {
                    Log.d(TAG, "Fallback prediction: Healthy (normal temp/HR)")
                    "Healthy"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in fallback prediction logic", e)
            return "Healthy" // Default fallback
        }
    }

    private fun applyScaling(features: FloatArray): FloatArray {
        // If we have scaler parameters, apply scaling
        if (scalerParams != null) {
            try {
                val mean = scalerParams?.getAsJsonArray("mean")
                val scale = scalerParams?.getAsJsonArray("scale")

                if (mean != null && scale != null) {
                    Log.d(TAG, "Applying scaling with mean and scale arrays")

                    for (i in features.indices) {
                        // THIS IS THE FIX: Correctly use size() method instead of a property
                        if (i < mean.size() && i < scale.size()) {
                            val meanValue = mean.get(i).asFloat
                            val scaleValue = scale.get(i).asFloat
                            val originalValue = features[i]
                            features[i] = (originalValue - meanValue) / scaleValue

                            Log.d(TAG, "Feature $i: $originalValue -> ${features[i]} (mean=$meanValue, scale=$scaleValue)")
                        }
                    }
                } else {
                    Log.w(TAG, "Mean or scale array is null, skipping preprocessing")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error preprocessing features", e)
            }
        } else {
            Log.d(TAG, "No scaler parameters available, using raw features")
        }

        return features
    }

    fun close() {
        try {
            Log.d(TAG, "Closing ONNX model resources")
            session?.close()
            env.close()
            Log.d(TAG, "ONNX resources closed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing model resources", e)
        }
    }
}