package com.rapido.captainapp.presentation.home

sealed class HomeIntent {
    object ToggleDuty : HomeIntent()
    data class AcceptOrder(val orderId: String) : HomeIntent()
    data class RejectOrder(val orderId: String) : HomeIntent()
    data class NavigateToStatus(val orderId: String) : HomeIntent()
}