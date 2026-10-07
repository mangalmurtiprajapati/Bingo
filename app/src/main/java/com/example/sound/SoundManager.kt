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
                    val envelope = when {
                        i < attackSamples -> 0.5 * (1.0 - Math.cos(PI * i / attackSamples))
                        i >= numSamples - releaseSamples -> 0.5 * (1.0 + Math.cos(PI * (i - (numSamples - releaseSamples)) / releaseSamples))
                        else -> 1.0
                    }
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
        scope.launch {
            playTone(frequencyHz = 480.0, durationMs = 45, volumeMultiplier = 0.45f)
            delay(35)
            playTone(frequencyHz = 820.0, durationMs = 60, volumeMultiplier = 0.55f)
        }
    }

    fun playLineComplete() {
        scope.launch {
            playTone(frequencyHz = 523.25, durationMs = 80, volumeMultiplier = 0.55f)
            delay(70)
            playTone(frequencyHz = 659.25, durationMs = 80, volumeMultiplier = 0.60f)
            delay(70)
            playTone(frequencyHz = 783.99, durationMs = 80, volumeMultiplier = 0.65f)
            delay(70)
            playTone(frequencyHz = 1046.50, durationMs = 140, volumeMultiplier = 0.75f)
        }
    }

    fun playDaub() {
        playCrossSound()
    }

    fun playBallCall() {
        playTone(frequencyHz = 783.99, durationMs = 100, volumeMultiplier = 0.45f)
    }

    fun playSpinWheelTick() {
        playTone(frequencyHz = 1046.50, durationMs = 25, volumeMultiplier = 0.22f)
    }

    fun playWinFanfare() {
        if (!isSoundEnabled) return
        scope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51)
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
     * Synthesizes and loops:
     * "Soothing slice-of-life instrumental, retro Japanese cartoon vibe, soft piano and glockenspiel,
     * light koto accents, warm and peaceful, slow tempo 80 BPM, simple repeating melody, nostalgic, loopable, no vocals"
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
                val totalDurationSec = 24.0 // 8 measures at 80 BPM (3 sec/measure)
                val totalSamples = (sampleRate * totalDurationSec).toInt()

                // Polyphonic mixing buffer using floating point accumulation
                val mixBuffer = FloatArray(totalSamples)

                fun addNote(
                    startMs: Long,
                    durationMs: Long,
                    freq: Double,
                    volume: Float,
                    instrumentType: String // "piano", "glock", "koto", "bass"
                ) {
                    if (freq <= 20.0) return
                    val startSample = (sampleRate * (startMs / 1000.0)).toInt()
                    val noteSamples = (sampleRate * (durationMs / 1000.0)).toInt()

                    for (i in 0 until noteSamples) {
                        val sampleIdx = startSample + i
                        if (sampleIdx >= totalSamples) break
                        val t = i.toDouble() / sampleRate
                        val progress = i.toDouble() / noteSamples

                        val sampleVal = when (instrumentType) {
                            "piano" -> {
                                val decay = exp(-2.0 * progress)
                                val wave = sin(2.0 * PI * freq * t) +
                                        0.35 * sin(4.0 * PI * freq * t) +
                                        0.12 * sin(6.0 * PI * freq * t)
                                wave * decay * volume
                            }
                            "glock" -> {
                                val bellDecay = exp(-3.6 * progress)
                                val shimmer = sin(2.0 * PI * freq * t) +
                                        0.18 * sin(2.0 * PI * (freq * 2.76) * t) +
                                        0.10 * sin(2.0 * PI * (freq * 5.4) * t)
                                shimmer * bellDecay * volume
                            }
                            "koto" -> {
                                val pluckDecay = exp(-2.8 * progress)
                                val pluckAttack = (1.0 - exp(-15.0 * progress)).coerceAtMost(1.0)
                                val wave = sin(2.0 * PI * freq * t) +
                                        0.45 * sin(4.0 * PI * freq * t) +
                                        0.22 * sin(6.0 * PI * freq * t)
                                wave * pluckAttack * pluckDecay * volume
                            }
                            "bass" -> {
                                val bassDecay = exp(-1.4 * progress)
                                val wave = sin(2.0 * PI * freq * t) + 0.4 * sin(4.0 * PI * freq * t)
                                wave * bassDecay * volume
                            }
                            else -> 0.0
                        }
                        mixBuffer[sampleIdx] += sampleVal.toFloat()
                    }
                }

                // --- 80 BPM SLICE-OF-LIFE RETRO JAPANESE NOSTALGIC COMPOSITION ---
                // Beat = 750ms. Measures of 3000ms each in G Major Pentatonic

                // Measure 1 (0ms - 3000ms): G Major Sunlit Awakening
                addNote(startMs = 0L, durationMs = 2800L, freq = 98.0, volume = 0.45f, instrumentType = "bass") // G2
                addNote(startMs = 0L, durationMs = 700L, freq = 392.00, volume = 0.40f, instrumentType = "piano") // G4
                addNote(startMs = 750L, durationMs = 700L, freq = 493.88, volume = 0.42f, instrumentType = "piano") // B4
                addNote(startMs = 1500L, durationMs = 700L, freq = 587.33, volume = 0.45f, instrumentType = "piano") // D5
                addNote(startMs = 1500L, durationMs = 900L, freq = 783.99, volume = 0.28f, instrumentType = "glock") // G5
                addNote(startMs = 2250L, durationMs = 700L, freq = 659.25, volume = 0.38f, instrumentType = "koto")  // E5

                // Measure 2 (3000ms - 6000ms): E Minor Pastoral Breeze
                addNote(startMs = 3000L, durationMs = 2800L, freq = 164.81, volume = 0.42f, instrumentType = "bass") // E3
                addNote(startMs = 3000L, durationMs = 1100L, freq = 783.99, volume = 0.48f, instrumentType = "piano") // G5 (sustained)
                addNote(startMs = 3000L, durationMs = 1000L, freq = 987.77, volume = 0.26f, instrumentType = "glock") // B5
                addNote(startMs = 4125L, durationMs = 350L, freq = 659.25, volume = 0.35f, instrumentType = "koto")  // E5 grace
                addNote(startMs = 4500L, durationMs = 700L, freq = 587.33, volume = 0.42f, instrumentType = "piano") // D5
                addNote(startMs = 5250L, durationMs = 700L, freq = 493.88, volume = 0.38f, instrumentType = "koto")  // B4

                // Measure 3 (6000ms - 9000ms): C Major Warm Nostalgia
                addNote(startMs = 6000L, durationMs = 2800L, freq = 130.81, volume = 0.44f, instrumentType = "bass") // C3
                addNote(startMs = 6000L, durationMs = 700L, freq = 440.00, volume = 0.40f, instrumentType = "piano") // A4
                addNote(startMs = 6750L, durationMs = 700L, freq = 493.88, volume = 0.42f, instrumentType = "piano") // B4
                addNote(startMs = 7500L, durationMs = 700L, freq = 587.33, volume = 0.45f, instrumentType = "piano") // D5
                addNote(startMs = 7500L, durationMs = 900L, freq = 987.77, volume = 0.28f, instrumentType = "glock") // B5
                addNote(startMs = 8250L, durationMs = 700L, freq = 440.00, volume = 0.38f, instrumentType = "koto")  // A4

                // Measure 4 (9000ms - 12000ms): D Major Peaceful Breath
                addNote(startMs = 9000L, durationMs = 2800L, freq = 146.83, volume = 0.42f, instrumentType = "bass") // D3
                addNote(startMs = 9000L, durationMs = 1450L, freq = 392.00, volume = 0.45f, instrumentType = "piano") // G4 (restful)
                addNote(startMs = 9000L, durationMs = 1200L, freq = 587.33, volume = 0.25f, instrumentType = "glock") // D5
                addNote(startMs = 10500L, durationMs = 700L, freq = 440.00, volume = 0.38f, instrumentType = "koto") // A4
                addNote(startMs = 11250L, durationMs = 700L, freq = 493.88, volume = 0.40f, instrumentType = "piano") // B4

                // Measure 5 (12000ms - 15000ms): G Major Melody Ascending Variation
                addNote(startMs = 12000L, durationMs = 2800L, freq = 98.0, volume = 0.45f, instrumentType = "bass") // G2
                addNote(startMs = 12000L, durationMs = 700L, freq = 587.33, volume = 0.42f, instrumentType = "piano") // D5
                addNote(startMs = 12750L, durationMs = 700L, freq = 659.25, volume = 0.42f, instrumentType = "piano") // E5
                addNote(startMs = 13500L, durationMs = 700L, freq = 783.99, volume = 0.45f, instrumentType = "piano") // G5
                addNote(startMs = 13500L, durationMs = 900L, freq = 1174.66, volume = 0.28f, instrumentType = "glock") // D6
                addNote(startMs = 14250L, durationMs = 700L, freq = 880.00, volume = 0.38f, instrumentType = "koto") // A5

                // Measure 6 (15000ms - 18000ms): E Minor Peak of Melody
                addNote(startMs = 15000L, durationMs = 2800L, freq = 164.81, volume = 0.42f, instrumentType = "bass") // E3
                addNote(startMs = 15000L, durationMs = 1100L, freq = 987.77, volume = 0.48f, instrumentType = "piano") // B5
                addNote(startMs = 15000L, durationMs = 1000L, freq = 783.99, volume = 0.26f, instrumentType = "glock") // G5
                addNote(startMs = 16125L, durationMs = 350L, freq = 880.00, volume = 0.35f, instrumentType = "koto")  // A5 grace
                addNote(startMs = 16500L, durationMs = 700L, freq = 783.99, volume = 0.42f, instrumentType = "piano") // G5
                addNote(startMs = 17250L, durationMs = 700L, freq = 659.25, volume = 0.38f, instrumentType = "koto")  // E5

                // Measure 7 (18000ms - 21000ms): C Major Return Home
                addNote(startMs = 18000L, durationMs = 2800L, freq = 130.81, volume = 0.44f, instrumentType = "bass") // C3
                addNote(startMs = 18000L, durationMs = 700L, freq = 587.33, volume = 0.42f, instrumentType = "piano") // D5
                addNote(startMs = 18750L, durationMs = 700L, freq = 493.88, volume = 0.40f, instrumentType = "piano") // B4
                addNote(startMs = 18750L, durationMs = 800L, freq = 987.77, volume = 0.24f, instrumentType = "glock") // B5
                addNote(startMs = 19500L, durationMs = 700L, freq = 440.00, volume = 0.38f, instrumentType = "koto")  // A4
                addNote(startMs = 20250L, durationMs = 700L, freq = 392.00, volume = 0.40f, instrumentType = "piano") // G4

                // Measure 8 (21000ms - 24000ms): G Major Peaceful Resolve & Infinite Loop Transition
                addNote(startMs = 21000L, durationMs = 2500L, freq = 98.0, volume = 0.45f, instrumentType = "bass") // G2
                addNote(startMs = 21000L, durationMs = 2000L, freq = 392.00, volume = 0.45f, instrumentType = "piano") // G4 (sustained resolution)
                addNote(startMs = 21000L, durationMs = 1500L, freq = 783.99, volume = 0.25f, instrumentType = "glock") // G5
                addNote(startMs = 21750L, durationMs = 1200L, freq = 1174.66, volume = 0.20f, instrumentType = "glock") // D6 shimmer
                // 23000ms - 24000ms has natural quiet decay before looping back seamlessly

                // Convert float buffer to 16-bit PCM
                val pcmData = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val rawSample = (mixBuffer[i] * 18000.0f).toInt()
                    pcmData[i] = rawSample.coerceIn(-32768, 32767).toShort()
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
                track.setLoopPoints(0, pcmData.size, -1) // Seamless infinite loop
                track.setVolume(0.24f)

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
