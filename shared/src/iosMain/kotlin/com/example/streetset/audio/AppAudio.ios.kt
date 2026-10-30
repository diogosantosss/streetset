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

    actual fun init() {
        if (initialized) return
        initialized = true

        scope.launch {
            AppSound.entries.forEach { preload(it) }
        }
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
        val player = players[sound] ?: return
        player.currentTime = 0.0
        player.play()
    }

    @OptIn(BetaInteropApi::class)
    private fun ByteArray.toNSData(): NSData = usePinned {
        NSData.create(bytes = it.addressOf(0), length = this.size.toULong())
    }
}
