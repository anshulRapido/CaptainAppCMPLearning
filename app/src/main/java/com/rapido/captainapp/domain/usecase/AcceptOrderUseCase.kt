package com.rapido.captainapp.domain.usecase

import com.rapido.captainapp.domain.model.Order

class AcceptOrderUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String): Result<Order> {
        return orderRepository.acceptOrder(orderId)
    }
}