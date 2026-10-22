package com.example.streetset.di

import android.content.Context
import android.os.Build
import com.example.streetset.data.preferences.AppInfo
import com.example.streetset.data.preferences.ThemePreferences
import com.example.streetset.util.fullDisplay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.time.Instant
import androidx.core.content.edit

actual val settingsModule: Module = module {
    single<ThemePreferences> { AndroidThemePreferences(androidContext()) }
    single { androidAppInfo(androidContext()) }
}

private class AndroidThemePreferences(context: Context) : ThemePreferences {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun readTheme(): String? = preferences.getString(THEME_KEY, null)

    override fun saveTheme(name: String) {
        preferences.edit { putString(THEME_KEY, name) }
    }
}

private fun androidAppInfo(context: Context): AppInfo {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

    val lastUpdated = Instant.fromEpochMilliseconds(packageInfo.lastUpdateTime)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .fullDisplay()

    val buildNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode.toString()
    } else {
        @Suppress("DEPRECATION")
        packageInfo.versionCode.toString()
    }

    return AppInfo(
        name = "StreetSet",
        versionName = packageInfo.versionName ?: "-",
        buildNumber = buildNumber,
        lastUpdated = lastUpdated,
    )
}

private const val PREFERENCES_NAME = "streetset_preferences"
private const val THEME_KEY = "theme"
