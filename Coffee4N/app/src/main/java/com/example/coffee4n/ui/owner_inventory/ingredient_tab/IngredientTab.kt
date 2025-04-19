package com.example.coffee4n.ui.owner_inventory.ingredient_tab

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffee4n.model.Ingredient
import com.example.coffee4n.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientTab(
    onIngredientSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: IngredientTabViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return IngredientTabViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val state by viewModel.state.collectAsState()

    val filteredIngredients = state.ingredients
        .filter { it.name.contains(state.searchQuery, ignoreCase = true) }
        .filter {
            when (state.selectedFilter) {
                1 -> it.quantity >= it.threshold
                2 -> it.quantity in 1 until it.threshold
                3 -> it.quantity <= 0
                else -> true
            }
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFC67C4E)
            )
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                TextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search ingredient") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)),
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.updateSearchQuery("")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Clear search",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(state.filterList) { index, title ->
                        val selected = index == state.selectedFilter
                        Button(
                            onClick = { viewModel.updateSelectedFilter(index) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) Color(0xFFC67C4E) else Color.Gray,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(title, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                    ,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filteredIngredients) { ingredient ->
                        Button(
                            onClick = {onIngredientSelected(ingredient.name)},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Unspecified
                            ),
                            elevation = null,
                            contentPadding = PaddingValues(0.dp),
                            shape = RectangleShape
                        ) {
                            IngredientCard(
                                ingredient = ingredient,
                                onShowAddTransactionDialog = { selectedIngredient, type ->
                                    viewModel.showAddTransactionDialog(selectedIngredient, type)
                                },
                                onShowEditIngredientDialog = { selectedIngredient ->
                                    viewModel.showAddEditIngredientDialog(isNew = false, ingredient = selectedIngredient)
                                },
                                onShowConfirmDeleteIngreDientDialog = { seletedIngredient ->
                                    viewModel.showConfirmDeleteIngredientDialog(ingredient)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    if (state.showAddTransactionDialog) {
        AddTransactionDialog(
            ingredient = state.selectedIngredient,
            transactionType = state.transactionType,
            onQuantityChange = {viewModel.updateQuantityInput(it)},
            onUnitPriceChange = {viewModel.updateUnitPriceInput(it)},
            onTransact = {viewModel.onTransact()},
            onDismiss = {viewModel.onDismiss()}
        )
    }

    if (state.showAddEditIngredientDialog) {
        AddEditIngredientDialog(
            isNew = state.isNew,
            name = state.nameInput,
            unit = state.unitInput,
            threshold = state.thresholdInput,
            onNameChange = { viewModel.updateNameInput(it) },
            onUnitChange = { viewModel.updateUnitInput(it) },
            onThresHoldChange = { viewModel.updateThresholdInput(it) },
            onConfirm = { viewModel.onConfirmAddEditIngredient() },
            onDismiss = { viewModel.onDismiss() }
        )
    }

    if (state.showConfirmDeleteIngredientDialog) {
        ConfirmDeleteIngredientDialog(
            ingredientName = state.selectedIngredient.name,
            onConfirm = { viewModel.onConfirmDeleteIngredient() },
            onDismiss = { viewModel.onDismiss() }
        )
    }
}

//@Preview
//@Composable
//fun PreviewCard() {
//    val ingredient = Ingredient(25, "White Chocolate Sauce", "bag", 20, 15)
//    IngredientCard(
//        ingredient,
//        { ingredient, tran -> {} },
//        { ingredient -> {} },
//        { ingredient -> {} }
//    )
//}

@Composable
fun IngredientCard(
    ingredient: Ingredient,
    onShowAddTransactionDialog: (Ingredient, TransactionType) -> Unit,
    onShowEditIngredientDialog: (Ingredient) -> Unit,
    onShowConfirmDeleteIngreDientDialog: (Ingredient) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp).shadow(4.dp, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD8E2DC))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row (
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        fontSize = 20.sp,
                        color = Color(0xFF5A9280),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Quantity: ${ingredient.quantity} (${ingredient.unit})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text(
                        text = "Status: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
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
            }
            Spacer(modifier = Modifier.width(15.dp))
            Column (
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { onShowEditIngredientDialog(ingredient) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .padding(0.dp)
                            .background(Color(0xFFECE4DB))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFFC67C4E),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Delete button
                    IconButton(
                        onClick = { onShowConfirmDeleteIngreDientDialog(ingredient) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .padding(0.dp)
                            .background(Color(0xFFFAE1DD))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFE38B73),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Button(
                    onClick = { onShowAddTransactionDialog(ingredient, TransactionType.IMPORT) },
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF145BDE)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC67C4E)),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(22.dp)
                ) {
                    Text(
                        text = "Import",
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { onShowAddTransactionDialog(ingredient, TransactionType.EXPORT) },
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC67C4E)),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(22.dp)
                ) {
                    Text(
                        text = "Export",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}