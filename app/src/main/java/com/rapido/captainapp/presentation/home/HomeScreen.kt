package com.rapido.captainapp.presentation.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rapido.captainapp.domain.model.DutyStatus
import com.rapido.captainapp.domain.model.Order
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToStatus: (String) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.collectAsState()

    // Collect side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is HomeSideEffect.ShowToast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }
            is HomeSideEffect.PlayNotificationSound -> {
                // Sound would play here
            }
            is HomeSideEffect.NavigateToStatusTab -> {
                onNavigateToStatus(sideEffect.orderId)
            }
            is HomeSideEffect.ShowError -> {
                Toast.makeText(context, "Error: ${sideEffect.error}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hello, ${state.captainName}!")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Duty Toggle Card
                DutyToggleCard(
                    dutyStatus = state.dutyStatus,
                    isLoading = state.isLoading,
                    onToggle = {
                        viewModel.handleIntent(HomeIntent.ToggleDuty)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Active Orders Section
                ActiveOrdersSection(
                    orders = state.activeOrders,
                    onOrderClick = { order ->
                        viewModel.handleIntent(HomeIntent.NavigateToStatus(order.id))
                    }
                )
            }

            // Incoming Order Popup
            AnimatedVisibility(
                visible = state.incomingOrder != null,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                state.incomingOrder?.let { order ->
                    IncomingOrderPopup(
                        order = order,
                        isLoading = state.isLoading,
                        onAccept = {
                            viewModel.handleIntent(HomeIntent.AcceptOrder(order.id))
                        },
                        onReject = {
                            viewModel.handleIntent(HomeIntent.RejectOrder(order.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DutyToggleCard(
    dutyStatus: DutyStatus,
    isLoading: Boolean,
    onToggle: () -> Unit
) {
    val isOnDuty = dutyStatus == DutyStatus.ON_DUTY
    val backgroundColor = if (isOnDuty) Color(0xFF22C55E) else Color(0xFFEF4444)
    val statusText = if (isOnDuty) "ON DUTY" else "OFF DUTY"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = statusText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onToggle,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = backgroundColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = backgroundColor
                    )
                } else {
                    Text(
                        text = if (isOnDuty) "Go OFF Duty" else "Go ON Duty",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveOrdersSection(
    orders: List<Order>,
    onOrderClick: (Order) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Active Orders (${orders.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (orders.isEmpty()) {
                Text(
                    text = "No active orders",
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(orders) { order ->
                        OrderItem(
                            order = order,
                            onClick = { onOrderClick(order) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItem(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.id,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "₹${order.amount}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = order.pickupAddress,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = "Status: ${order.status.name}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun IncomingOrderPopup(
    order: Order,
    isLoading: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "🔔 NEW ORDER!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Order Details
                OrderDetailRow("Order ID:", order.id)
                OrderDetailRow("Customer:", order.customerName)
                OrderDetailRow("Pickup:", order.pickupAddress)
                OrderDetailRow("Delivery:", order.deliveryAddress)
                OrderDetailRow("Amount:", "₹${order.amount}")
                OrderDetailRow("Distance:", order.distance)

                Spacer(modifier = Modifier.height(20.dp))

                // Accept/Reject Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onReject,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject")
                    }

                    Button(
                        onClick = onAccept,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF22C55E)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Accept")
                        }
                    }
                }

                Text(
                    text = "⏱️ 15 seconds to respond",
                    fontSize = 12.sp,
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun OrderDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp
        )
    }
}