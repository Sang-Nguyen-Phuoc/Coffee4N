package com.example.coffee4n.utils

import java.text.NumberFormat
import java.util.Date
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class Converters {
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        return format.format(amount)
    }

    fun stringToTimestamp(dateTimeString: String, pattern: String = "yyyy-MM-dd HH:mm"): Long {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        val localDateTime = LocalDateTime.parse(dateTimeString, formatter)
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun timestampToString(timestamp: Long, pattern: String = "yyyy-MM-dd HH:mm"): String {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        val localDateTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
        return localDateTime.format(formatter)
    }
}