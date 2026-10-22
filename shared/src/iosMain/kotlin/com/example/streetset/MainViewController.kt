package com.example.streetset

import androidx.compose.ui.window.ComposeUIViewController
import com.example.streetset.di.initKoin
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

@Suppress("unused")
fun MainViewController() = ComposeUIViewController (
    configure = {
        Napier.base(DebugAntilog())
        initKoin()
    }
) { App() }
