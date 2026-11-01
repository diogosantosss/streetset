package com.example.streetset.di

import com.example.streetset.data.DatabaseSeeder
import com.example.streetset.presentation.calendar.CalendarViewModel
import com.example.streetset.presentation.calendar.CreateSessionCalendarViewModel
import com.example.streetset.presentation.calendar.DeleteSessionViewModel
import com.example.streetset.presentation.sessiondetail.AddBlockViewModel
import com.example.streetset.presentation.sessiondetail.BlockOptionsViewModel
import com.example.streetset.presentation.sessiondetail.SessionBlocksViewModel
import com.example.streetset.presentation.settings.SettingsViewModel
import com.example.streetset.presentation.settings.ThemeRepository
import com.example.streetset.presentation.stats.StatsViewModel
import com.example.streetset.presentation.training.TrainingSessionViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    includes(databaseModule, settingsModule)

    // single instances
    singleOf(::DatabaseSeeder)
    singleOf(::ThemeRepository)

    // Tabs related VMs region
    // Calendar
    viewModelOf(::CalendarViewModel)
    viewModelOf(::CreateSessionCalendarViewModel)
    viewModelOf(::DeleteSessionViewModel)

    // Stats
    viewModelOf(::StatsViewModel)

    // Settings
    viewModelOf(::SettingsViewModel)


    // Session Details VMs
    viewModel { (sessionId: Int) -> SessionBlocksViewModel(sessionId) }
    viewModel { (sessionId: Int) -> AddBlockViewModel(sessionId) }
    viewModelOf(::BlockOptionsViewModel)

    // Training VM
    viewModel { (sessionId: Int) -> TrainingSessionViewModel(sessionId) }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(appModule)
    }
}

fun initKoin() = initKoin {}
