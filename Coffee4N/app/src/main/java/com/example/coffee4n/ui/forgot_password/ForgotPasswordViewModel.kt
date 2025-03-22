package com.example.coffee4n.ui.forgot_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(private val userRepository: UserRepository) : ViewModel() {
    // Trạng thái email
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    // Kết quả gửi OTP (true nếu thành công, false nếu thất bại)
    private val _sendOTPResult = MutableStateFlow<Boolean?>(null)
    val sendOTPResult: StateFlow<Boolean?> = _sendOTPResult

    // Cập nhật email
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    // Gửi mã OTP
    fun sendOTP() {
        viewModelScope.launch {
            val result = userRepository.sendOTP(email.value)
            _sendOTPResult.value = result
        }
    }

//    // Factory để khởi tạo ViewModel
//    companion object {
//        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
//            @Suppress("UNCHECKED_CAST")
//            override fun <T : ViewModel> create(modelClass: Class<T>): T {
//                return ForgotPasswordViewModel(UserRepository()) as T
//            }
//        }
//    }
}