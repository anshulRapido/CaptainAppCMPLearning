package com.rapido.captainapp.domain.usecase

import com.rapido.captainapp.domain.model.DutyStatus
import com.rapido.captainapp.data.repository.CaptainRepository

class UpdateDutyStatusUseCase(
    private val captainRepository: CaptainRepository
) {
    suspend operator fun invoke(status: DutyStatus): Result<Unit> {
        return captainRepository.updateDutyStatus(status)
    }
}