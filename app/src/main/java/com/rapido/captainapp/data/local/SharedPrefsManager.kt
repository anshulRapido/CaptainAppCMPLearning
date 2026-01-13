package com.rapido.captainapp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.rapido.captainapp.domain.model.DutyStatus

class SharedPrefsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "captain_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_DUTY_STATUS = "duty_status"
        private const val KEY_CAPTAIN_ID = "captain_id"
        private const val KEY_CAPTAIN_NAME = "captain_name"
        private const val KEY_SERVICE_RUNNING = "key_service_running"
    }

    fun saveDutyStatus(status: DutyStatus) {
        prefs.edit().putString(KEY_DUTY_STATUS, status.name).apply()
    }

    fun getDutyStatus(): DutyStatus {
        val statusString = prefs.getString(KEY_DUTY_STATUS, DutyStatus.OFF_DUTY.name)
        return DutyStatus.valueOf(statusString ?: DutyStatus.OFF_DUTY.name)
    }

    fun saveCaptainId(captainId: String) {
        prefs.edit().putString(KEY_CAPTAIN_ID, captainId).apply()
    }

    fun getCaptainId(): String {
        return prefs.getString(KEY_CAPTAIN_ID, "CAPT001") ?: "CAPT001"
    }

    fun saveCaptainName(name: String) {
        prefs.edit().putString(KEY_CAPTAIN_NAME, name).apply()
    }

    fun getCaptainName(): String {
        return prefs.getString(KEY_CAPTAIN_NAME, "Captain") ?: "Captain"
    }

    fun saveServiceRunning(isRunning: Boolean) {
        prefs.edit().putBoolean(KEY_SERVICE_RUNNING, isRunning).apply()
    }

    fun isServiceRunning(): Boolean {
        return prefs.getBoolean(KEY_SERVICE_RUNNING, false)
    }
}