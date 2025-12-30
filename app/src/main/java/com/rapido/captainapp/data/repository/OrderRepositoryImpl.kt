package com.rapido.captainapp.data.repository

import com.rapido.captainapp.data.local.OrderDao
import com.rapido.captainapp.data.local.toDomain
import com.rapido.captainapp.data.local.toEntity
import com.rapido.captainapp.domain.model.Order
import com.rapido.captainapp.domain.model.OrderStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class OrderRepositoryImpl(
    private val orderDao: OrderDao
) : OrderRepository {

    // For dummy pending order (simulating Firebase notifications)
    private val _pendingOrder = MutableStateFlow<Order?>(null)

    override fun getActiveOrders(): Flow<List<Order>> {
        return orderDao.getActiveOrders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getOrderById(orderId: String): Order? {
        return orderDao.getOrderById(orderId)?.toDomain()
    }

    override suspend fun acceptOrder(orderId: String): Result<Order> {
        return try {
            // Simulate API delay
            delay(500)

            val order = _pendingOrder.value?.copy(status = OrderStatus.ASSIGNED)
                ?: throw Exception("No pending order")

            // Save to local database
            orderDao.insertOrder(order.toEntity())

            // Clear pending order
            _pendingOrder.value = null

            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectOrder(orderId: String): Result<Unit> {
        return try {
            delay(300)
            _pendingOrder.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Order> {
        return try {
            delay(500)

            val orderEntity = orderDao.getOrderById(orderId)
                ?: throw Exception("Order not found")

            val updatedEntity = orderEntity.copy(status = status.name)
            orderDao.updateOrder(updatedEntity)

            // If delivered, remove from active orders after a delay
            if (status == OrderStatus.DELIVERED) {
                delay(1000)
                orderDao.deleteOrder(orderId)
            }

            Result.success(updatedEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPendingOrder(): Flow<Order?> {
        return _pendingOrder
    }

    // DUMMY: Simulate incoming order (call this to test)
    override suspend fun simulateIncomingOrder() {
        delay(3000) // Wait 3 seconds after going on duty

        val dummyOrder = createDummyOrder()
        _pendingOrder.value = dummyOrder
    }

    private fun createDummyOrder(): Order {
        val orderNumber = (1000..9999).random()
        return Order(
            id = "ORD$orderNumber",
            pickupAddress = "McDonald's, Koramangala",
            deliveryAddress = "BTM Layout, 2nd Stage",
            customerName = "Rahul Sharma",
            amount = 250.0,
            distance = "3.5 km",
            status = OrderStatus.ASSIGNED
        )
    }
}