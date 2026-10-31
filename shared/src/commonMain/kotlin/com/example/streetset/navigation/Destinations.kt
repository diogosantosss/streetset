package com.example.streetset.navigation

import kotlinx.serialization.Serializable

@Serializable
data object CalendarRoute

@Serializable
data object StatsRoute

@Serializable
data object SettingsRoute


@Serializable
data class SessionDetailRoute(
    val sessionId: Int,
    val title: String,
)

@Serializable
data class TrainingSessionRoute(
    val sessionId: Int,
    val title: String,
)

