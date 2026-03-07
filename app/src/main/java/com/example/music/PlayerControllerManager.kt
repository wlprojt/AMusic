package com.example.music

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors

class PlayerControllerManager(
    private val context: Context
) {
    private var controller: MediaController? = null

    fun connect(
        onConnected: (MediaController) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )

        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener({
            try {
                val mediaController = future.get()
                controller = mediaController
                onConnected(mediaController)
            } catch (e: Exception) {
                onError(e)
            }
        }, MoreExecutors.directExecutor())
    }

    fun release() {
        controller?.release()
        controller = null
    }
}