package com.example.coffee4n.model

import com.google.firebase.database.IgnoreExtraProperties

data class User(
    val id: Int = 0,
    val firebaseUid: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val name: String = "",
    val address: String = ""
) {
    // Constructor không đối số để Firebase có thể tạo instance
    constructor() : this(0, "", "", "", "", "", "")
}
