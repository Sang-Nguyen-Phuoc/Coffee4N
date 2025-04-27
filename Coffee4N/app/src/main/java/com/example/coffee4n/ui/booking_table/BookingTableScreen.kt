package com.example.coffee4n.ui.booking_table

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.coffee4n.model.Table
import com.example.coffee4n.model.BookingTable
import com.example.coffee4n.repository.TableRepository
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingTableScreen(navController: NavController? = null) {
    val firebaseDatabase = remember { Firebase.database }
    val tableRepository = remember { TableRepository(firebaseDatabase) }
    val viewModel = remember { BookingTableViewModel(tableRepository) }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val primaryColor = Color(239, 83, 80) // Coffee4N brand color
    val backgroundColor = Color(0xFFFAF3E0)
    val availableColor = Color(0xFF4CAF50)
    val bookedColor = Color(0xFFE57373)

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearErrorMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Enhanced Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController?.popBackStack() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        "Book a Table",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Quick info button
                    IconButton(
                        onClick = { /* Show info dialog */ },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = "Info",
                            tint = primaryColor
                        )
                    }
                }
            }

            // Main Content
            when {
                state.isLoading -> {
                    LoadingState(primaryColor)
                }
                state.tables.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    TablesList(
                        tables = state.tables,
                        bookingTables = state.bookingTables,
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        availableColor = availableColor,
                        bookedColor = bookedColor
                    )
                }
            }
        }

        // Floating Summary Card
        if (state.tables.isNotEmpty()) {
            EnhancedBookingSummaryCard(
                tables = state.tables,
                availableColor = availableColor,
                bookedColor = bookedColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            )
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
        )
    }
}

@Composable
private fun LoadingState(primaryColor: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = primaryColor,
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Loading tables...",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.TableBar,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tables available",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Please check back later",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun TablesList(
    tables: List<Table>,
    bookingTables: List<BookingTable>,
    viewModel: BookingTableViewModel,
    primaryColor: Color,
    availableColor: Color,
    bookedColor: Color
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(tables) { index, table ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(durationMillis = 350, delayMillis = index * 50)) +
                        slideInVertically(animationSpec = tween(durationMillis = 350, delayMillis = index * 50), initialOffsetY = { it / 2 })
            ) {
                EnhancedBookingTableCard(
                    table = table,
                    bookingTables = bookingTables,
                    onBook = { customerName, phoneNumber, numberOfPeople, bookingTime, notes ->
                        viewModel.bookTable(table.id, customerName, phoneNumber, numberOfPeople, bookingTime, notes)
                    },
                    onCancel = { bookingId -> viewModel.cancelBooking(bookingId) },
                    onEdit = { booking ->
                        viewModel.editBooking(booking.id!!, booking.customerName, booking.phoneNumber, booking.numberOfPeople, booking.bookingTime, booking.notes)
                    },
                    primaryColor = primaryColor,
                    availableColor = availableColor,
                    bookedColor = bookedColor
                )
            }
        }

        // Bottom spacing for summary card
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun EnhancedBookingTableCard(
    table: Table,
    bookingTables: List<BookingTable>,
    onBook: (String, String, Int, String, String?) -> Unit,
    onCancel: (Int) -> Unit,
    onEdit: (BookingTable) -> Unit,
    primaryColor: Color,
    availableColor: Color,
    bookedColor: Color
) {
    var showDialog by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                isPressed = false
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Table Image
            AsyncImage(
                model = table.imageUrl.ifEmpty { "https://picsum.photos/400/200" },
                contentDescription = "Table ${table.tableNumber} Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 100f
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (table.status == "AVAILABLE") availableColor else bookedColor,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = table.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Table Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Table ${table.tableNumber}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Chair,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "${table.capacity} seats",
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }

                    Button(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            Icons.Rounded.EventSeat,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Book Now",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Booking Dialog
    if (showDialog) {
        EnhancedBookingDialog(
            table = table,
            onDismiss = { showDialog = false },
            onConfirm = { customerName, phoneNumber, numberOfPeople, bookingTime, notes ->
                onBook(customerName, phoneNumber, numberOfPeople, bookingTime, notes)
                showDialog = false
            },
            primaryColor = primaryColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedBookingDialog(
    table: Table,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String, String?) -> Unit,
    primaryColor: Color
) {
    var customerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var numberOfPeople by remember { mutableStateOf("") }
    var bookingTime by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    val formatter = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
                    bookingTime = formatter.format(calendar.time)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
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
                    Column {
                        Text(
                            text = "Book Table ${table.tableNumber}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Capacity: ${table.capacity} people",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    EnhancedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = "Customer Name",
                        icon = Icons.Default.Person,
                        placeholder = "Enter your name"
                    )

                    EnhancedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = "Phone Number",
                        icon = Icons.Default.Phone,
                        placeholder = "Enter your phone number",
                        keyboardType = KeyboardType.Phone
                    )

                    EnhancedTextField(
                        value = numberOfPeople,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) {
                                numberOfPeople = it
                            }
                        },
                        label = "Number of People",
                        icon = Icons.Default.Group,
                        placeholder = "Enter number of people",
                        keyboardType = KeyboardType.Number
                    )

                    EnhancedTextField(
                        value = bookingTime,
                        onValueChange = { },
                        label = "Booking Time",
                        icon = Icons.Default.Schedule,
                        placeholder = "Select date and time",
                        readOnly = true,
                        modifier = Modifier.clickable { datePickerDialog.show() }
                    )

                    EnhancedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Notes",
                        icon = Icons.Default.Notes,
                        placeholder = "Any special requests? (optional)",
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
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (customerName.isNotBlank() && phoneNumber.isNotBlank() &&
                                numberOfPeople.isNotBlank() && bookingTime.isNotBlank()) {
                                onConfirm(
                                    customerName,
                                    phoneNumber,
                                    numberOfPeople.toIntOrNull() ?: 1,
                                    bookingTime,
                                    notes.takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = customerName.isNotBlank() && phoneNumber.isNotBlank() &&
                                numberOfPeople.isNotBlank() && bookingTime.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
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
                            Text("Confirm Booking")
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
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
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
                tint = Color(239, 83, 80)
            )
        },
        modifier = modifier.fillMaxWidth(),
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(239, 83, 80),
            focusedLabelColor = Color(239, 83, 80),
            cursorColor = Color(239, 83, 80)
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun EnhancedBookingSummaryCard(
    tables: List<Table>,
    availableColor: Color,
    bookedColor: Color,
    modifier: Modifier = Modifier
) {
    val availableTables = tables.count { it.status == "AVAILABLE" }
    val bookedTables = tables.count { it.status == "BOOKED" }
    val totalCapacity = tables.sumOf { it.capacity }
    val availableCapacity = tables.filter { it.status == "AVAILABLE" }.sumOf { it.capacity }

    Card(
        modifier = modifier
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Availability Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    value = availableTables.toString(),
                    label = "Available",
                    color = availableColor,
                    icon = Icons.Rounded.CheckCircle
                )

                SummaryItem(
                    value = bookedTables.toString(),
                    label = "Booked",
                    color = bookedColor,
                    icon = Icons.Rounded.Cancel
                )

                SummaryItem(
                    value = availableCapacity.toString(),
                    label = "Seats Available",
                    color = Color(239, 83, 80),
                    icon = Icons.Rounded.Chair
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    value: String,
    label: String,
    color: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}