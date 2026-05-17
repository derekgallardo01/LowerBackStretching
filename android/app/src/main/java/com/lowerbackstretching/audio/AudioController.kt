package com.lowerbackstretching.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.util.Log

/**
 * Wraps platform audio APIs for the three streams the app cares about:
 *
 * - **Music loop** — long, low-volume background music. One [MediaPlayer].
 * - **Ambient loop** — long nature soundscape. One [MediaPlayer].
 * - **Chimes** — short one-shot bell/ding. One [SoundPool].
 *
 * Calls are idempotent: setting a track that's already playing is a
 * no-op; setting a different track stops the previous and starts the
 * new one. Missing resources fail silently (logged at DEBUG) so the
 * app still works in dev when the `res/raw/` MP3s haven't been added.
 *
 * Music ducking: when a chime fires it requests
 * `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`. Other apps that respect audio
 * focus (Spotify, Apple Music) lower their volume for the duration.
 */
object AudioController {

    private const val TAG = "AudioController"

    private var musicPlayer: MediaPlayer? = null
    private var ambientPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private val chimeSoundIds = mutableMapOf<String, Int>()

    private var currentMusic: MusicTrack = MusicTrack.NONE
    private var currentAmbient: AmbientTrack = AmbientTrack.NONE

    // ---------- Music ----------

    fun setMusic(context: Context, track: MusicTrack, volume: Float) {
        if (track == currentMusic) {
            musicPlayer?.setVolumeSafely(volume)
            return
        }
        musicPlayer?.releaseSafely()
        musicPlayer = null
        currentMusic = track

        val res = track.resName?.let { resIdOrNull(context, it) } ?: return
        musicPlayer = MediaPlayer.create(context, res)?.apply {
            isLooping = true
            setVolumeSafely(volume)
            start()
        }
    }

    fun setMusicVolume(volume: Float) {
        musicPlayer?.setVolumeSafely(volume)
    }

    // ---------- Ambient ----------

    fun setAmbient(context: Context, track: AmbientTrack, volume: Float) {
        if (track == currentAmbient) {
            ambientPlayer?.setVolumeSafely(volume)
            return
        }
        ambientPlayer?.releaseSafely()
        ambientPlayer = null
        currentAmbient = track

        val res = track.resName?.let { resIdOrNull(context, it) } ?: return
        ambientPlayer = MediaPlayer.create(context, res)?.apply {
            isLooping = true
            setVolumeSafely(volume)
            start()
        }
    }

    fun setAmbientVolume(volume: Float) {
        ambientPlayer?.setVolumeSafely(volume)
    }

    // ---------- Chimes ----------

    fun playChime(context: Context, track: ChimeTrack) {
        val resName = track.resName ?: return
        val pool = ensureSoundPool()
        val soundId = chimeSoundIds.getOrPut(resName) {
            resIdOrNull(context, resName)?.let { pool.load(context, it, 1) } ?: -1
        }
        if (soundId == -1) return

        // Briefly request ducking focus around the chime.
        requestDuckingFocus(context)
        pool.play(soundId, 1f, 1f, /* priority */ 1, /* loop */ 0, /* rate */ 1f)
        // Auto-release focus 500ms later (chimes are very short).
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
            { releaseDuckingFocus(context) }, 500
        )
    }

    // ---------- Lifecycle ----------

    /** Tear down everything. Call when the player screen disposes. */
    fun stopAll() {
        musicPlayer?.releaseSafely(); musicPlayer = null; currentMusic = MusicTrack.NONE
        ambientPlayer?.releaseSafely(); ambientPlayer = null; currentAmbient = AmbientTrack.NONE
        soundPool?.release(); soundPool = null
        chimeSoundIds.clear()
    }

    // ---------- Internals ----------

    private fun ensureSoundPool(): SoundPool {
        soundPool?.let { return it }
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        return SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attrs)
            .build()
            .also { soundPool = it }
    }

    private var focusRequest: AudioFocusRequest? = null

    private fun requestDuckingFocus(context: Context) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener { /* no-op */ }
                .build()
            am.requestAudioFocus(req)
            focusRequest = req
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
            )
        }
    }

    private fun releaseDuckingFocus(context: Context) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { am.abandonAudioFocusRequest(it) }
            focusRequest = null
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(null)
        }
    }

    private fun resIdOrNull(context: Context, resName: String): Int? {
        val id = context.resources.getIdentifier(resName, "raw", context.packageName)
        return if (id != 0) id else {
            Log.d(TAG, "Audio resource not found: res/raw/$resName")
            null
        }
    }

    private fun MediaPlayer.setVolumeSafely(volume: Float) {
        val v = volume.coerceIn(0f, 1f)
        try {
            setVolume(v, v)
        } catch (t: Throwable) {
            Log.d(TAG, "setVolume failed: ${t.message}")
        }
    }

    private fun MediaPlayer.releaseSafely() {
        try {
            if (isPlaying) stop()
        } catch (_: Throwable) { }
        try {
            release()
        } catch (_: Throwable) { }
    }
}
