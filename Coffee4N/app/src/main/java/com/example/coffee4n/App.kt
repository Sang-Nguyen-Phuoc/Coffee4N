package com.example.coffee4n

import android.app.Application
import com.example.coffee4n.model.database.AppDatabase
import com.example.coffee4n.utils.Cloudinary.initCloudinary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class App : Application() {
    // No need for applicationScope or database initialization here
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onCreate() {
        super.onCreate()
        // Any other app initialization can go here
        AppDatabase.initDatabase(this, applicationScope)
        initCloudinary(this)
    }
}