package com.rapido.captainapp.domain.usecase

import com.rapido.captainapp.domain.model.DutyStatus
import com.rapido.captainapp.data.repository.CaptainRepository
import com.rapido.captainapp.domain.model.Captain
import kotlinx.coroutines.flow.Flow

class UpdateDutyStatusUseCase(
    private val captainRepository: CaptainRepository
) {
    suspend operator fun invoke(status: DutyStatus): Result<Unit> {
        return captainRepository.updateDutyStatus(status)
    }

    fun getDutyStatus(): Flow<DutyStatus> {
        return captainRepository.getDutyStatus()
    }

    suspend  fun getCaptain(): Captain? {
        return captainRepository.getCaptain()
    }
}