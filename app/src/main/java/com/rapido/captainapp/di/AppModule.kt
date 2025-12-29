package com.rapido.captainapp.di

import android.content.Context
import com.rapido.captainapp.data.local.CaptainDatabase
import com.rapido.captainapp.data.local.SharedPrefsManager
import com.rapido.captainapp.data.repository.CaptainRepositoryImpl
import com.rapido.captainapp.data.repository.OrderRepositoryImpl
import com.rapido.captainapp.domain.repository.CaptainRepository
import com.rapido.captainapp.domain.repository.OrderRepository
import com.rapido.captainapp.domain.usecase.AcceptOrderUseCase
import com.rapido.captainapp.domain.usecase.RejectOrderUseCase
import com.rapido.captainapp.domain.usecase.UpdateDutyStatusUseCase
import com.rapido.captainapp.domain.usecase.UpdateOrderStatusUseCase
import com.rapido.captainapp.presentation.home.HomeViewModel
import com.rapido.captainapp.presentation.status.StatusViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single { CaptainDatabase.getDatabase(androidContext()) }
    single { get<CaptainDatabase>().orderDao() }

    // SharedPreferences
    single { SharedPrefsManager(androidContext()) }

    // Repositories
    single<OrderRepository> { OrderRepositoryImpl(get()) }
    single<CaptainRepository> { CaptainRepositoryImpl(get()) }

    // Use Cases
    single { UpdateDutyStatusUseCase(get()) }
    single { AcceptOrderUseCase(get()) }
    single { RejectOrderUseCase(get()) }
    single { UpdateOrderStatusUseCase(get()) }

    // ViewModels
    viewModel {
        HomeViewModel(
            updateDutyStatusUseCase = get(),
            acceptOrderUseCase = get(),
            rejectOrderUseCase = get(),
            orderRepository = get(),
            captainRepository = get()
        )
    }

    viewModel {
        StatusViewModel(
            updateOrderStatusUseCase = get(),
            orderRepository = get()
        )
    }
}