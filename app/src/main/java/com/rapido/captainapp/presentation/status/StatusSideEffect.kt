package com.rapido.captainapp.presentation.status

sealed class StatusSideEffect {
    data class ShowSuccess(val message: String) : StatusSideEffect()
    data class ShowError(val error: String) : StatusSideEffect()
    object OrderCompleted : StatusSideEffect()
    object NavigateBackToHome : StatusSideEffect()
}