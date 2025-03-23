package com.example.coffee4n.ui.login

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Khởi tạo ViewModel với dependencies
    val userDao = AppDatabase.getDatabase(context).userDao()
    val firebaseAuth = FirebaseAuth.getInstance()
    val firebaseDatabase = FirebaseDatabase.getInstance()
    val viewModel: LoginViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(UserRepository(userDao, firebaseAuth, firebaseDatabase)) as T
            }
        }
    )

    // Lấy trạng thái từ ViewModel
    val email = viewModel.email.collectAsState()
    val password = viewModel.password.collectAsState()
    val loginState = viewModel.loginState.collectAsState()

    // Password visibility toggle
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(loginState) {
        when (loginState.value) {
            is LoginState.Success -> {
                with(prefs.edit()) {
                    putString("authToken", (loginState.value as LoginState.Success).token)
                    putBoolean("isFirstTime", false)
                    apply()
                }
                navController.navigate(Destinations.HOME) {
                    popUpTo(Destinations.WELCOME) { inclusive = true }
                }
                viewModel.resetLoginState()
            }
            is LoginState.Error -> {
                snackbarHostState.showSnackbar((loginState.value as LoginState.Error).message)
            }
            else -> {}
        }
    }
        Box(
            modifier = Modifier.fillMaxSize().background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(88.dp))
                Text(
                    "Welcome back! Glad to see you, Again!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold, // Chữ in đậm
                    color = Color.Black // Màu chữ đen
                )
                Spacer(modifier = Modifier.height(32.dp))
                TextField(
                    value = email.value,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("Enter your email", color = Color.Gray) }, // Placeholder màu xám
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)) // Bo góc tròn
                        .background(Color(0xFFF5F5F5)), // Màu nền xám nhạt
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent, // Ẩn đường viền khi focus
                        unfocusedIndicatorColor = Color.Transparent, // Ẩn đường viền khi không focus
                        focusedContainerColor = Color(0xFFF5F5F5), // Nền khi focus
                        unfocusedContainerColor = Color(0xFFF5F5F5) // Nền khi không focus
                    ),
                    textStyle = TextStyle(fontSize = 20.sp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = password.value,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text("Enter your password", color = Color.Gray) }, // Placeholder màu xám
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = Color.Gray // Màu xám cho icon mắt
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)) // Bo góc tròn
                        .background(Color(0xFFF5F5F5)), // Màu nền xám nhạt
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent, // Ẩn đường viền khi focus
                        unfocusedIndicatorColor = Color.Transparent, // Ẩn đường viền khi không focus
                        focusedContainerColor = Color(0xFFF5F5F5), // Nền khi focus
                        unfocusedContainerColor = Color(0xFFF5F5F5) // Nền khi không focus
                    ),
                    textStyle = TextStyle(fontSize = 20.sp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { /* Navigate to Forgot Password screen if implemented */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Forgot Password?", color = Color.DarkGray) // Màu xám đậm cho link
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = viewModel::login,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp)), // Bo góc tròn
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary // Nút màu đen
                    ),
                    enabled = loginState.value !is LoginState.Loading
                ) {
                    if (loginState.value is LoginState.Loading) {
                        CircularProgressIndicator(color = Color.White) // Vòng tròn loading màu trắng
                    } else {
                        Text("Login", color = Color.White) // Chữ trắng trên nút
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Or Login with",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Color.Gray // Màu xám cho văn bản
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
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { navController.navigate(Destinations.SIGNUP) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "Don't have an account? ",
                        color = MaterialTheme.colorScheme.primary // Màu chính (teal) cho link
                    )
                    Text("Register Now",
                        color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
}