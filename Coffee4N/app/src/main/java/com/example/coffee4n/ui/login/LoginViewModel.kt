package com.example.coffee4n.ui.login

import OwnerRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.Owner
import com.example.coffee4n.repository.UserRepository
import com.example.coffee4n.session.OwnerSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Update LoginViewModel
class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val ownerRepository = OwnerRepository()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _owner = MutableStateFlow<Owner?>(null)
    val owner: StateFlow<Owner?> = _owner

    private val _passcode = MutableStateFlow<String>("")
    val passcode: StateFlow<String> = _passcode

    private val _isOwnerLogging = MutableStateFlow<Boolean>(false)
    val isOwnerLogging: StateFlow<Boolean> = _isOwnerLogging

    private val _token = MutableStateFlow<String>("")
    val token: StateFlow<String> = _token

    init {
        loadOwner()
    }

    fun loadOwner() {
        viewModelScope.launch {
            ownerRepository.getOwner(OwnerSession.ownerId).collect { ownerData ->
                _owner.value = ownerData
            }
        }
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onPasscodeChange(newPasscode: String) {
        _passcode.value = newPasscode
    }

    fun login() {
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading

                val firebaseUser = userRepository.authenticate(_email.value, _password.value)
                if (firebaseUser == null) {
                    _loginState.value = LoginState.Error("Invalid email or password.")
                    return@launch
                }

                _token.value = userRepository.getAuthToken()

                if (_email.value == _owner.value?.email) {
                    _isOwnerLogging.value = true
                    resetLoginState()
                    return@launch
                }

                val remoteUser = userRepository.fetchRemoteUser(firebaseUser.uid)
                if (remoteUser != null) {
                    _loginState.value = LoginState.Success(remoteUser.id, _token.value)
                } else {
                    _loginState.value = LoginState.Error("User not found.")
                }

//                val result = userRepository.loginAsCustomer(_email.value, _password.value)
//
//                if (result != null) {
//                    // Get the Firebase auth token
//                    val token = userRepository.getAuthToken()
//                    _loginState.value = LoginState.Success(result, token)
//                } else {
//                    _loginState.value = LoginState.Error("Invalid email or password")
//                }

            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun verifyOwner() {
        if (_passcode.value != _owner.value?.passCode) {
            _loginState.value = LoginState.Error("Invalid passcode")
        }
        else {
            _loginState.value = LoginState.Success(userId = -1, authToken = _token.value, isOwner = true)
        }
        onDissmiss()
    }

    fun onDissmiss() {
        onPasscodeChange("")
        _isOwnerLogging.value = false
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }


    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading
                val userId = userRepository.signInWithGoogle(idToken)
                if (userId != null) {
                    val token = userRepository.getAuthToken()
                    _loginState.value = LoginState.Success(userId, token)
                } else {
                    _loginState.value = LoginState.Error("Google login failed")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Google login failed")
            }
        }
    }



}