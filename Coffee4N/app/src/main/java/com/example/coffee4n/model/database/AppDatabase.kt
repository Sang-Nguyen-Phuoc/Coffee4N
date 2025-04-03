package com.example.coffee4n.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.coffee4n.model.*
import com.example.coffee4n.utils.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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
    version = 4,
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
            // Return instance immediately if already initialized
            INSTANCE?.let { return it }

            // Otherwise create the database on IO thread
            return synchronized(this) {
                // Double check inside synchronized block
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

        // Optional: Add a method for pre-initializing database in a coroutine scope
        // This can be called from App.onCreate if you want to start database init early
        fun initDatabase(context: Context, scope: CoroutineScope) {
            scope.launch(SupervisorJob() +  Dispatchers.IO) {
                getDatabase(context)
            }
        }
    }
}