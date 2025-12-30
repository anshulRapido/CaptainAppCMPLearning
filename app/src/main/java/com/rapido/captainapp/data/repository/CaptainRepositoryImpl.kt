package com.rapido.captainapp.data.repository

import com.rapido.captainapp.data.local.SharedPrefsManager
import com.rapido.captainapp.domain.model.Captain
import com.rapido.captainapp.domain.model.DutyStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class CaptainRepositoryImpl(
    private val prefsManager: SharedPrefsManager
) : CaptainRepository {

    private val _dutyStatus = MutableStateFlow(prefsManager.getDutyStatus())

    override fun getDutyStatus(): Flow<DutyStatus> {
        return _dutyStatus
    }

    override suspend fun updateDutyStatus(status: DutyStatus): Result<Unit> {
        return try {
            // Simulate API call
            delay(500)

            // Save to SharedPreferences
            prefsManager.saveDutyStatus(status)

            // Update flow
            _dutyStatus.value = status

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCaptain(): Captain {
        return Captain(
            id = prefsManager.getCaptainId(),
            name = prefsManager.getCaptainName(),
            phone = "+91 9876543210",
            dutyStatus = prefsManager.getDutyStatus()
        )
    }
}