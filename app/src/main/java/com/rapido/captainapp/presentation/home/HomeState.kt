package com.rapido.captainapp.presentation.home

import com.rapido.captainapp.domain.model.DutyStatus
import com.rapido.captainapp.domain.model.Order

data class HomeState(
    val dutyStatus: DutyStatus = DutyStatus.OFF_DUTY,
    val activeOrders: List<Order> = emptyList(),
    val incomingOrder: Order? = null,
    val isLoading: Boolean = false,
    val captainName: String = "Captain"
)