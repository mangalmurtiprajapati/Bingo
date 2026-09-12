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
     * Plays a brief synthesized sound wave using AudioTrack to guarantee offline sound without external assets.
     */
    private fun playTone(frequencyHz: Double, durationMs: Int, volumeMultiplier: Float = 0.5f) {
        if (!isSoundEnabled) return

        scope.launch {
            try {
                val sampleRate = 22050
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    // Sine wave with soft attack and decay envelope
                    val envelope = when {
                        i < numSamples * 0.1 -> i / (numSamples * 0.1)
                        i > numSamples * 0.8 -> (numSamples - i) / (numSamples * 0.2)
                        else -> 1.0
                    }
                    val sampleValue = (sin(2.0 * Math.PI * frequencyHz * time) * 32767.0 * volumeMultiplier * envelope).toInt()
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
                delay(durationMs + 50L)
                audioTrack.release()
            } catch (_: Exception) {
                // Safe fallback on low memory or audio track allocation error
            }
        }
    }

    fun playClick() {
        playTone(frequencyHz = 880.0, durationMs = 60, volumeMultiplier = 0.4f)
    }

    fun playNumberPlaced() {
        playTone(frequencyHz = 659.25, durationMs = 70, volumeMultiplier = 0.5f)
    }

    fun playCrossSound() {
        // Crisp pen scratch / strike sound effect
        scope.launch {
            playTone(frequencyHz = 440.0, durationMs = 50, volumeMultiplier = 0.6f)
            delay(30)
            playTone(frequencyHz = 880.0, durationMs = 70, volumeMultiplier = 0.7f)
        }
    }

    fun playLineComplete() {
        // Bright bell chord when a line is struck
        scope.launch {
            playTone(frequencyHz = 523.25, durationMs = 90, volumeMultiplier = 0.7f) // C5
            delay(60)
            playTone(frequencyHz = 783.99, durationMs = 90, volumeMultiplier = 0.8f) // G5
            delay(60)
            playTone(frequencyHz = 1046.50, durationMs = 150, volumeMultiplier = 0.9f) // C6
        }
    }

    fun playDaub() {
        playCrossSound()
    }

    fun playBallCall() {
        playTone(frequencyHz = 783.99, durationMs = 120, volumeMultiplier = 0.5f) // G5
    }

    fun playSpinWheelTick() {
        playTone(frequencyHz = 1046.50, durationMs = 30, volumeMultiplier = 0.3f) // C6
    }

    fun playWinFanfare() {
        if (!isSoundEnabled) return
        scope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51) // C5, E5, G5, C6, E6
            notes.forEach { note ->
                playTone(frequencyHz = note, durationMs = 150, volumeMultiplier = 0.8f)
                delay(120)
            }
        }
    }

    fun playJackpotSound() {
        if (!isSoundEnabled) return
        scope.launch {
            val arpeggio = listOf(440.0, 554.37, 659.25, 880.0, 1108.73, 1318.51)
            repeat(2) {
                arpeggio.forEach { freq ->
                    playTone(frequencyHz = freq, durationMs = 90, volumeMultiplier = 0.8f)
                    delay(70)
                }
            }
        }
    }

    fun startAmbientGameMusic() {
        stopAmbientMusic()
        if (!isMusicEnabled) return

        musicJob = scope.launch {
            val scale = listOf(261.63, 329.63, 392.00, 493.88) // Soft C major 7th chord tones
            var idx = 0
            while (isMusicEnabled) {
                playTone(frequencyHz = scale[idx % scale.size], durationMs = 300, volumeMultiplier = 0.15f)
                idx++
                delay(1200)
            }
        }
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
