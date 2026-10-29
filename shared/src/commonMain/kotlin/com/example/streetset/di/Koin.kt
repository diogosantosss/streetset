package com.example.streetset.di

import com.example.streetset.data.DatabaseSeeder
import com.example.streetset.presentation.programdetail.ProgramDetailViewModel
import com.example.streetset.presentation.programdetail.CreateSessionViewModel
import com.example.streetset.presentation.programdetail.SessionOptionsViewModel
import com.example.streetset.presentation.programs.ProgramsViewModel
import com.example.streetset.presentation.programs.SheetViewModel
import com.example.streetset.presentation.sessiondetail.AddBlockViewModel
import com.example.streetset.presentation.sessiondetail.BlockOptionsViewModel
import com.example.streetset.presentation.sessiondetail.SessionBlocksViewModel
import com.example.streetset.presentation.settings.SettingsViewModel
import com.example.streetset.presentation.settings.ThemeRepository
import com.example.streetset.presentation.training.TrainingSessionViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    includes(databaseModule, settingsModule)

    singleOf(::DatabaseSeeder)
    singleOf(::ThemeRepository)

    viewModelOf(::ProgramsViewModel)
    viewModelOf(::SheetViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ProgramDetailViewModel)
    viewModelOf(::CreateSessionViewModel)
    viewModelOf(::SessionOptionsViewModel)
    viewModel { (sessionId: Int) -> SessionBlocksViewModel(sessionId) }
    viewModel { (sessionId: Int) -> AddBlockViewModel(sessionId) }
    viewModelOf(::BlockOptionsViewModel)
    viewModel { (sessionId: Int) -> TrainingSessionViewModel(sessionId) }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(appModule)
    }
}

fun initKoin() = initKoin {}
