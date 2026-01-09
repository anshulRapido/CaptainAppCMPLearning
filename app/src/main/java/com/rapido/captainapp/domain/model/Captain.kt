package com.rapido.captainapp.domain.model

data class Captain(
    val id: String,
    val name: String,
    val phone: String,
    val dutyStatus: DutyStatus
)