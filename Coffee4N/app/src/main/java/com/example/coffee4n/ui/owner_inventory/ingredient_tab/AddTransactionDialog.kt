package com.example.coffee4n.ui.owner_inventory.ingredient_tab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.coffee4n.model.Ingredient
import com.example.coffee4n.model.TransactionType

@Preview
@Composable
fun Preview() {
    AddTransactionDialog(
        Ingredient(0, "Milk", "bag", 20, 15),
        TransactionType.IMPORT,
    )
}

@Composable
fun AddTransactionDialog(
    ingredient: Ingredient,
    transactionType: TransactionType,
    onQuantityChange: (Int) -> Unit = {},
    onUnitPriceChange: (Double) -> Unit = {},
    onTransact: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val isExport = transactionType == TransactionType.EXPORT
    val title = if (isExport) "Export ingredient" else "Import ingredient"
    val actionLabel = if (isExport) "Export" else "Import"

    val quantity = remember { mutableStateOf("") }
    val unitPrice = remember { mutableStateOf(if (isExport) "1" else "") }
    val isValidQuantity = quantity.value.toIntOrNull()?.let { it > 0 } ?: false
    val isValidPrice = unitPrice.value.toDoubleOrNull()?.let { it > 0.0 } ?: false

    val currentQuantity = ingredient.quantity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF313131))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row (
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Ingredient: ")
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF5A9280)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text("Quantity (${ingredient.unit}): ")
                    Text(
                        text = "${ingredient.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "Status: ",
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    val stockColor = when {
                        ingredient.quantity <= 0 -> Color.Red
                        ingredient.quantity < ingredient.threshold -> Color(0xFFFFA500) // Orange
                        else -> Color(0xFF4CAF50) // Green
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(stockColor, shape = RoundedCornerShape(4.dp))
                            .padding(vertical = 2.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = when {
                                ingredient.quantity <= 0 -> "Out"
                                ingredient.quantity < ingredient.threshold -> "Low"
                                else -> "Enough"
                            },
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall

                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = quantity.value,
                    onValueChange = {
                        // Chỉ cho phép số nguyên dương
                        val input = it.filter { char -> char.isDigit() }
                        val entered = input.toIntOrNull()

                        if (entered != null) {
                            if (isExport && entered > currentQuantity) {
                                quantity.value = currentQuantity.toString()
                            } else {
                                quantity.value = input
                            }
                        } else {
                            quantity.value = ""
                        }

                        onQuantityChange(quantity.value.toIntOrNull() ?: 0)
                    },
                    label = { Text("Quantity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )

                if (!isExport) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = unitPrice.value,
                        onValueChange = {
                            // Chỉ cho phép số thực hợp lệ (dấu chấm làm phân cách thập phân)
                            val input = it.replace(',', '.').filter { char -> char.isDigit() || char == '.' }
                            val entered = input.toDoubleOrNull()

                            if (entered != null) {
                                unitPrice.value = input
                            } else {
                                unitPrice.value = ""
                            }

                            onUnitPriceChange(unitPrice.value.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("Price/${ingredient.unit}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC67C4E),
                            focusedLabelColor = Color(0xFFC67C4E),
                            cursorColor = Color(0xFFC67C4E)
                        )
                    )
                }

            }
        },
        confirmButton = {
            Button(
                onClick = onTransact,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC67C4E)),
                shape = RoundedCornerShape(8.dp),
                enabled = isValidQuantity && isValidPrice,
            ) {
                Text(actionLabel, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6D6D6D))
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color.White
    )
}