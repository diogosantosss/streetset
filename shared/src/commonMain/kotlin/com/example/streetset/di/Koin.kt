package com.example.streetset.di

import com.example.streetset.data.DatabaseSeeder
import com.example.streetset.presentation.programs.ProgramsViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.dsl.module

val appModule = module {
    includes(databaseModule)

    singleOf(::DatabaseSeeder)

    viewModelOf(::ProgramsViewModel)
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        includes(config)
        modules(appModule)
    }
}

fun initKoin() = initKoin {}