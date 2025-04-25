package com.example.coffee4n.ui.login


sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val userId: Int, val authToken: String, val isOwner: Boolean=false) : LoginState() // Thêm authToken
    data class Error(val message: String) : LoginState()
}