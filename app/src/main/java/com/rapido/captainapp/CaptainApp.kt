package com.rapido.captainapp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.rapido.captainapp.presentation.History.OrderHistory
import com.rapido.captainapp.presentation.History.OrderHistoryViewModel
import com.rapido.captainapp.presentation.home.HomeScreen
import com.rapido.captainapp.presentation.home.HomeViewModel
import com.rapido.captainapp.presentation.status.StatusScreen
import com.rapido.captainapp.presentation.status.StatusViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.compose.viewModel

@Composable
fun CaptainApp() {
    // ViewModels - created once at app level, shared across tabs
    val homeViewModel: HomeViewModel = koinViewModel()
    val statusViewModel: StatusViewModel = koinViewModel()
    val historyViewModel: OrderHistoryViewModel = koinViewModel()
    // Tab state
    var selectedTab by remember { mutableStateOf(0) }
    var currentOrderId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(Icons.Default.LocationOn, contentDescription = "Status")
                    },
                    label = { Text("Status") }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = "History")
                    },
                    label = { Text("History") }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> HomeScreen(
                viewModel = homeViewModel,
                onNavigateToStatus = { orderId ->
                    currentOrderId = orderId
                    selectedTab = 1 // Switch to Status tab
                }
            )

            1 -> StatusScreen(
                viewModel = statusViewModel,
                orderId = currentOrderId,
                onNavigateBack = {
                    selectedTab = 0 // Switch back to Home tab
                }
            )

            2 -> OrderHistory(
                viewModel = historyViewModel
            )
        }
    }
}


//Need overlay / center content → Box (ZStack)
//
//Need background + shape → Surface
//
//Need material card UI → Card


//Scaffold = predefined screen layout structure
//
//It gives you slots for common screen parts:
//
//top bar
//
//bottom bar
//
//floating action button
//
//content (body)