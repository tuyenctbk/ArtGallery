package com.example.data

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.data.model.SoundscapeType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class AudioSoundscapeManager {

    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playSoundscape(type: SoundscapeType) {
        stop()
        if (type == SoundscapeType.MUTE) return

        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast( sampleRate * 2 )

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
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
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()

            synthJob = scope.launch {
                val buffer = ShortArray(1024)
                var phase = 0.0
                var randomSeed = 123456789L

                while (isActive) {
                    for (i in buffer.indices) {
                        val sample: Float = when (type) {
                            SoundscapeType.MUSEUM_HALL -> {
                                // Subtle ambient drone + room reverberation
                                phase += 2.0 * Math.PI * 110.0 / sampleRate // A2 note
                                val sine1 = sin(phase) * 0.08
                                val sine2 = sin(phase * 1.5) * 0.04
                                (sine1 + sine2).toFloat()
                            }
                            SoundscapeType.RAIN_SKYLIGHT -> {
                                // Filtered soft white noise (rain simulation)
                                randomSeed = (randomSeed * 1103515245 + 12345) and 0x7fffffffL
                                val noise = (randomSeed.toFloat() / 0x7fffffffL) * 2.0f - 1.0f
                                noise * 0.06f
                            }
                            SoundscapeType.GENTLE_PIANO -> {
                                // Warm pentatonic chime sweep
                                phase += 2.0 * Math.PI * 220.0 / sampleRate
                                val mod = (sin(phase * 0.05) + 1.0) * 0.5
                                (sin(phase) * 0.07 * mod).toFloat()
                            }
                            SoundscapeType.HEARTH_FIRE -> {
                                // Low rumble + crackle impulse
                                randomSeed = (randomSeed * 1103515245 + 12345) and 0x7fffffffL
                                val noise = (randomSeed.toFloat() / 0x7fffffffL) * 2.0f - 1.0f
                                val crackle = if ((randomSeed % 400) == 0L) 0.3f else 0.0f
                                (noise * 0.03f + crackle * 0.1f)
                            }
                            SoundscapeType.OCEAN_BREEZE -> {
                                // Swelling wave noise
                                phase += 2.0 * Math.PI * 0.2 / sampleRate
                                val waveMod = ((sin(phase) + 1.0) / 2.0).toFloat()
                                randomSeed = (randomSeed * 1103515245 + 12345) and 0x7fffffffL
                                val noise = (randomSeed.toFloat() / 0x7fffffffL) * 2.0f - 1.0f
                                noise * 0.05f * waveMod
                            }
                            SoundscapeType.MUTE -> 0.0f
                        }

                        val shortSample = (sample.coerceIn(-1.0f, 1.0f) * 32767).toInt().toShort()
                        buffer[i] = shortSample
                    }
                    audioTrack?.write(buffer, 0, buffer.size)
                    delay(5)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // ignore
        }
        audioTrack = null
    }
}
