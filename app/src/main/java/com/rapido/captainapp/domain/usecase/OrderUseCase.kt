package com.rapido.captainapp.domain.usecase

import com.rapido.captainapp.data.repository.OrderRepository

class SimulateOrderUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke() {
        orderRepository.simulateIncomingOrder()
    }
}