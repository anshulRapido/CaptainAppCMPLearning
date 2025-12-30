package com.rapido.captainapp.presentation.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rapido.captainapp.domain.model.OrderStatus
import com.rapido.captainapp.data.repository.OrderRepository
import com.rapido.captainapp.domain.usecase.UpdateOrderStatusUseCase
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class StatusViewModel(
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase,
    private val orderRepository: OrderRepository
) : ViewModel(), ContainerHost<StatusState, StatusSideEffect> {

    override val container: Container<StatusState, StatusSideEffect> = container(StatusState())

    init {
        observeActiveOrders()
    }

    fun handleIntent(intent: StatusIntent) {
        when (intent) {
            is StatusIntent.LoadOrder -> loadOrder(intent.orderId)
            is StatusIntent.MarkArrived -> markArrived(intent.orderId)
            is StatusIntent.MarkPicked -> markPicked(intent.orderId)
            is StatusIntent.MarkFinished -> markFinished(intent.orderId)
            is StatusIntent.SwitchOrder -> switchOrder(intent.orderId)
        }
    }

    private fun observeActiveOrders() = intent {
        viewModelScope.launch {
            orderRepository.getActiveOrders().collect { orders ->
                reduce {
                    state.copy(
                        allActiveOrders = orders,
                        // If current order is null and there are orders, set first as current
                        currentOrder = state.currentOrder ?: orders.firstOrNull()
                    )
                }
            }
        }
    }

    private fun loadOrder(orderId: String) = intent {
        viewModelScope.launch {
            val order = orderRepository.getOrderById(orderId)
            reduce {
                state.copy(currentOrder = order)
            }
        }
    }

    private fun markArrived(orderId: String) = intent {
        reduce {
            state.copy(isUpdating = true)
        }

        updateOrderStatusUseCase(orderId, OrderStatus.ARRIVED)
            .onSuccess { updatedOrder ->
                reduce {
                    state.copy(
                        isUpdating = false,
                        currentOrder = updatedOrder
                    )
                }
                postSideEffect(StatusSideEffect.ShowSuccess("Arrived at pickup location ✅"))
            }
            .onFailure { error ->
                reduce {
                    state.copy(isUpdating = false)
                }
                postSideEffect(StatusSideEffect.ShowError(error.message ?: "Failed to update status"))
            }
    }

    private fun markPicked(orderId: String) = intent {
        reduce {
            state.copy(isUpdating = true)
        }

        updateOrderStatusUseCase(orderId, OrderStatus.PICKED_UP)
            .onSuccess { updatedOrder ->
                reduce {
                    state.copy(
                        isUpdating = false,
                        currentOrder = updatedOrder
                    )
                }
                postSideEffect(StatusSideEffect.ShowSuccess("Order picked up! 📦 Now delivering..."))
            }
            .onFailure { error ->
                reduce {
                    state.copy(isUpdating = false)
                }
                postSideEffect(StatusSideEffect.ShowError(error.message ?: "Failed to update status"))
            }
    }

    private fun markFinished(orderId: String) = intent {
        reduce {
            state.copy(isUpdating = true)
        }

        updateOrderStatusUseCase(orderId, OrderStatus.DELIVERED)
            .onSuccess {
                reduce {
                    state.copy(
                        isUpdating = false,
                        currentOrder = null
                    )
                }
                postSideEffect(StatusSideEffect.ShowSuccess("Order delivered! 🎉"))
                postSideEffect(StatusSideEffect.OrderCompleted)

                // If no more orders, navigate back to home
                if (state.allActiveOrders.isEmpty()) {
                    postSideEffect(StatusSideEffect.NavigateBackToHome)
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(isUpdating = false)
                }
                postSideEffect(StatusSideEffect.ShowError(error.message ?: "Failed to complete order"))
            }
    }

    private fun switchOrder(orderId: String) = intent {
        viewModelScope.launch {
            val order = orderRepository.getOrderById(orderId)
            reduce {
                state.copy(currentOrder = order)
            }
        }
    }
}