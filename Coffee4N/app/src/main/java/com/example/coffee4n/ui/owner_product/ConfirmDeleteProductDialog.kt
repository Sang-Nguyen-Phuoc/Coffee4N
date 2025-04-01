package com.example.coffee4n.ui.owner_product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDeleteProductDialog(
    productName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(300.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tiêu đề dialog
                Text(
                    text = "Confirm Delete",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF313131)
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Nội dung thông báo
                Text(
                    text = "Are you sure you want to delete the product \"$productName\"?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Nút xác nhận và hủy (đồng nhất với AddEditProductDialog)
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Nút Cancel (giống nút Dismiss trong AddEditProductDialog)
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF6D6D6D) // Màu xám cho nút Cancel, giống AddEditProductDialog
                        )
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Nút Delete (giống nút Save/Update trong AddEditProductDialog)
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC67C4E) // Màu chính, giống nút Save/Update
                        ),
                        shape = RoundedCornerShape(8.dp) // Hình dạng tròn giống AddEditProductDialog
                    ) {
                        Text("Delete", color = Color.White) // Văn bản màu trắng, giống AddEditProductDialog
                    }
                }
            }
        }
    }
}