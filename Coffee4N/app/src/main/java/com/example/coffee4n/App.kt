package com.example.coffee4n

import android.app.Application
import androidx.room.Room
import com.example.coffee4n.model.database.AppDatabase

class App : Application() {
    companion object {
        lateinit var database: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "coffee_db"
        ).build()
    }
}