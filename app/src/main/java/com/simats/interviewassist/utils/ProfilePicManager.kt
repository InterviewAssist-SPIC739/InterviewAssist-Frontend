package com.simats.interviewassist.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ProfilePicManager {
    private const val PROFILE_PIC_FILE_NAME = "profile_picture.jpg"

    fun saveBase64Image(context: Context, base64String: String?, preferenceManager: PreferenceManager): String? {
        if (base64String.isNullOrEmpty()) {
            preferenceManager.saveProfilePicPath(null)
            return null
        }

        // If it's a URL or a short relative path, save it directly (Base64 is much longer)
        if (base64String.startsWith("http") || (base64String.length < 500 && (base64String.startsWith("/") || base64String.contains(".")))) {
            preferenceManager.saveProfilePicPath(base64String)
            return base64String
        }

        return try {
            val cleanBase64 = if (base64String.contains(",")) base64String.substringAfter(",") else base64String
            val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            saveBitmapToFile(context, bitmap, preferenceManager)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun saveBitmapToFile(context: Context, bitmap: Bitmap?, preferenceManager: PreferenceManager): String? {
        if (bitmap == null) {
            preferenceManager.saveProfilePicPath(null)
            return null
        }

        val file = File(context.filesDir, PROFILE_PIC_FILE_NAME)
        var fos: FileOutputStream? = null
        return try {
            fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            val path = file.absolutePath
            preferenceManager.saveProfilePicPath(path)
            path
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun getProfilePicFile(context: Context): File? {
        val file = File(context.filesDir, PROFILE_PIC_FILE_NAME)
        return if (file.exists()) file else null
    }
}
