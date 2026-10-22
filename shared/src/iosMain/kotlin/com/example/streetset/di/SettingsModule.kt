package com.example.streetset.di

import com.example.streetset.data.preferences.AppInfo
import com.example.streetset.data.preferences.ThemePreferences
import com.example.streetset.util.fullDisplay
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileModificationDate
import platform.Foundation.NSUserDefaults
import platform.Foundation.timeIntervalSince1970
import kotlin.time.Instant

actual val settingsModule: Module = module {
    single<ThemePreferences> { IosThemePreferences() }
    single { iosAppInfo() }
}

private class IosThemePreferences : ThemePreferences {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun readTheme(): String? = defaults.stringForKey(THEME_KEY)

    override fun saveTheme(name: String) {
        defaults.setObject(name, forKey = THEME_KEY)
    }
}

private fun iosAppInfo(): AppInfo {
    val info = NSBundle.mainBundle.infoDictionary

    return AppInfo(
        name = "StreetSet",
        versionName = info?.get("CFBundleShortVersionString") as? String ?: "-",
        buildNumber = info?.get("CFBundleVersion") as? String ?: "-",
        lastUpdated = bundleLastUpdated() ?: "-",
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun bundleLastUpdated(): String? {
    val attributes = NSFileManager.defaultManager.attributesOfItemAtPath(
        path = NSBundle.mainBundle.bundlePath,
        error = null,
    ) ?: return null
    val date = attributes[NSFileModificationDate] as? NSDate ?: return null

    return Instant.fromEpochSeconds(date.timeIntervalSince1970.toLong())
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .fullDisplay()
}

private const val THEME_KEY = "theme"
