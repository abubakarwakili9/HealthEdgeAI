package com.example.healthedgeai.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImageHelper(private val context: Context) {
    private val TAG = "ImageHelper"
    private var currentPhotoPath: String? = null

    /**
     * Create an image file in the app's private directory
     */
    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )

        // Save a file path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        return image
    }

    /**
     * Launch camera intent
     */
    fun dispatchTakePictureIntent(takePictureLauncher: ActivityResultLauncher<Intent>): Uri? {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Log.e(TAG, "Error creating image file", ex)
                return null
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    context,
                    "com.example.healthedgeai.fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePictureLauncher.launch(takePictureIntent)
                return photoURI
            }
        }
        return null
    }

    /**
     * Launch gallery intent
     */
    fun dispatchSelectPictureIntent(selectPictureLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectPictureLauncher.launch(intent)
    }

    /**
     * Process and save image from gallery
     */
    fun saveImageFromGallery(uri: Uri): String? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Create a file to save the image
            val imageFile = createImageFile()
            val outputStream = FileOutputStream(imageFile)

            // Compress and save the image
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()

            return imageFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image from gallery", e)
            return null
        }
    }

    /**
     * Get current photo path
     */
    fun getCurrentPhotoPath(): String? {
        return currentPhotoPath
    }
}