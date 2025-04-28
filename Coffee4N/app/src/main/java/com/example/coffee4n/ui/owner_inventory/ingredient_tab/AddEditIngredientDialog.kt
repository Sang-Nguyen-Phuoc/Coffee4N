package com.example.coffee4n.ui.owner_inventory.ingredient_tab

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AddEditIngredientDialog(
    isNew: Boolean = true,
    name: String = "",
    unit: String = "",
    threshold: Int = 0,
    onNameChange: (String) -> Unit = {},
    onUnitChange: (String) -> Unit = {},
    onThresHoldChange: (Int) -> Unit = {},
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val isValid = name.isNotBlank() && unit.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "Add New Ingredient" else "Edit Ingredient",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF313131)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { onNameChange(it) },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { onUnitChange(it) },
                    label = { Text("Unit (e.g. bag, can, pack)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )
                OutlinedTextField(
                    value = threshold.toString(),
                    onValueChange = {
                        val input = it.filter { char -> char.isDigit() }
                        val entered = input.toIntOrNull()
                        onThresHoldChange(entered ?: 0)
                    },
                    label = { Text("Threshold") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC67C4E)),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = if (isNew) "Add" else "Update",
                    color = Color.White
                )
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

@Composable
fun ConfirmDeleteIngredientDialog(
    ingredientName: String = "",
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {}
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
                Text(
                    text = "Confirm Delete",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF313131)
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Are you sure you want to delete the ingredient \"$ingredientName\"?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF6D6D6D)
                        )
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC67C4E)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                }
            }
        }
    }
}
