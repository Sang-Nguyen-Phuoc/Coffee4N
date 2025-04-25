package com.example.coffee4n.ui.owner_profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.coffee4n.utils.Cloudinary

@Preview
@Composable
fun EditProfileDialog(
    email: String = "",
    shopName: String = "",
    shopPhone: String = "",
    shopAddress: String = "",
    avatarUrl: String = "",
    updateShopName: (String) -> Unit = {},
    updateShopPhone: (String) -> Unit = {},
    updateShopAddress: (String) -> Unit = {},
    updateAvatarUrl: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadProgress by remember { mutableStateOf(0) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    // Launcher để chọn ảnh
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            isUploading = true
            uploadError = null

            Cloudinary.uploadImageToCloudinary(
                context = context,
                imageUri = it,
                uploadPreset = "coffee_upload",
                onProgress = { progress ->
                    uploadProgress = progress
                },
                onSuccess = { _, secureUrl ->
                    updateAvatarUrl(secureUrl)
                    isUploading = false
                    uploadError = null
                },
                onError = { error ->
                    isUploading = false
                    uploadError = error
                }
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "Edit Shop Information",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF313131),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )

                OutlinedTextField(
                    value = shopName,
                    onValueChange = { updateShopName(it) },
                    label = { Text("Shop Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )

                OutlinedTextField(
                    value = shopPhone,
                    onValueChange = {
                        val input = it.filter { char -> char.isDigit() }
                        updateShopPhone(input)
                    },
                    label = { Text("Shop Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )

                OutlinedTextField(
                    value = shopAddress,
                    onValueChange = { updateShopAddress(it) },
                    label = { Text("Shop Address") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )

                Button(
                    onClick = { launcher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC67C4E)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Select Avatar", color = Color.White)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(vertical = 15.dp)
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        if (avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Profile Avatar",
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))

                        // Hiển thị tiến trình và lỗi
                        if (isUploading) {
                            LinearProgressIndicator(
                                progress = uploadProgress / 100f,
                                color = Color(0xFFC67C4E),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                            uploadError?.let {
                                Text(
                                    text = "Upload failed: $it",
                                    color = Color.Red,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                // Vô hiệu hóa nút khi đang upload ảnh hoặc thiếu dữ liệu
                enabled = !isUploading
                        && shopPhone.isNotBlank()
                        && shopName.isNotBlank()
                        && shopAddress.isNotBlank()
                ,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC67C4E)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6D6D6D))
            ) {
                Text("Cancel")
            }
        }
    )
}