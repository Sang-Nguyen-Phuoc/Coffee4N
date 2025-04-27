package com.example.coffee4n.ui.profile

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedEditUserDialog(
    email: String,
    username: String,
    name: String,
    phone: String,
    address: String,
    onUsernameChange: (String) -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onPhoneChange: (String) -> Unit = {},
    onAddressChange: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val isFormValid by remember {
        derivedStateOf {
            name.isNotBlank() && username.isNotBlank() && phone.isNotBlank() && address.isNotBlank()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Profile",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Form Fields
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Email Field (Read-only)
                    EnhancedTextField(
                        value = email,
                        onValueChange = {},
                        label = "Email",
                        icon = Icons.Default.Email,
                        readOnly = true,
                        enabled = false
                    )

                    // Username Field
                    EnhancedTextField(
                        value = username,
                        onValueChange = onUsernameChange,
                        label = "Username",
                        icon = Icons.Default.Person,
                        placeholder = "Enter your username"
                    )

                    // Full Name Field
                    EnhancedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        label = "Full Name",
                        icon = Icons.Default.Badge,
                        placeholder = "Enter your full name"
                    )

                    // Phone Field
                    EnhancedTextField(
                        value = phone,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                onPhoneChange(input)
                            }
                        },
                        label = "Phone Number",
                        icon = Icons.Default.Phone,
                        placeholder = "Enter your phone number",
                        keyboardType = KeyboardType.Number
                    )

                    // Address Field
                    EnhancedTextField(
                        value = address,
                        onValueChange = onAddressChange,
                        label = "Address",
                        icon = Icons.Default.LocationOn,
                        placeholder = "Enter your address",
                        singleLine = false,
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.LightGray, Color.LightGray)
                            )
                        )
                    ) {
                        Text("Cancel")
                    }

                    // Save Button
                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = isFormValid,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(239, 83, 80),
                            disabledContainerColor = Color.LightGray
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String = "",
    readOnly: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) Color(239, 83, 80) else Color.Gray
            )
        },
        modifier = Modifier.fillMaxWidth(),
        readOnly = readOnly,
        enabled = enabled,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(239, 83, 80),
            focusedLabelColor = Color(239, 83, 80),
            cursorColor = Color(239, 83, 80),
            disabledBorderColor = Color.LightGray,
            disabledLabelColor = Color.Gray,
            disabledTextColor = Color.DarkGray,
            disabledLeadingIconColor = Color.Gray
        ),
        shape = RoundedCornerShape(12.dp)
    )
}