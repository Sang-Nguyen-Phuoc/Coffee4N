package com.example.coffee4n.ui.signup

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.coffee4n.R
import com.example.coffee4n.model.database.AppDatabase
import com.example.coffee4n.navigation.Destinations
import com.example.coffee4n.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Khởi tạo ViewModel với dependencies
    val userDao = AppDatabase.getDatabase(context).userDao()
    val firebaseAuth = FirebaseAuth.getInstance()
    val firebaseDatabase = FirebaseDatabase.getInstance()
    val viewModel: SignupViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return SignupViewModel(UserRepository(userDao, firebaseAuth, firebaseDatabase)) as T
            }
        }
    )

    // Lấy trạng thái từ ViewModel
    val username = viewModel.username.collectAsState()
    val email = viewModel.email.collectAsState()
    val password = viewModel.password.collectAsState()
    val confirmPassword = viewModel.confirmPassword.collectAsState()
    val signupState = viewModel.signupState.collectAsState()

    // Xử lý side effects
    LaunchedEffect(signupState.value) {
        when (val state = signupState.value) {
            is SignupState.Success -> {
                with(prefs.edit()) {
                    putString("authToken", state.token)
                    putBoolean("isFirstTime", false)
                    apply()
                }
                navController.navigate(Destinations.HOME) {
                    popUpTo(Destinations.WELCOME) { inclusive = true }
                }
                viewModel.resetSignupState()
            }
            is SignupState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Welcome back! Glad to see you, Again!", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(32.dp))
            TextField(
                value = username.value,
                onValueChange = { viewModel.onUsernameChange(it) },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = email.value,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = password.value,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = confirmPassword.value,
                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                label = { Text("Confirm password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.signup() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = signupState.value !is SignupState.Loading
            ) {
                if (signupState.value is SignupState.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Agree and Register")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Or Login with",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { /* Handle Facebook login */ }) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_facebook),
                        contentDescription = "Facebook Login",
                        modifier = Modifier.size(40.dp)
                    )
                }
                IconButton(onClick = { /* Handle Google login */ }) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Login",
                        modifier = Modifier.size(40.dp)
                    )
                }
                IconButton(onClick = { /* Handle Apple login */ }) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_apple),
                        contentDescription = "Apple Login",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}