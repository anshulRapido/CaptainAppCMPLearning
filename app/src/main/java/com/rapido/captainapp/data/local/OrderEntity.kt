package com.rapido.captainapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rapido.captainapp.domain.model.Order
import com.rapido.captainapp.domain.model.OrderStatus

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val pickupAddress: String,
    val deliveryAddress: String,
    val customerName: String,
    val amount: Double,
    val distance: String,
    val status: String,
    val timestamp: Long
)

// Extension functions for conversion
fun OrderEntity.toDomain(): Order {
    return Order(
        id = id,
        pickupAddress = pickupAddress,
        deliveryAddress = deliveryAddress,
        customerName = customerName,
        amount = amount,
        distance = distance,
        status = OrderStatus.valueOf(status),
        timestamp = timestamp
    )
}

fun Order.toEntity(): OrderEntity {
    return OrderEntity(
        id = id,
        pickupAddress = pickupAddress,
        deliveryAddress = deliveryAddress,
        customerName = customerName,
        amount = amount,
        distance = distance,
        status = status.name,
        timestamp = timestamp
    )
}