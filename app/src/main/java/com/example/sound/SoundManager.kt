package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val context: Context) {

    private val soundPool: SoundPool
    private val scope = CoroutineScope(Dispatchers.Default)

    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true

    private var musicJob: Job? = null

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
     * to eliminate any audio pops or clicks.
     */
    private fun playTone(frequencyHz: Double, durationMs: Int, volumeMultiplier: Float = 0.5f) {
        if (!isSoundEnabled) return

        scope.launch {
            try {
                val sampleRate = 22050
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(100)
                val samples = ShortArray(numSamples)

                val attackSamples = (numSamples * 0.15).toInt().coerceAtLeast(10)
                val releaseSamples = (numSamples * 0.35).toInt().coerceAtLeast(10)

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    // Smooth Hann / raised cosine envelope to prevent clicking
                    val envelope = when {
                        i < attackSamples -> 0.5 * (1.0 - Math.cos(Math.PI * i / attackSamples))
                        i >= numSamples - releaseSamples -> 0.5 * (1.0 + Math.cos(Math.PI * (i - (numSamples - releaseSamples)) / releaseSamples))
                        else -> 1.0
                    }
                    val sampleValue = (sin(2.0 * Math.PI * frequencyHz * time) * 28000.0 * volumeMultiplier * envelope).toInt()
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
                delay(durationMs + 40L)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            } catch (_: Exception) {
                // Fallback on low memory
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
        // Crisp pen strike sound
        scope.launch {
            playTone(frequencyHz = 480.0, durationMs = 45, volumeMultiplier = 0.5f)
            delay(35)
            playTone(frequencyHz = 820.0, durationMs = 60, volumeMultiplier = 0.6f)
        }
    }

    fun playLineComplete() {
        // Bright celebration chord when a line is struck
        scope.launch {
            playTone(frequencyHz = 523.25, durationMs = 80, volumeMultiplier = 0.6f) // C5
            delay(70)
            playTone(frequencyHz = 659.25, durationMs = 80, volumeMultiplier = 0.65f) // E5
            delay(70)
            playTone(frequencyHz = 783.99, durationMs = 80, volumeMultiplier = 0.7f) // G5
            delay(70)
            playTone(frequencyHz = 1046.50, durationMs = 140, volumeMultiplier = 0.8f) // C6
        }
    }

    fun playDaub() {
        playCrossSound()
    }

    fun playBallCall() {
        playTone(frequencyHz = 783.99, durationMs = 100, volumeMultiplier = 0.45f) // G5
    }

    fun playSpinWheelTick() {
        playTone(frequencyHz = 1046.50, durationMs = 25, volumeMultiplier = 0.25f) // C6
    }

    fun playWinFanfare() {
        if (!isSoundEnabled) return
        scope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51) // C5, E5, G5, C6, E6
            notes.forEach { note ->
                playTone(frequencyHz = note, durationMs = 120, volumeMultiplier = 0.75f)
                delay(110)
            }
        }
    }

    fun playJackpotSound() {
        if (!isSoundEnabled) return
        scope.launch {
            val arpeggio = listOf(440.0, 554.37, 659.25, 880.0, 1108.73, 1318.51)
            repeat(2) {
                arpeggio.forEach { freq ->
                    playTone(frequencyHz = freq, durationMs = 80, volumeMultiplier = 0.75f)
                    delay(65)
                }
            }
        }
    }

    fun startAmbientGameMusic() {
        // Kept silent during regular play so tones do not interrupt or annoy the user during thinking/tapping.
        stopAmbientMusic()
    }

    fun stopAmbientMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    fun release() {
        stopAmbientMusic()
        soundPool.release()
    }
}
