package com.rapido.captainapp.presentation.History

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rapido.captainapp.domain.model.Order
import com.rapido.captainapp.domain.usecase.OrderHistoryUseCase
import com.rapido.captainapp.domain.usecase.OrderRepository
import com.rapido.captainapp.presentation.status.StatusSideEffect
import com.rapido.captainapp.presentation.status.StatusState
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container


class OrderHistoryViewModel(
    private val useCase: OrderHistoryUseCase
): ViewModel(),
    ContainerHost<OrderHistoryState, OrderHistorySideEffect> {
    override val container = container<OrderHistoryState, OrderHistorySideEffect>(OrderHistoryState())

     init {
         loadOrders()
     }

    fun loadOrders() {
        viewModelScope.launch {
            useCase.getPastOrders().collect { orders ->
                val uiOrders = orders.map { it.toUi() }
                intent {
                    reduce {
                        state.copy(orders = uiOrders)
                    }
                    if (orders.isEmpty()) {
                        postSideEffect(
                            OrderHistorySideEffect.ShowError("No orders found")
                        )
                    }
                }
            }
        }
    }
}
data class OrderHistoryState(
    val orders: List<OrderUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class OrderUi(
    val id: String,
    val pickupAddress: String,
    val deliveryAddress: String,
    val customerName: String
)

sealed class OrderHistoryIntent {
    object refreshButtonAction: OrderHistoryIntent()
}

sealed class  OrderHistorySideEffect {
    data class ShowError(val message: String) : OrderHistorySideEffect()
}

fun  Order.toUi(): OrderUi {
    return OrderUi(
        id = id,
        pickupAddress = pickupAddress,
        deliveryAddress = deliveryAddress,
        customerName = customerName
    )
}