package com.rapido.captainapp.presentation.status

import com.rapido.captainapp.domain.model.Order

data class StatusState(
    val currentOrder: Order? = null,
    val allActiveOrders: List<Order> = emptyList(),
    val isUpdating: Boolean = false,
    val errorMessage: String? = null
)