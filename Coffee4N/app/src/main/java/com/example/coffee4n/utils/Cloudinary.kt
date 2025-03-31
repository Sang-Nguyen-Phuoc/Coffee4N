package com.example.coffee4n.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object Cloudinary {

    private const val TAG = "Cloudinary"
    private var isInitialized = false

    @Synchronized
    fun initCloudinary(context: Context) {
        if (!isInitialized) {
            val config = mapOf(
                "cloud_name" to "dizp8jtoi",
                "api_key" to "123273799372117",
                "api_secret" to "rbVIYo9jeLF-HAMF-zaTpbYk_eI"
            )
            MediaManager.init(context.applicationContext, config)
            isInitialized = true
            Log.d(TAG, "Cloudinary đã được khởi tạo.")
        }
    }

    fun uploadImageToCloudinary(
        context: Context,
        imageUri: Uri,
        folder: String = "Home/CoffeeProductImage",
        uploadPreset: String? = "coffee_upload",
        onSuccess: (String, String) -> Unit = { _, _ -> },
        onError: (String) -> Unit = {},
        onProgress: (Int) -> Unit = {}
    ) {
        if (!isInitialized) {
            onError("Cloudinary chưa được khởi tạo. Vui lòng gọi initCloudinary trước.")
            Log.e(TAG, "Cloudinary chưa được khởi tạo.")
            return
        }


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = uriToFile(context, imageUri)
                if (file != null) {
                    val request = MediaManager.get().upload(file.absolutePath)
                        .option("folder", folder)
                        .apply { uploadPreset?.let { option("upload_preset", it) } }
                        .callback(object : UploadCallback {
                            override fun onStart(requestId: String) {
                                Log.d(TAG, "Bắt đầu tải lên: $requestId")
                            }

                            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                                val progress = (bytes * 100 / totalBytes).toInt()
                                CoroutineScope(Dispatchers.Main).launch { onProgress(progress) }
                                Log.d(TAG, "Đang tải: $progress%")
                            }

                            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                                val publicId = resultData["public_id"] as String
                                val secureUrl = resultData["secure_url"] as String
                                CoroutineScope(Dispatchers.Main).launch { onSuccess(publicId, secureUrl) }
                                Log.d(TAG, "Tải lên thành công! Public ID: $publicId, URL: $secureUrl")
                                file.delete() // Xóa tệp tạm sau khi tải lên
                            }

                            override fun onError(requestId: String, error: ErrorInfo) {
                                CoroutineScope(Dispatchers.Main).launch { onError(error.description) }
                                Log.e(TAG, "Lỗi tải lên: ${error.description}")
                                file.delete() // Xóa tệp tạm nếu lỗi
                            }

                            override fun onReschedule(requestId: String, error: ErrorInfo) {
                                Log.d(TAG, "Tái lập lịch tải lên: ${error.description}")
                            }
                        })


                    request.option("timeout", 30000)
                    request.dispatch()
                } else {
                    CoroutineScope(Dispatchers.Main).launch { onError("Không thể chuyển Uri thành tệp.") }
                    Log.e(TAG, "Không thể chuyển Uri thành tệp.")
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch { onError("Lỗi không xác định: ${e.message}") }
                Log.e(TAG, "Lỗi không xác định: ${e.message}", e)
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: IOException) {
            Log.e(TAG, "Lỗi khi chuyển Uri thành tệp: ${e.message}", e)
            null
        }
    }
}