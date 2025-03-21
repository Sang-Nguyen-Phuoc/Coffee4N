package com.example.coffee4n.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignupViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _signupState = MutableStateFlow<SignupState>(SignupState.Idle)
    val signupState: StateFlow<SignupState> = _signupState

    fun onUsernameChange(newUsername: String) {
        _username.value = newUsername
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
    }

    fun signup() {
        if (password.value != confirmPassword.value) {
            _signupState.value = SignupState.Error("Passwords do not match")
            return
        }
        viewModelScope.launch {
            _signupState.value = SignupState.Loading
            val token = userRepository.register(email.value, password.value, username.value)
            _signupState.value = if (token != null) {
                SignupState.Success(token)
            } else {
                SignupState.Error("Signup failed")
            }
        }
    }

    fun resetSignupState() {
        _signupState.value = SignupState.Idle
    }
}

sealed class SignupState {
    object Idle : SignupState()
    object Loading : SignupState()
    data class Success(val token: String) : SignupState()
    data class Error(val message: String) : SignupState()
}