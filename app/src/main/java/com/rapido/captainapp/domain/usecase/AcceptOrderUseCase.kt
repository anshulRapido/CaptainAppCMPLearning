package com.rapido.captainapp.domain.usecase

import com.rapido.captainapp.domain.model.Order
import com.rapido.captainapp.domain.repository.OrderRepository

class AcceptOrderUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String): Result<Order> {
        return orderRepository.acceptOrder(orderId)
    }
}