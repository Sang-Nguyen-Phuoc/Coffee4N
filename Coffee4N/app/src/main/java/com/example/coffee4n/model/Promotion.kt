package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "promotion")
data class Promotion(
    @PrimaryKey val id: Int,
    val code: String,
    val description: String,
    val discountType: String,
    val discountValue: Double,
    val startDate: Date,
    val endDate: Date,
    val isActive: Boolean = true
) {
    @Ignore
    fun isValid(): Boolean {
        val currentDate = Date()
        return isActive && currentDate.after(startDate) && currentDate.before(endDate)
    }

    companion object {
        private val isoFormatWithOffset = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        private val isoFormatWithZ = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun fromIsoString(dateStr: String?): Date {
            if (dateStr.isNullOrEmpty()) {
                println("Date string is null or empty, returning current date")
                return Date()
            }

            return try {
                isoFormatWithOffset.parse(dateStr) ?: Date()
            } catch (e: Exception) {
                try {
                    isoFormatWithZ.parse(dateStr) ?: Date()
                } catch (e2: Exception) {
                    println("Error parsing date string: $dateStr, errors: ${e.message}, ${e2.message}")
                    Date()
                }
            }
        }

        fun fromFirebaseTimestamp(timestamp: Long?): Date {
            return timestamp?.let { Date(it) } ?: Date()
        }
    }
}