package com.rapido.captainapp.domain.model

enum class OrderStatus {
    ASSIGNED,      // Order accepted, going to pickup
    ARRIVED,       // Arrived at restaurant
    PICKED_UP,     // Food collected, delivering
    DELIVERED      // Order completed
}