package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundManager(private val context: Context) {

    private val soundPool: SoundPool
    private val scope = CoroutineScope(Dispatchers.Default)

    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true

    private var musicTrack: AudioTrack? = null
    private var isSynthesizingMusic = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    /**
     * Plays a smooth synthesized sound wave using AudioTrack with raised cosine envelope
     * and pleasant musical overtones.
     */
    private fun playTone(frequencyHz: Double, durationMs: Int, volumeMultiplier: Float = 0.5f) {
        if (!isSoundEnabled) return

        scope.launch {
            try {
                val sampleRate = 22050
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(100)
                val samples = ShortArray(numSamples)

                val attackSamples = (numSamples * 0.12).toInt().coerceAtLeast(10)
                val releaseSamples = (numSamples * 0.35).toInt().coerceAtLeast(10)

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    // Smooth Hann / raised cosine envelope to prevent clicking
                    val envelope = when {
                        i < attackSamples -> 0.5 * (1.0 - Math.cos(PI * i / attackSamples))
                        i >= numSamples - releaseSamples -> 0.5 * (1.0 + Math.cos(PI * (i - (numSamples - releaseSamples)) / releaseSamples))
                        else -> 1.0
                    }
                    // Add subtle 2nd harmonic for rich bell timbre
                    val wave = sin(2.0 * PI * frequencyHz * time) + 0.22 * sin(4.0 * PI * frequencyHz * time)
                    val sampleValue = (wave * 22000.0 * volumeMultiplier * envelope).toInt()
                    samples[i] = sampleValue.coerceIn(-32768, 32767).toShort()
                }

                val bufferSize = samples.size * 2
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()
                delay(durationMs + 30L)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            } catch (_: Exception) {
                // Ignore audio track allocation fallbacks
            }
        }
    }

    fun playClick() {
        playTone(frequencyHz = 750.0, durationMs = 45, volumeMultiplier = 0.35f)
    }

    fun playNumberPlaced() {
        playTone(frequencyHz = 620.0, durationMs = 55, volumeMultiplier = 0.45f)
    }

    fun playCrossSound() {
        // Crisp crayon or pencil strike sound
        scope.launch {
            playTone(frequencyHz = 480.0, durationMs = 45, volumeMultiplier = 0.45f)
            delay(35)
            playTone(frequencyHz = 820.0, durationMs = 60, volumeMultiplier = 0.55f)
        }
    }

    fun playLineComplete() {
        // Bright cheerful chord when a line is completed
        scope.launch {
            playTone(frequencyHz = 523.25, durationMs = 80, volumeMultiplier = 0.55f) // C5
            delay(70)
            playTone(frequencyHz = 659.25, durationMs = 80, volumeMultiplier = 0.60f) // E5
            delay(70)
            playTone(frequencyHz = 783.99, durationMs = 80, volumeMultiplier = 0.65f) // G5
            delay(70)
            playTone(frequencyHz = 1046.50, durationMs = 140, volumeMultiplier = 0.75f) // C6
        }
    }

    fun playDaub() {
        playCrossSound()
    }

    fun playBallCall() {
        playTone(frequencyHz = 783.99, durationMs = 100, volumeMultiplier = 0.45f) // G5
    }

    fun playSpinWheelTick() {
        playTone(frequencyHz = 1046.50, durationMs = 25, volumeMultiplier = 0.22f) // C6
    }

    fun playWinFanfare() {
        if (!isSoundEnabled) return
        scope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51) // C5, E5, G5, C6, E6
            notes.forEach { note ->
                playTone(frequencyHz = note, durationMs = 120, volumeMultiplier = 0.70f)
                delay(110)
            }
        }
    }

    fun playMegaStarCelebration() {
        if (!isSoundEnabled) return
        scope.launch {
            val arpeggio = listOf(440.0, 554.37, 659.25, 880.0, 1108.73, 1318.51)
            repeat(2) {
                arpeggio.forEach { freq ->
                    playTone(frequencyHz = freq, durationMs = 80, volumeMultiplier = 0.70f)
                    delay(65)
                }
            }
        }
    }

    fun playJackpotSound() {
        playMegaStarCelebration()
    }

    /**
     * Synthesizes and loops a gentle, cheerful children's music-box melody in the background.
     * Uses a single AudioTrack in MODE_STATIC with infinite hardware loop points for silky smooth,
     * low-CPU playback.
     */
    @Synchronized
    fun startAmbientGameMusic() {
        if (!isMusicEnabled) return

        val existingTrack = musicTrack
        if (existingTrack != null) {
            try {
                if (existingTrack.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    existingTrack.play()
                }
                return
            } catch (_: Exception) {}
        }

        if (isSynthesizingMusic) return
        isSynthesizingMusic = true

        scope.launch(Dispatchers.Default) {
            try {
                val sampleRate = 22050
                // Sweet, cheerful kids melody (Twinkle Twinkle Little Star & Happy Day Chimes)
                val notes = listOf(
                    Pair(523.25, 340), // C5
                    Pair(523.25, 340), // C5
                    Pair(783.99, 340), // G5
                    Pair(783.99, 340), // G5
                    Pair(880.00, 340), // A5
                    Pair(880.00, 340), // A5
                    Pair(783.99, 680), // G5 (hold)

                    Pair(698.46, 340), // F5
                    Pair(698.46, 340), // F5
                    Pair(659.25, 340), // E5
                    Pair(659.25, 340), // E5
                    Pair(587.33, 340), // D5
                    Pair(587.33, 340), // D5
                    Pair(523.25, 680), // C5 (resolve)

                    Pair(783.99, 340), // G5
                    Pair(783.99, 340), // G5
                    Pair(698.46, 340), // F5
                    Pair(698.46, 340), // F5
                    Pair(659.25, 340), // E5
                    Pair(659.25, 340), // E5
                    Pair(587.33, 680), // D5 (hold)

                    Pair(523.25, 340), // C5
                    Pair(523.25, 340), // C5
                    Pair(783.99, 340), // G5
                    Pair(783.99, 340), // G5
                    Pair(880.00, 340), // A5
                    Pair(880.00, 340), // A5
                    Pair(783.99, 680), // G5 (hold)

                    Pair(698.46, 340), // F5
                    Pair(698.46, 340), // F5
                    Pair(659.25, 340), // E5
                    Pair(659.25, 340), // E5
                    Pair(587.33, 340), // D5
                    Pair(587.33, 340), // D5
                    Pair(523.25, 750), // C5 (grand finish)

                    Pair(0.0, 600)     // Peaceful breath before seamless repeat
                )

                var totalSamplesCount = 0
                for ((_, dur) in notes) {
                    totalSamplesCount += (sampleRate * (dur / 1000.0)).toInt()
                }

                val pcmData = ShortArray(totalSamplesCount)
                var offset = 0

                for ((freq, dur) in notes) {
                    val noteSamples = (sampleRate * (dur / 1000.0)).toInt()
                    if (freq > 20.0) {
                        for (i in 0 until noteSamples) {
                            val t = i.toDouble() / sampleRate
                            // Celesta / music-box natural exponential decay
                            val decay = exp(-2.6 * i / noteSamples)
                            // Warm toy music-box harmonics
                            val wave = sin(2.0 * PI * freq * t) +
                                    0.28 * sin(4.0 * PI * freq * t) +
                                    0.10 * sin(6.0 * PI * freq * t)
                            val sampleVal = (wave * 8500.0 * decay).toInt()
                            pcmData[offset + i] = sampleVal.coerceIn(-32768, 32767).toShort()
                        }
                    }
                    offset += noteSamples
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcmData.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcmData, 0, pcmData.size)
                track.setLoopPoints(0, pcmData.size, -1) // Infinite loop
                track.setVolume(0.25f)

                synchronized(this@SoundManager) {
                    musicTrack?.release()
                    musicTrack = track
                    isSynthesizingMusic = false
                    if (isMusicEnabled) {
                        try {
                            track.play()
                        } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {
                isSynthesizingMusic = false
            }
        }
    }

    @Synchronized
    fun stopAmbientMusic() {
        try {
            musicTrack?.pause()
        } catch (_: Exception) {}
    }

    @Synchronized
    fun release() {
        try {
            musicTrack?.stop()
            musicTrack?.release()
            musicTrack = null
        } catch (_: Exception) {}
        soundPool.release()
    }
}
