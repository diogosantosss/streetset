package com.example.streetset.di

import android.content.Context
import androidx.room3.Room
import com.example.streetset.data.StreetSetDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

private fun provideDataBase(context: Context): StreetSetDatabase =
    Room.databaseBuilder(
        context,
        StreetSetDatabase::class.java,
        DATABASE_NAME,
    )
        .fallbackToDestructiveMigration(true)
        .build()

private inline fun <reified T : Any> Module.daoOf(
    crossinline provider: (StreetSetDatabase) -> T,
) = single<T> { provider(get()) }

actual val databaseModule: Module = module {
    single { provideDataBase(androidContext()) }

    daoOf { it.exerciseDao() }
    daoOf { it.programDao() }
    daoOf { it.weekDao() }
    daoOf { it.sessionDao() }
    daoOf { it.blockDao() }
    daoOf { it.plannedSetDao() }
    daoOf { it.sessionLogDao() }
}

private const val DATABASE_NAME = "streetset.db"