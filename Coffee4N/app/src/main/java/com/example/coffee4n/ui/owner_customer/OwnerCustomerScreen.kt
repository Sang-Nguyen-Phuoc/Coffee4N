package com.example.coffee4n.ui.owner_customer

import android.app.Application
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.*
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
import com.example.coffee4n.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerCustomerScreen() {
    val context = LocalContext.current

    val viewModel: OwnerCustomerViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OwnerCustomerViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val state by viewModel.state.collectAsState()

    var itemsToShow by remember { mutableStateOf(10) }

    val searchCustomers = state.customers.filter {
           it.name.contains(state.searchQuery, ignoreCase = true)
        || it.email.contains(state.searchQuery, ignoreCase = true)
        || it.username.contains(state.searchQuery, ignoreCase = true)
        || it.address.contains(state.searchQuery, ignoreCase = true)
        || it.phone.contains(state.searchQuery, ignoreCase = true)
    }

    val displayCustomer = searchCustomers.take(itemsToShow)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Customer Management",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF313131)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF9F2ED)
                )
            )
        },
        containerColor = Color(0xFFF9F2ED)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        placeholder = { Text("Search customer...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                            .clip(RoundedCornerShape(8.dp)),
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

                    // Customer count
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Total Customers:",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${searchCustomers.size}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF5A9280)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(displayCustomer) { customer ->
                            Button(
                                onClick = { viewModel.onShowCustomerDetail(customer)},
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color.Unspecified
                                ),
                                elevation = null,
                                contentPadding = PaddingValues(0.dp),
                                shape = RectangleShape
                            ) {
                                CustomerCard(customer)
                            }
                        }
                        if (displayCustomer.size < searchCustomers.size) {
                            item {
                                Button(
                                    onClick = {
                                        itemsToShow += 10
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFC67C4E),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(text = "Load More")
                                }
                            }
                        }
                    }
                }
            }
        }
        if (state.showCustomerDetailDialog) {
            CustomerDetail(
                name = state.selectedCustomer.name,
                email = state.selectedCustomer.email,
                username = state.selectedCustomer.username,
                phone = state.selectedCustomer.phone,
                address = state.selectedCustomer.address,
                onDismissRequest = { viewModel.onDismiss() }
            )
        }
    }
}

@Composable
fun CustomerCard(
    customer: User
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD8E2DC))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (customer.name.isNotBlank()) customer.name else "<No Name>",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                fontSize = 20.sp,
                color = Color(0xFF5A9280),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))
            RowInfor("Email", customer.email)

            Spacer(modifier = Modifier.height(2.dp))
            RowInfor("Username", customer.username)

            Spacer(modifier = Modifier.height(2.dp))
            RowInfor("Phone", customer.phone)

            Spacer(modifier = Modifier.height(2.dp))
            RowInfor("Address", customer.address)
        }
    }
}

@Composable
fun RowInfor(title: String, value: String){
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "${title}: ",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
        Text(
            text = value.ifBlank { "<not set>" },
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFFC67C4E)),
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
