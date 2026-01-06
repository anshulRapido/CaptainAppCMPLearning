package com.rapido.captainapp.presentation.home

sealed class HomeIntent {
    object ToggleDuty : HomeIntent()
    data class AcceptOrder(val orderId: String) : HomeIntent()
    data class RejectOrder(val orderId: String) : HomeIntent()
    data class NavigateToStatus(val orderId: String) : HomeIntent()
}

//all subclasses are known at compile time and must be declared in the same file.

//Rule of thumb
//
//No data → object
//
//Has data → data class


//object in Kotlin = a single instance (singleton).

//Use companion object when behavior conceptually belongs to the class.