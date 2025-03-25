package com.example.coffee4n.utils

import android.content.Context
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.io.File
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object Cloudinary {
    private const val TAG = "CloudinaryUtil"
    private const val CLOUDINARY_FOLDER = "Home/CoffeeProductImage"

    fun initCloudinary(context: Context) {
        val config = HashMap<String, String>()
        config["cloud_name"] = "dizp8jtoi"
        config["api_key"] = "123273799372117"
        config["api_secret"] = "rbVIYo9jeLF-HAMF-zaTpbYk_eI"
        MediaManager.init(context, config)
    }

    data class UploadResult(
        val url: String? = null,
        val error: String? = null
    )

    suspend fun uploadProductImage(
        productId: String,
        imageFile: File,
        onProgress: (Int) -> Unit = {}
    ): UploadResult = suspendCancellableCoroutine { continuation ->
        val filePath = imageFile.absolutePath

        MediaManager.get().upload(filePath)
            .option("public_id", productId)
            .option("folder", CLOUDINARY_FOLDER)
            .unsigned("ml_default")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d(TAG, "Upload started for product: $productId")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    val progress = (bytes * 100 / totalBytes).toInt()
                    onProgress(progress)
                    Log.d(TAG, "Upload progress: $progress%")
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    Log.d(TAG, "Upload successful: $secureUrl")
                    continuation.resume(UploadResult(url = secureUrl))
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e(TAG, "Upload error: ${error.description}")
                    continuation.resume(UploadResult(error = error.description))
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.w(TAG, "Upload rescheduled: ${error.description}")
                }
            })
            .dispatch()
    }
}
