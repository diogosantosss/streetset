package com.example.streetset.data.preferences

interface ScreenOnPreferences {
    fun read(): Boolean
    fun save(enabled: Boolean)
}
