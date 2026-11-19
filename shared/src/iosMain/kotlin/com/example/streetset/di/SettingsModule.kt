package com.example.streetset.di

import com.example.streetset.data.preferences.AppInfo
import com.example.streetset.data.preferences.ScreenOnPreferences
import com.example.streetset.data.preferences.ThemePreferences
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

actual val settingsModule: Module = module {
    single<ThemePreferences> { IosThemePreferences() }
    single<ScreenOnPreferences> { IosScreenOnPreferences() }
    single { iosAppInfo() }
}

private class IosThemePreferences : ThemePreferences {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun readTheme(): String? = defaults.stringForKey(THEME_KEY)

    override fun saveTheme(name: String) {
        defaults.setObject(name, forKey = THEME_KEY)
    }
}

private class IosScreenOnPreferences : ScreenOnPreferences {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun read(): Boolean = defaults.boolForKey(KEEP_SCREEN_ON_KEY)

    override fun save(enabled: Boolean) {
        defaults.setBool(enabled, forKey = KEEP_SCREEN_ON_KEY)
    }
}

private fun iosAppInfo(): AppInfo {
    val info = NSBundle.mainBundle.infoDictionary

    return AppInfo(
        name = "StreetSet",
        versionName = info?.get("CFBundleShortVersionString") as? String ?: "1.0.0",
    )
}

private const val THEME_KEY = "theme"
private const val KEEP_SCREEN_ON_KEY = "keep_screen_on"
