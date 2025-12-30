package com.rapido.captainapp.domain.usecase

import com.rapido.captainapp.data.repository.OrderRepository

class RejectOrderUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String): Result<Unit> {
        return orderRepository.rejectOrder(orderId)
    }
}