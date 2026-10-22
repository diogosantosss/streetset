package com.example.streetset.data.preferences

interface ThemePreferences {
    fun readTheme(): String?
    fun saveTheme(name: String)
}
