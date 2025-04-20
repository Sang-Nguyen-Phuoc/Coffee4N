package com.example.coffee4n.ui.owner_inventory.transaction_tab

import android.app.Application
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cancel
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffee4n.model.TransactionType
import com.example.coffee4n.utils.Converters
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionTab(
    initIngredientName: String
) {
    val context = LocalContext.current
    val viewModel: TransactionTabViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TransactionTabViewModel(context.applicationContext as Application) as T
            }
        }
    )

    var searchQuery by remember { mutableStateOf(initIngredientName) }

    val state by viewModel.state.collectAsState()

    val filterTransaction = state.transactions
        .filter { it.ingredientName.contains(searchQuery, ignoreCase = true) }
        .filter {
            when(state.selectedFilter) {
                1 -> it.transaction.type == TransactionType.IMPORT
                2 -> it.transaction.type == TransactionType.EXPORT
                else -> true
            }
        }
        .filter {
            val converters = Converters()
            val transactionTime = it.transaction.timestamp
            (state.startDate == "" || transactionTime >= converters.stringToTimestamp(state.startDate)) &&
            (state.endDate == "" || transactionTime <= converters.stringToTimestamp(state.endDate))
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
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search ingredient") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
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

                Spacer(modifier = Modifier.height(5.dp))
                DateSelector(
                    label = "From",
                    date = state.startDate,
                    defaultTime = "00:00",
                    onDateSelected = { date -> viewModel.updateStartDate(date) },
                    onClear = { viewModel.updateStartDate("") }
                )

                DateSelector(
                    label = "To",
                    date = state.endDate,
                    defaultTime = "23:59",
                    onDateSelected = { date -> viewModel.updateEndDate(date) },
                    onClear = { viewModel.updateEndDate("") }
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Total Transactions:",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${filterTransaction.size}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF5A9280)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                    ,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filterTransaction) { transaction ->
                        TransactionCard(transaction)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(
    transactionDetail: TransactionDetail
) {
    val converters = Converters()
    val isImport = transactionDetail.transaction.type == TransactionType.IMPORT
    val typeText = if (isImport) "Import" else "Export"
    val typeColor = if (isImport) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card (
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD8E2DC)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = transactionDetail.ingredientName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    fontSize = 20.sp,
                    color = Color(0xFF5A9280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isImport) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = "Type icon",
                        tint = typeColor,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(text = typeText, color = typeColor, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Quantity: ${transactionDetail.transaction.quantity} (${transactionDetail.transaction.unit})")
            Spacer(modifier = Modifier.height(4.dp))
            if (isImport) {
                Text(text = "Unit price: ${converters.formatCurrency(transactionDetail.transaction.unitPrice)}")
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Total: ${converters.formatCurrency(transactionDetail.transaction.unitPrice * transactionDetail.transaction.quantity)}")
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Time: ${converters.timestampToString(transactionDetail.transaction.timestamp)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun DateSelector(
    label: String,
    date: String,
    defaultTime: String,
    onDateSelected: (String) -> Unit,
    onClear: () -> Unit
) {
    val context = LocalContext.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Button(
            modifier = Modifier.width(220.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (date.isNotEmpty()) Color(0xFFC67C4E) else Color.Gray,
                contentColor = Color.White
            ),
            onClick = {
                val cal = Calendar.getInstance()
                DatePickerDialog(context, { _, year, month, day ->
                    val selected = String.format("%04d-%02d-%02d $defaultTime", year, month + 1, day)
                    onDateSelected(selected)
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            }
        ) {
            Text(
                text = "$label: ${if (date.isNotEmpty()) date else "Select"}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }

        IconButton(
            onClick = onClear,
            modifier = Modifier.padding(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = "Clear $label Date",
                tint = Color(0xFFF44336),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
