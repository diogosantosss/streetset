package com.example.streetset.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import streetset.shared.generated.resources.Res
import java.io.File

actual object AppAudio {

    private val context: Context by lazy { KoinPlatform.getKoin().get() }
    private val scope = CoroutineScope(Dispatchers.IO)

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds = mutableMapOf<AppSound, Int>()
    private var initialized = false

    actual fun init() {
        if (initialized) return
        initialized = true

        scope.launch {
            AppSound.entries.forEach { preload(it) }
        }
    }

    private suspend fun preload(sound: AppSound) {
        if (soundIds.containsKey(sound)) return
        val path = "files/sounds/${sound.fileName}"
        val bytes = Res.readBytes(path)
        val file = File(context.cacheDir, sound.fileName)
        if (!file.exists()) {
            file.writeBytes(bytes)
        }
        val id = soundPool.load(file.absolutePath, 1)
        soundIds[sound] = id
    }

    actual fun play(sound: AppSound) {
        val id = soundIds[sound] ?: return
        soundPool.play(id, 1f, 1f, 0, 0, 1f)
    }
}
