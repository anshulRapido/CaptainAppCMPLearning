package com.rapido.captainapp.domain.usecase

import com.rapido.captainapp.domain.model.Captain
import com.rapido.captainapp.domain.model.DutyStatus
import kotlinx.coroutines.flow.Flow

interface CaptainRepository {
    // Observe captain duty status
    fun getDutyStatus(): Flow<DutyStatus>

    // Update duty status
    suspend fun updateDutyStatus(status: DutyStatus): Result<Unit>

    // Get captain info
    suspend fun getCaptain(): Captain?
}