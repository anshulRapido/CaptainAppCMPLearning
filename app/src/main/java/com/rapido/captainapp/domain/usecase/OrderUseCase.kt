package com.rapido.captainapp.domain.usecase

import com.rapido.captainapp.data.repository.OrderRepository
import com.rapido.captainapp.domain.model.Order
import kotlinx.coroutines.flow.Flow

class OrderUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke() {
        orderRepository.simulateIncomingOrder()
    }

    fun getActiveOrders(): Flow<List<Order>> {
        return orderRepository.getActiveOrders()
    }

    fun getPendingOrder(): Flow<Order?> {
        return orderRepository.getPendingOrder()
    }

}