package com.rapido.captainapp.presentation.home

sealed class HomeSideEffect {
    data class ShowToast(val message: String) : HomeSideEffect()
    object PlayNotificationSound : HomeSideEffect()
    data class NavigateToStatusTab(val orderId: String) : HomeSideEffect()
    data class ShowError(val error: String) : HomeSideEffect()

    object StartOrderListenerService : HomeSideEffect()
    object StopOrderListenerService : HomeSideEffect()
}