package com.rapido.captainapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rapido.captainapp.domain.model.DutyStatus
import com.rapido.captainapp.domain.usecase.AcceptOrderUseCase
import com.rapido.captainapp.domain.usecase.RejectOrderUseCase
import com.rapido.captainapp.domain.usecase.OrderUseCase
import com.rapido.captainapp.domain.usecase.UpdateDutyStatusUseCase
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class HomeViewModel(
    private val updateDutyStatusUseCase: UpdateDutyStatusUseCase,
    private val acceptOrderUseCase: AcceptOrderUseCase,
    private val rejectOrderUseCase: RejectOrderUseCase,
    private val orderUseCase: OrderUseCase
) : ViewModel(), ContainerHost<HomeState, HomeSideEffect> {

    override val container: Container<HomeState, HomeSideEffect> = container(HomeState())

    init {
        observeDutyStatus()
        observeActiveOrders()
        observePendingOrders()
        loadCaptainInfo()
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ToggleDuty -> toggleDuty()
            is HomeIntent.AcceptOrder -> acceptOrder(intent.orderId)
            is HomeIntent.RejectOrder -> rejectOrder(intent.orderId)
            is HomeIntent.NavigateToStatus -> navigateToStatus(intent.orderId)
        }
    }

    private fun observeDutyStatus() = intent {
        viewModelScope.launch {
            updateDutyStatusUseCase.getDutyStatus().collect { status ->
                reduce {
                    state.copy(dutyStatus = status)
                }

                // DUMMY: Simulate incoming order when going on duty
                if (status == DutyStatus.ON_DUTY) {
                    viewModelScope.launch {
                        orderUseCase.invoke()
                    }
                }
            }
        }
    }

    private fun observeActiveOrders() = intent {
        viewModelScope.launch {
            orderUseCase.getActiveOrders().collect { orders ->
                reduce {
                    state.copy(activeOrders = orders)
                }
            }
        }
    }

    private fun observePendingOrders() = intent {
        viewModelScope.launch {
            orderUseCase.getPendingOrder().collect { order ->
                reduce {
                    state.copy(incomingOrder = order)
                }

                // Play sound when new order arrives
                if (order != null) {
                    postSideEffect(HomeSideEffect.PlayNotificationSound)
                }
            }
        }
    }

    private fun loadCaptainInfo() = intent {
        viewModelScope.launch {
            val captain = updateDutyStatusUseCase.getCaptain()
            reduce {
                state.copy(captainName = captain?.name ?: "Captain")
            }
        }
    }

    private fun toggleDuty() = intent {
        reduce {
            state.copy(isLoading = true)
        }

        val newStatus = if (state.dutyStatus == DutyStatus.OFF_DUTY) {
            DutyStatus.ON_DUTY
        } else {
            DutyStatus.OFF_DUTY
        }

        updateDutyStatusUseCase(newStatus)
            .onSuccess {
                reduce {
                    state.copy(isLoading = false)
                }

                val message = if (newStatus == DutyStatus.ON_DUTY) {
                    "You are now ON DUTY 🟢"
                } else {
                    "You are now OFF DUTY 🔴"
                }
                postSideEffect(HomeSideEffect.ShowToast(message))
            }
            .onFailure { error ->
                reduce {
                    state.copy(isLoading = false)
                }
                postSideEffect(HomeSideEffect.ShowError(error.message ?: "Failed to update duty status"))
            }
    }

    private fun acceptOrder(orderId: String) = intent {
        reduce {
            state.copy(isLoading = true)
        }

        acceptOrderUseCase(orderId)
            .onSuccess { order ->
                reduce {
                    state.copy(
                        isLoading = false,
                        incomingOrder = null
                    )
                }

                postSideEffect(HomeSideEffect.ShowToast("Order accepted! ✅"))
                postSideEffect(HomeSideEffect.NavigateToStatusTab(order.id))
            }
            .onFailure { error ->
                reduce {
                    state.copy(isLoading = false)
                }
                postSideEffect(HomeSideEffect.ShowError(error.message ?: "Failed to accept order"))
            }
    }

    private fun rejectOrder(orderId: String) = intent {
        rejectOrderUseCase(orderId)
            .onSuccess {
                reduce {
                    state.copy(incomingOrder = null)
                }
                postSideEffect(HomeSideEffect.ShowToast("Order rejected"))
            }
            .onFailure { error ->
                postSideEffect(HomeSideEffect.ShowError(error.message ?: "Failed to reject order"))
            }
    }

    private fun navigateToStatus(orderId: String) = intent {
        postSideEffect(HomeSideEffect.NavigateToStatusTab(orderId))
    }
}