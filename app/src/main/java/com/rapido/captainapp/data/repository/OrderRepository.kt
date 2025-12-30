package com.rapido.captainapp.domain.repository

import com.rapido.captainapp.domain.model.Order
import com.rapido.captainapp.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    // Observe all active orders
    fun getActiveOrders(): Flow<List<Order>>

    // Get single order by ID
    suspend fun getOrderById(orderId: String): Order?

    // Accept order
    suspend fun acceptOrder(orderId: String): Result<Order>

    // Reject order
    suspend fun rejectOrder(orderId: String): Result<Unit>

    // Update order status
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Order>

    // Get pending order (incoming notification)
    fun getPendingOrder(): Flow<Order?>
}