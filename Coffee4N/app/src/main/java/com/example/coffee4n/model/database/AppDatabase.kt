package com.example.coffee4n.model.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.coffee4n.model.*
import com.example.coffee4n.utils.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Database(
    entities = [
        Product::class,
        User::class,
        Order::class,
        Employee::class,
        Category::class,
        CartItem::class,
        OrderItem::class,
        Promotion::class,
        Table::class,
        Attendance::class,
        Booking::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun userDao(): UserDao
    abstract fun orderDao(): OrderDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun categoryDao(): CategoryDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun orderItemDao(): OrderItemDao
    abstract fun promotionDao(): PromotionDao
    abstract fun tableDao(): TableDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun bookingDao(): BookingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Return existing instance if available
            INSTANCE?.let { return it }

            // Create new instance in synchronized block
            return synchronized(this) {
                val instance = INSTANCE
                if (instance != null) {
                    instance
                } else {
                    val newInstance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "coffee4n_database_v2"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = newInstance
                    newInstance
                }
            }
        }

        fun initDatabase(context: Context, scope: CoroutineScope) {
            scope.launch(SupervisorJob() + Dispatchers.IO) {
                getDatabase(context)
            }
        }

        /**
         * Logs comprehensive information about all databases for the app
         */
        fun logDatabaseInfo(context: Context) {
            try {
                val startTime = System.currentTimeMillis()
                Log.d("DatabaseInfo", "╔══════════════════════════════════════╗")
                Log.d("DatabaseInfo", "║        DATABASE INFO REPORT         ║")
                Log.d("DatabaseInfo", "╚══════════════════════════════════════╝")

                val databases = context.databaseList().sorted()
                if (databases.isEmpty()) {
                    Log.d("DatabaseInfo", "No databases found for this app")
                    return
                }

                Log.d("DatabaseInfo", "\n📁 Discovered ${databases.size} database(s):")
                databases.forEachIndexed { index, dbName ->
                    Log.d("DatabaseInfo", "\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                    Log.d("DatabaseInfo", " DATABASE #${index + 1}: $dbName")
                    logDatabaseDetails(context, dbName)
                }

                val elapsedTime = System.currentTimeMillis() - startTime
                Log.d("DatabaseInfo", "\n⏱️ Report generated in ${elapsedTime}ms")
            } catch (e: Exception) {
                Log.e("DatabaseInfo", "❌ Error generating database report", e)
            }
        }

        private fun logDatabaseDetails(context: Context, dbName: String) {
            try {
                val dbPath = context.getDatabasePath(dbName).absolutePath
                val dbFile = File(dbPath)

                if (!dbFile.exists()) {
                    Log.d("DatabaseInfo", "⚠️ Database file does not exist")
                    return
                }

                // Basic info
                Log.d("DatabaseInfo", "├─ 📂 Path: $dbPath")
                Log.d("DatabaseInfo", "├─ 📏 Size: ${"%.2f".format(dbFile.length() / 1024.0)} KB")
                Log.d("DatabaseInfo", "├─ 📅 Last modified: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(dbFile.lastModified()))}")

                // Detailed info for main database
                if (dbName == "coffee4n_database_v2") {
                    logTablesInfo(context, dbName)
                }
            } catch (e: Exception) {
                Log.e("DatabaseInfo", "❌ Error logging database details", e)
            }
        }

        private fun logTablesInfo(context: Context, dbName: String) {
            var db: SupportSQLiteDatabase? = null
            try {
                db = getDatabase(context).openHelper.readableDatabase
                val tables = mutableListOf<String>()
                db.query(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'room_%'",
                    emptyArray()
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        tables.add(cursor.getString(0))
                    }
                }
                if (tables.isEmpty()) {
                    Log.d("DatabaseInfo", "└─ ❌ No tables found")
                    return
                }
                Log.d("DatabaseInfo", "└─ 🗂️ Tables (${tables.size}):")
                tables.sorted().forEach { tableName ->
                    logTableDetails(db, tableName)
                }
            } catch (e: Exception) {
                Log.e("DatabaseInfo", "❌ Error logging table info", e)
            } finally {
                db?.close()
            }
        }

        private fun logTableDetails(db: SupportSQLiteDatabase, tableName: String) {
            try {
                var recordCount = 0
                db.query("SELECT COUNT(*) FROM $tableName", emptyArray()).use { cursor ->
                    if (cursor.moveToFirst()) {
                        recordCount = cursor.getInt(0)
                    }
                }
                val columns = mutableListOf<String>()
                db.query("PRAGMA table_info($tableName)", emptyArray()).use { cursor ->
                    while (cursor.moveToNext()) {
                        val columnName = cursor.getString(1)
                        val columnType = cursor.getString(2)
                        columns.add("$columnName: $columnType")
                    }
                }
                Log.d("DatabaseInfo", "\n   ┌─ 📊 Table: $tableName")
                Log.d("DatabaseInfo", "   ├─ 📊 Records: $recordCount")
                Log.d("DatabaseInfo", "   └─ 🔠 Columns (${columns.size}):")
                columns.forEach { column ->
                    Log.d("DatabaseInfo", "      ◦ $column")
                }
            } catch (e: Exception) {
                Log.e("DatabaseInfo", "   ❌ Error logging table details", e)
            }
        }
    }
}