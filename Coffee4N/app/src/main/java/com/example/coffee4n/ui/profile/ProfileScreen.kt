package com.example.coffee4n.ui.profile

import android.app.Application
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.coffee4n.R
import com.example.coffee4n.model.User
import com.example.coffee4n.navigation.Destinations
import com.example.coffee4n.ui.theme.Coffee4NTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val firebaseAuth = FirebaseAuth.getInstance()
    val firebaseDatabase = FirebaseDatabase.getInstance()

    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(
            application = application,
            firebaseAuth = firebaseAuth,
            firebaseDatabase = firebaseDatabase
        )
    )

    val state by viewModel.state.collectAsState()
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Giả sử lấy userId từ SharedPreferences
    val userId = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .getInt("userId", 0)

    LaunchedEffect(userId) {
        if (userId != 0) {
            viewModel.loadUser(userId)
        }
    }

    Coffee4NTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    ProfileHeader(user)
                    Spacer(modifier = Modifier.height(24.dp))
                    UserInfoSection(user)
                    if (userId != 0 ) {
                        EditButton { viewModel.showEditDialog() }
                        Spacer(Modifier.weight(1f))
                        LogoutButton {
                            viewModel.logout(context)
                            navController.navigate(Destinations.LOGIN) {
                                popUpTo(0)
                            }
                        }
                    }
                    else {
                        Spacer(Modifier.weight(1f))
                        LoginButton {
                            navController.navigate(Destinations.LOGIN)
                        }
                    }
                }
                if (state.showEditDialog) {
                    EditUserDialog(
                        email = state.email,
                        username = state.username,
                        name = state.name,
                        phone = state.phone,
                        address = state.address,
                        onUsernameChange = { viewModel.updateUsername(it) },
                        onNameChange = { viewModel.updateName(it) },
                        onPhoneChange = { viewModel.updatePhone(it) },
                        onAddressChange = { viewModel.updateAddress(it) },
                        onSave = { viewModel.saveUser()},
                        onDismiss = { viewModel.hideEditDialog() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: User?) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_profile_placeholder),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = user?.name?.takeIf { it.isNotBlank() } ?: "No Name",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = user?.email ?: "No Email",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UserInfoSection(user: User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoItem(Icons.Default.Person, "Username", user?.username ?: "Not set")
            InfoItem(Icons.Default.Phone, "Phone", user?.phone ?: "Not set")
            InfoItem(Icons.Default.LocationOn, "Address", user?.address ?: "Not set")
        }
    }
}

@Composable
private fun InfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EditButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Black
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 0.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit account",
            modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Edit account", fontSize = 16.sp)
    }
}

@Composable
private fun LoginButton(onLogin: () -> Unit) {
    Button(
        onClick = onLogin,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Login,
            contentDescription = "Login",
            modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Log In", fontSize = 16.sp)
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Logout,
            contentDescription = "Logout",
            modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Log Out", fontSize = 16.sp)
    }
}


