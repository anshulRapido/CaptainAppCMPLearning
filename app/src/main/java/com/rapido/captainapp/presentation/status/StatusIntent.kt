package com.rapido.captainapp.presentation.status

sealed class StatusIntent {
    data class LoadOrder(val orderId: String) : StatusIntent()
    data class MarkArrived(val orderId: String) : StatusIntent()
    data class MarkPicked(val orderId: String) : StatusIntent()
    data class MarkFinished(val orderId: String) : StatusIntent()
    data class SwitchOrder(val orderId: String) : StatusIntent()
}