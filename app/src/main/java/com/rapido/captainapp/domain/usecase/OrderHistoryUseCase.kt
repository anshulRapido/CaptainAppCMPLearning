package com.rapido.captainapp.domain.usecase

import com.rapido.captainapp.domain.model.Order
import kotlinx.coroutines.flow.Flow

class OrderHistoryUseCase(
    val orderRepository: OrderRepository
) {

    suspend fun getPastOrders(): Flow<List<Order>> {
        return orderRepository.getPastOrders()
    }
}