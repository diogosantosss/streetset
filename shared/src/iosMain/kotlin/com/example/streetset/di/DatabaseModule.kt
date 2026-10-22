package com.example.streetset.di

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.streetset.data.StreetSetDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSHomeDirectory

private fun provideDataBase(): StreetSetDatabase =
    Room.databaseBuilder<StreetSetDatabase>(
        name = NSHomeDirectory() + "/Documents/" + DATABASE_NAME,
    )
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(true)
        .build()

private inline fun <reified T : Any> Module.daoOf(
    crossinline provider: (StreetSetDatabase) -> T,
) = single<T> { provider(get()) }

actual val databaseModule: Module = module {
    single { provideDataBase() }

    daoOf { it.exerciseDao() }
    daoOf { it.programDao() }
    daoOf { it.weekDao() }
    daoOf { it.sessionDao() }
    daoOf { it.blockDao() }
    daoOf { it.plannedSetDao() }
    daoOf { it.sessionLogDao() }
}

private const val DATABASE_NAME = "streetset.db"