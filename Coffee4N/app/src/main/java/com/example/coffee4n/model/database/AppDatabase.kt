package com.example.coffee4n.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.coffee4n.model.*
import com.example.coffee4n.utils.Converters

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
    version = 1,
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
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coffee4n_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}