package com.example.coffee4n

import android.app.Application
import com.example.coffee4n.model.database.AppDatabase
import com.example.coffee4n.utils.Cloudinary.initCloudinary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class App : Application() {
    // Singleton instance of the database
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Any other app initialization can go here
        // Optional: Pre-initialize the database on a background thread
        val applicationScope = CoroutineScope(SupervisorJob() +  Dispatchers.IO)
        applicationScope.launch {
            database // Trigger lazy initialization
        }
        initCloudinary(this)
    }
}