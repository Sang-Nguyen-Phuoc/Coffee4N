package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "promotion")
data class Promotion(
    @PrimaryKey val id: Int,
    val code: String,
    val description: String,
    val discountType: String,
    val discountValue: Double,
    val startDate: Date,
    val endDate: Date
) {
    fun isValid(): Boolean {
        val currentDate = Date()
        return currentDate.after(startDate) && currentDate.before(endDate)
    }

    companion object {
        private val isoFormatWithOffset = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        private val isoFormatWithZ = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }

        fun fromIsoString(dateStr: String?): Date {
            if (dateStr.isNullOrEmpty()) {
                println("Date string is null or empty, returning current date")
                return Date()
            }

            return try {
                // Thử parse định dạng có offset (yyyy-MM-dd'T'HH:mm:ss+07:00)
                isoFormatWithOffset.parse(dateStr) ?: Date()
            } catch (e: Exception) {
                try {
                    // Nếu thất bại, thử parse định dạng ISO với Z (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
                    isoFormatWithZ.parse(dateStr) ?: Date()
                } catch (e2: Exception) {
                    println("Error parsing date string: $dateStr, errors: ${e.message}, ${e2.message}")
                    Date() // Trả về ngày hiện tại nếu parse thất bại
                }
            }
        }
    }
}