package com.example.coffee4n.ui.profile

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
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

    val userId = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .getInt("userId", 0)

    LaunchedEffect(userId) {
        if (userId != 0) {
            viewModel.loadUser(userId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF3E0)) // Warm background color
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Enhanced Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(239, 83, 80), // Coffee4N brand color
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PaddingValues(top = 16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Profile",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (userId != 0) {
                        IconButton(
                            onClick = { viewModel.showEditDialog() },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Main Content
            when {
                isLoading -> {
                    LoadingState()
                }
                error != null -> {
                    ErrorState(error)
                }
                else -> {
                    ProfileContent(
                        user = user,
                        userId = userId,
                        onEditClick = { viewModel.showEditDialog() },
                        onLogoutClick = {
                            viewModel.logout(context)
                            navController.navigate(Destinations.LOGIN) {
                                popUpTo(0)
                            }
                        },
                        onLoginClick = {
                            navController.navigate(Destinations.LOGIN)
                        }
                    )
                }
            }
        }

        // Edit Dialog
        if (state.showEditDialog) {
            EnhancedEditUserDialog(
                email = state.email,
                username = state.username,
                name = state.name,
                phone = state.phone,
                address = state.address,
                onUsernameChange = { viewModel.updateUsername(it) },
                onNameChange = { viewModel.updateName(it) },
                onPhoneChange = { viewModel.updatePhone(it) },
                onAddressChange = { viewModel.updateAddress(it) },
                onSave = { viewModel.saveUser() },
                onDismiss = { viewModel.hideEditDialog() }
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(239, 83, 80),
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun ErrorState(error: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(239, 83, 80)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error ?: "An error occurred",
                color = Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProfileContent(
    user: User?,
    userId: Int,
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Header with enhanced design
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(239, 83, 80),
                            Color(239, 83, 80).copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    )
                )
                .padding(vertical = 32.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image with camera icon
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.ic_profile_placeholder),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color.White, CircleShape)
                            .shadow(8.dp, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    if (userId != 0) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-8).dp, y = (-8).dp)
                                .size(32.dp)
                                .clickable { /* Handle profile image change */ },
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Change Profile Picture",
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp),
                                tint = Color(239, 83, 80)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user?.name?.takeIf { it.isNotBlank() } ?: "Guest User",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = user?.email ?: if (userId == 0) "Please sign in" else "No Email",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // User Information Cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Information Card
            if (userId != 0) {
                EnhancedInfoCard(
                    title = "Account Information",
                    items = listOf(
                        ProfileInfoItem(Icons.Default.Person, "Username", user?.username ?: "Not set"),
                        ProfileInfoItem(Icons.Default.Email, "Email", user?.email ?: "Not set"),
                        ProfileInfoItem(Icons.Default.Phone, "Phone", user?.phone ?: "Not set"),
                        ProfileInfoItem(Icons.Default.LocationOn, "Address", user?.address ?: "Not set")
                    )
                )

//                // Stats Card
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(16.dp),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color.White)
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        horizontalArrangement = Arrangement.SpaceEvenly
//                    ) {
//                        StatsItem(Icons.Default.ShoppingCart, "Orders", "12")
//                        VerticalDivider()
//                        StatsItem(Icons.Default.Favorite, "Favorites", "5")
//                        VerticalDivider()
//                        StatsItem(Icons.Default.Star, "Points", "250")
//                    }
//                }
            }

            // Action Buttons
            if (userId != 0) {
                EnhancedButton(
                    text = "Edit Profile",
                    icon = Icons.Default.Edit,
                    onClick = onEditClick,
                    backgroundColor = Color(239, 83, 80),
                    contentColor = Color.White
                )

                EnhancedButton(
                    text = "Sign Out",
                    icon = Icons.Default.Logout,
                    onClick = onLogoutClick,
                    backgroundColor = Color.Red,
                    contentColor = Color.White
                )
            } else {
                EnhancedButton(
                    text = "Sign In",
                    icon = Icons.Default.Login,
                    onClick = onLoginClick,
                    backgroundColor = Color(239, 83, 80),
                    contentColor = Color.White
                )
            }
        }
    }
}

@Composable
private fun EnhancedInfoCard(
    title: String,
    items: List<ProfileInfoItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            items.forEachIndexed { index, item ->
                EnhancedInfoRow(item.icon, item.label, item.value)
                if (index < items.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Color(239, 83, 80).copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color(239, 83, 80),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun StatsItem(icon: ImageVector, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(239, 83, 80),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .height(48.dp)
            .width(1.dp)
            .background(Color.LightGray.copy(alpha = 0.5f))
    )
}

@Composable
private fun EnhancedButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

data class ProfileInfoItem(
    val icon: ImageVector,
    val label: String,
    val value: String
)