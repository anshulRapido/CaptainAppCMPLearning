package com.rapido.captainapp.domain.usecase

import com.rapido.captainapp.domain.model.Order
import com.rapido.captainapp.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

class UpdateOrderStatusUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String, status: OrderStatus): Result<Order> {
        return orderRepository.updateOrderStatus(orderId, status)
    }

    suspend fun getActiveOrders(): Flow<List<Order>> {
        return orderRepository.getActiveOrders()
    }

    suspend fun getOrderById(orderId: String): Order? {
        return orderRepository.getOrderById(orderId)
    }
}