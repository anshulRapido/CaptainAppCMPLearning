package com.rapido.captainapp.domain.usecase

class RejectOrderUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String): Result<Unit> {
        return orderRepository.rejectOrder(orderId)
    }
}