package com.example.streetset.audio

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionMixWithOthers
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToFile
import streetset.shared.generated.resources.Res

@OptIn(ExperimentalForeignApi::class)
actual object AppAudio {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val players = mutableMapOf<AppSound, AVAudioPlayer>()
    private var initialized = false
    private var level = 1f

    actual var volume: Float
        get() = level
        set(value) {
            level = value.coerceIn(0f, 1f)
        }

    actual fun init() {
        if (initialized) return
        initialized = true

        configureSession()
        scope.launch {
            AppSound.entries.forEach { preload(it) }
        }
    }

    /**
     * The default category interrupts the music of other apps. Playback with "mix with others"
     * keeps the beeps audible (even with the silent switch on) without stopping it.
     */
    private fun configureSession() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(
            AVAudioSessionCategoryPlayback,
            withOptions = AVAudioSessionCategoryOptionMixWithOthers,
            error = null,
        )
        session.setActive(true, error = null)
    }

    private suspend fun preload(sound: AppSound) {
        if (players.containsKey(sound)) return
        val path = "files/sounds/${sound.fileName}"
        val bytes = Res.readBytes(path)
        val tempPath = NSTemporaryDirectory() + sound.fileName
        val nsData = bytes.toNSData()
        nsData.writeToFile(tempPath, true)

        val player = AVAudioPlayer(contentsOfURL = NSURL.fileURLWithPath(tempPath), error = null)
        player.prepareToPlay()
        players[sound] = player
    }

    actual fun play(sound: AppSound) {
        if (level <= 0f) return
        val player = players[sound] ?: return
        player.volume = level
        player.currentTime = 0.0
        player.play()
    }

    @OptIn(BetaInteropApi::class)
    private fun ByteArray.toNSData(): NSData = usePinned {
        NSData.create(bytes = it.addressOf(0), length = this.size.toULong())
    }
}