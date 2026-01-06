package com.rapido.captainapp.presentation.status

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.rapido.captainapp.domain.model.Order
import com.rapido.captainapp.domain.model.OrderStatus
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
    viewModel: StatusViewModel,
    orderId: String?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.collectAsState()

    // Load order if orderId provided
    //    Lifecycle aware coroutine in compose
    //    Auto-cancels when Composable leaves composition
    //    When screen opens → load order, if order Id Changes reload
    LaunchedEffect(orderId) {
        orderId?.let {
            viewModel.handleIntent(StatusIntent.LoadOrder(it))
        }
    }

    // Collect side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is StatusSideEffect.ShowSuccess -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }
            is StatusSideEffect.ShowError -> {
                Toast.makeText(context, "Error: ${sideEffect.error}", Toast.LENGTH_LONG).show()
            }
            is StatusSideEffect.OrderCompleted -> {
                // Order completed animation/sound could go here
            }
            is StatusSideEffect.NavigateBackToHome -> {
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Status") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Multi-order switcher
            if (state.allActiveOrders.size > 1) {
                MultiOrderSwitcher(
                    orders = state.allActiveOrders,
                    currentOrderId = state.currentOrder?.id,
                    onOrderClick = { order ->
                        viewModel.handleIntent(StatusIntent.SwitchOrder(order.id))
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Current order details and actions
            state.currentOrder?.let { order ->
                OrderStatusCard(
                    order = order,
                    isUpdating = state.isUpdating,
                    onMarkArrived = {
                        viewModel.handleIntent(StatusIntent.MarkArrived(order.id))
                    },
                    onMarkPicked = {
                        viewModel.handleIntent(StatusIntent.MarkPicked(order.id))
                    },
                    onMarkFinished = {
                        viewModel.handleIntent(StatusIntent.MarkFinished(order.id))
                    }
                )
            } ?: run {
                // No active order
                EmptyStateView()
            }
        }
    }
}

@Composable
fun MultiOrderSwitcher(
    orders: List<Order>,
    currentOrderId: String?,
    onOrderClick: (Order) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Active Orders (${orders.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 150.dp)
            ) {
                items(orders) { order ->
                    MultiOrderItem(
                        order = order,
                        isCurrent = order.id == currentOrderId,
                        onClick = { onOrderClick(order) }
                    )
                }
            }
        }
    }
}

@Composable
fun MultiOrderItem(
    order: Order,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isCurrent) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isCurrent) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isCurrent) Color.White else MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = order.id.takeLast(3),
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.id,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = textColor
                )
                Text(
                    text = order.status.name,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.8f)
                )
            }

            if (isCurrent) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun OrderStatusCard(
    order: Order,
    isUpdating: Boolean,
    onMarkArrived: () -> Unit,
    onMarkPicked: () -> Unit,
    onMarkFinished: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Order Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.id,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${order.amount}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                item {
                    // Customer Info
                    customerInfo(order)

                    Spacer(modifier = Modifier.height(24.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(24.dp))

                    // Status Progress
                    orderStatusView(order, isUpdating, onMarkArrived, onMarkPicked, onMarkFinished)
                }
            }
        }
    }
}
@Composable
fun customerInfo(order: Order) {
            InfoRow(
                icon = Icons.Default.Person,
                label = "Customer",
                value = order.customerName
            )
            InfoRow(
                icon = Icons.Default.Place,
                label = "Pickup",
                value = order.pickupAddress
            )
            InfoRow(
                icon = Icons.Default.Home,
                label = "Delivery",
                value = order.deliveryAddress
            )
            InfoRow(
                icon = Icons.Default.LocationOn,
                label = "Distance",
                value = order.distance
            )
}

@Composable
fun orderStatusView(order: Order,
                    isUpdating: Boolean,
                    onMarkArrived: () -> Unit,
                    onMarkPicked: () -> Unit,
                    onMarkFinished: () -> Unit) {
            OrderStatusProgress(currentStatus = order.status)

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button based on current status
            StatusActionButton(
                currentStatus = order.status,
                isUpdating = isUpdating,
                onMarkArrived = onMarkArrived,
                onMarkPicked = onMarkPicked,
                onMarkFinished = onMarkFinished
            )
}
@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun OrderStatusProgress(currentStatus: OrderStatus) {
    val steps = listOf(
        OrderStatus.ASSIGNED to "Assigned",
        OrderStatus.ARRIVED to "Arrived",
        OrderStatus.PICKED_UP to "Picked Up",
        OrderStatus.DELIVERED to "Delivered"
    )

    Column {
        Text(
            text = "Order Journey",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        steps.forEachIndexed { index, (status, label) ->
            val isCompleted = currentStatus.ordinal >= status.ordinal
            val isCurrent = currentStatus == status

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step indicator
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> MaterialTheme.colorScheme.primary
                                else -> Color.LightGray
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Label
                Text(
                    text = label,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Gray
                    }
                )
            }

            // Connecting line
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .width(2.dp)
                        .height(24.dp)
                        .background(
                            if (isCompleted) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.LightGray
                            }
                        )
                )
            }
        }
    }
}

@Composable
fun StatusActionButton(
    currentStatus: OrderStatus,
    isUpdating: Boolean,
    onMarkArrived: () -> Unit,
    onMarkPicked: () -> Unit,
    onMarkFinished: () -> Unit
) {
    when (currentStatus) {
        OrderStatus.ASSIGNED -> {
            ActionButton(
                text = "Mark Arrived at Pickup",
                icon = Icons.Default.Place,
                isLoading = isUpdating,
                color = Color(0xFF3B82F6),
                onClick = onMarkArrived
            )
        }
        OrderStatus.ARRIVED -> {
            ActionButton(
                text = "Mark Order Picked",
                icon = Icons.Default.ShoppingBag,
                isLoading = isUpdating,
                color = Color(0xFFF59E0B),
                onClick = onMarkPicked
            )
        }
        OrderStatus.PICKED_UP -> {
            ActionButton(
                text = "Mark Delivered",
                icon = Icons.Default.CheckCircle,
                isLoading = isUpdating,
                color = Color(0xFF22C55E),
                onClick = onMarkFinished
            )
        }
        OrderStatus.DELIVERED -> {
            Text(
                text = "✅ Order Completed!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF22C55E),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isLoading: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White
            )
        } else {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmptyStateView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Active Orders",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Text(
                text = "Accept an order from Home tab to track it here",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}