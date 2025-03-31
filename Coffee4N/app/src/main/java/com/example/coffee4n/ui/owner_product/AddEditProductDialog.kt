package com.example.coffee4n.ui.owner_product

import androidx.compose.runtime.Composable
import android.net.Uri
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.res.painterResource
import com.example.coffee4n.R
import com.example.coffee4n.utils.Cloudinary

@Preview
@Composable
fun PreviewDialog() {
    AddEditProductDialog()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
    isEdit: Boolean = false,
    name: String = "",
    description: String = "",
    price: String = "",
    isBestSeller: Boolean = false,
    categoryId: String = "",
    stockQuantity: String = "",
    costPrice: String = "",
    imageUrl: String? = "",
    onNameChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    onPriceChange: (String) -> Unit = {},
    onBestSellerChange: (Boolean) -> Unit = {},
    onCategoryChange: (String) -> Unit = {},
    onStockQuantityChange: (String) -> Unit = {},
    onCostPriceChange: (String) -> Unit = {},
    onImageUrlChange: (String?) -> Unit = {},
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
                    onImageUrlChange(secureUrl)
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
                text = if (isEdit) "Edit Product" else "Add New Product",
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
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Product Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = onPriceChange,
                    label = { Text("Price") },
                    prefix = { Text("$") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isBestSeller,
                        onCheckedChange = onBestSellerChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFC67C4E),
                            uncheckedColor = Color.Gray
                        )
                    )
                    Text("Best Seller", color = Color(0xFF313131))
                }

                OutlinedTextField(
                    value = categoryId,
                    onValueChange = onCategoryChange,
                    label = { Text("Category ID") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )

                OutlinedTextField(
                    value = stockQuantity,
                    onValueChange = onStockQuantityChange,
                    label = { Text("Stock Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )

                OutlinedTextField(
                    value = costPrice,
                    onValueChange = onCostPriceChange,
                    label = { Text("Cost Price") },
                    prefix = { Text("$") },
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
                    Text("Select Image", color = Color.White)
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
                        if (imageUri != null || !imageUrl.isNullOrEmpty()) {
                            var image = imageUri ?: imageUrl
                            Image(
                                painter = rememberAsyncImagePainter(model = image),
                                contentDescription = "Preview Image",
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_coffee),
                                contentDescription = "Default Image",
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        // Hiển thị tiến trình và lỗi
                        if (isUploading) {
                            LinearProgressIndicator(
                                progress = uploadProgress / 100f,
                                color = Color(0xFFC67C4E),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
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
                enabled = !isUploading, // Vô hiệu hóa nút khi đang upload
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC67C4E)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isEdit) "Update" else "Save", color = Color.White)
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