package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties

@Entity(tableName = "user")
@IgnoreExtraProperties // Bỏ qua các trường không có trong class
data class User(
    @PrimaryKey val id: Int = 0, // Thêm giá trị mặc định
    val firebaseUid: String = "", // Thêm giá trị mặc định
    val username: String = "", // Thêm giá trị mặc định
    val email: String = "", // Thêm giá trị mặc định
    val phone: String = "", // Thêm giá trị mặc định
    val name: String = "", // Thêm giá trị mặc định
    val address: String = "" // Thêm giá trị mặc định
) {
    // Constructor không đối số để Firebase có thể tạo instance
    constructor() : this(0, "", "", "", "", "", "")
}
