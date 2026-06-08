package com.mauricior8.enfoque.data.media

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.provider.Settings
import androidx.compose.runtime.mutableStateOf

/** Snapshot of the current media session. */
data class NowPlaying(
    val title: String,
    val artist: String,
    val album: String,
    val art: Bitmap?,
    val isPlaying: Boolean,
    val packageName: String,
)

/**
 * Reads the active media session (e.g. Samsung Music, Spotify, YouTube Music)
 * via [MediaSessionManager]. Needs notification-listener access, which the user
 * grants in system settings. Exposes simple transport controls.
 */
class NowPlayingController(private val context: Context) {

    val state = mutableStateOf<NowPlaying?>(null)

    private var sessionManager: MediaSessionManager? = null
    private var controller: MediaController? = null
    private val component = ComponentName(context, EnfoqueNotificationListener::class.java)

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = refreshFromController()
        override fun onPlaybackStateChanged(state: PlaybackState?) = refreshFromController()
        override fun onSessionDestroyed() {
            controller = null
            this@NowPlayingController.state.value = null
        }
    }

    private val sessionsListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            bindTo(controllers)
        }

    fun hasNotificationAccess(): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        return flat.split(":").any { it.contains(context.packageName) }
    }

    fun start() {
        if (!hasNotificationAccess()) return
        try {
            val manager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            sessionManager = manager
            manager.addOnActiveSessionsChangedListener(sessionsListener, component)
            bindTo(manager.getActiveSessions(component))
        } catch (e: Exception) {
            // Access not granted / not available.
        }
    }

    fun stop() {
        controller?.unregisterCallback(controllerCallback)
        controller = null
        runCatching { sessionManager?.removeOnActiveSessionsChangedListener(sessionsListener) }
        sessionManager = null
    }

    private fun bindTo(controllers: List<MediaController>?) {
        controller?.unregisterCallback(controllerCallback)
        val active = controllers?.firstOrNull()
        controller = active
        active?.registerCallback(controllerCallback)
        refreshFromController()
    }

    private fun refreshFromController() {
        val c = controller
        if (c == null) {
            state.value = null
            return
        }
        val md = c.metadata
        val ps = c.playbackState
        val title = md?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Reproduciendo"
        val artist = md?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: md?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST) ?: ""
        val album = md?.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""
        val art = md?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: md?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        val playing = ps?.state == PlaybackState.STATE_PLAYING
        state.value = NowPlaying(title, artist, album, art, playing, c.packageName)
    }

    fun playPause() {
        val c = controller ?: return
        if (c.playbackState?.state == PlaybackState.STATE_PLAYING) c.transportControls.pause()
        else c.transportControls.play()
    }

    fun next() = controller?.transportControls?.skipToNext() ?: Unit
    fun previous() = controller?.transportControls?.skipToPrevious() ?: Unit
}
