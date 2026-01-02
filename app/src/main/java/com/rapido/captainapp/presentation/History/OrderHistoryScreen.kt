package com.rapido.captainapp.presentation.History

import android.graphics.Color
import android.icu.lang.UCharacter
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rapido.captainapp.ui.theme.Pink80
import com.rapido.captainapp.ui.theme.Purple40
import org.orbitmvi.orbit.compose.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistory(
    viewModel: OrderHistoryViewModel) {
    val state by viewModel.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order History") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) {
    Box(modifier = Modifier.fillMaxHeight().padding(24.dp)) {
        Column (
            modifier = Modifier.padding(it)
        ) {
            Text("Wallet balance: $100",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(16.dp))
            if (state.orders.isEmpty()) {
                Text(text = "No orders found")
            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                        //                .border(width = 1.dp, color = Blue200, shape = RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = Purple40,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    items(state.orders.size) { index ->
                        OrderHistoryItem(state.orders[index])
                        if (index != state.orders.size - 1) {
                            Spacer(modifier = Modifier.height(2.dp))

                            Divider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 1.dp,
                                color = Purple40
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
     }
    }
}

@Composable
fun OrderHistoryItem(
    item: OrderUi
) {
    Column {
        Text(text = item.id)
        Text(text = item.pickupAddress)
        Text(text = item.deliveryAddress)
        Text(text = item.customerName)
    }
}
