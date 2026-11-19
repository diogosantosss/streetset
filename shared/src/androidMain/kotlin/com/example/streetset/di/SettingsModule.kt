package com.example.streetset.di

import android.content.Context
import androidx.core.content.edit
import com.example.streetset.data.preferences.AppInfo
import com.example.streetset.data.preferences.ScreenOnPreferences
import com.example.streetset.data.preferences.ThemePreferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val settingsModule: Module = module {
    single<ThemePreferences> { AndroidThemePreferences(androidContext()) }
    single<ScreenOnPreferences> { AndroidScreenOnPreferences(androidContext()) }
    single { androidAppInfo(androidContext()) }
}

private class AndroidThemePreferences(context: Context) : ThemePreferences {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun readTheme(): String? = preferences.getString(THEME_KEY, null)

    override fun saveTheme(name: String) {
        preferences.edit { putString(THEME_KEY, name) }
    }
}

private class AndroidScreenOnPreferences(context: Context) : ScreenOnPreferences {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun read(): Boolean = preferences.getBoolean(KEEP_SCREEN_ON_KEY, false)

    override fun save(enabled: Boolean) {
        preferences.edit { putBoolean(KEEP_SCREEN_ON_KEY, enabled) }
    }
}

private fun androidAppInfo(context: Context): AppInfo {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

    return AppInfo(
        name = "StreetSet",
        versionName = packageInfo.versionName ?: "1.0.0",
    )
}

private const val PREFERENCES_NAME = "streetset_preferences"
private const val THEME_KEY = "theme"
private const val KEEP_SCREEN_ON_KEY = "keep_screen_on"
