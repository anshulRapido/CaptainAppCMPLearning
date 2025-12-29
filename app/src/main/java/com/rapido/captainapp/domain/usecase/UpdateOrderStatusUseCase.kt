package com.rapido.captainapp.domain.usecase

import com.rapido.captainapp.domain.model.Order
import com.rapido.captainapp.domain.model.OrderStatus
import com.rapido.captainapp.domain.repository.OrderRepository

class UpdateOrderStatusUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String, status: OrderStatus): Result<Order> {
        return orderRepository.updateOrderStatus(orderId, status)
    }
}