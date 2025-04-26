package com.example.coffee4n.ui.login

import android.app.Activity
import android.content.Context
import android.widget.Space
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.coffee4n.R
import com.example.coffee4n.model.Owner
import com.example.coffee4n.navigation.Destinations
import com.example.coffee4n.repository.UserRepository
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Khởi tạo ViewModel với dependencies
    val firebaseAuth = FirebaseAuth.getInstance()
    val firebaseDatabase = FirebaseDatabase.getInstance()
    val viewModel: LoginViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(UserRepository(firebaseAuth, firebaseDatabase)) as T
            }
        }
    )

    // Lấy trạng thái từ ViewModel
    val email = viewModel.email.collectAsState()
    val password = viewModel.password.collectAsState()
    val loginState = viewModel.loginState.collectAsState()
    val owner by viewModel.owner.collectAsState()
    val isOwnerLogging by viewModel.isOwnerLogging.collectAsState()
    val passcode by viewModel.passcode.collectAsState()

    // Password visibility toggle
    var passwordVisible by remember { mutableStateOf(false) }

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { idToken ->
                viewModel.signInWithGoogle(idToken)
            }
        } catch (e: ApiException) {
            scope.launch {
                snackbarHostState.showSnackbar("Google sign-in failed: ${e.message}")
            }
        }
    }


    // Xử lý hiệu ứng phụ từ loginState
    LaunchedEffect(loginState.value) {
        when (val state = loginState.value) {
            is LoginState.Success -> {
                if (state.isOwner) {
                    with(prefs.edit()) {
                        putBoolean("isOwner", true)
                        putString("authToken", state.authToken)
                        apply()
                    }
                    navController.navigate(Destinations.OWNER_DASHBOARD) {
                        popUpTo(Destinations.WELCOME) { inclusive = true }
                    }
                }
                else {
                    with(prefs.edit()) {
                        putInt("userId", state.userId)
                        putString("authToken", state.authToken) // Save the auth token
                        apply()
                    }
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.WELCOME) { inclusive = true }
                    }
                }
                viewModel.resetLoginState()
            }
            is LoginState.Error -> {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Short
                    )
                }
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
                .padding(24.dp, 70.dp, 24.dp, 24.dp)
        ) {
            OwnerCard(
                owner = owner ?: Owner(),
                onChangeStore = {
                    prefs.edit().remove("ownerId").apply()
                    navController.navigate(Destinations.WELCOME)
                }
            )

            Column (
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    "Welcome! Glad to see you!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold, // Chữ in đậm
                    color = Color.Black // Màu chữ đen
                )
                Spacer(modifier = Modifier.height(22.dp))
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
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { /* Navigate to Forgot Password screen if implemented */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Forgot Password?", color = Color.DarkGray) // Màu xám đậm cho link
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (email.value.isBlank() || password.value.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please enter email and password")
                            }
                        } else {
                            viewModel.login()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = loginState.value !is LoginState.Loading
                ) {
                    if (loginState.value is LoginState.Loading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text("Login", color = Color.White)
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
                    IconButton(onClick = { }) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_facebook),
                            contentDescription = "Facebook Login",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    IconButton(onClick = {
                        val signInIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    }) {
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
                    onClick = { navController.navigate(Destinations.HOME) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "Continue as a guest",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

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
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
        if (isOwnerLogging) {
            AlertDialog(
                onDismissRequest = { viewModel.onDissmiss() },
                title = {
                    Text(
                        text = "Verify Owner",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF313131)
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Please enter the provided passcode to verify that you are the owner."
                        )
                        OutlinedTextField(
                            value = passcode,
                            onValueChange = { viewModel.onPasscodeChange(it) },
                            label = { Text("Passcode") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF5A9280),
                                focusedLabelColor = Color(0xFF5A9280),
                                cursorColor = Color(0xFF5A9280)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.verifyOwner()
                        },
                        enabled = passcode.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A9280)),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Verify", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.onDissmiss() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6D6D6D))
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun OwnerCard(
    owner: Owner,
    onChangeStore: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD8E2DC)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 16.dp, 16.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = owner.avatarUrl,
                contentDescription = "${owner.shopName} avatar",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = owner.shopName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    ),
                    color = Color(0xFF5A9280)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = owner.shopAddress,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = onChangeStore
            ) {
                Row {
                    Text(
                        "Visit another store",
                        color = Color.Black
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = "Change store",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}