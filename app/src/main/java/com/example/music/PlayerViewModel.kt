package com.example.music

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PlayerUiState(
    val songs: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application.applicationContext)
    private val controllerManager = PlayerControllerManager(application.applicationContext)

    private var controller: MediaController? = null
    private var pendingMediaItems: List<MediaItem>? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState
    private var pendingPlayIndex: Int? = null

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _uiState.value = _uiState.value.copy(
                currentIndex = controller?.currentMediaItemIndex ?: -1
            )
        }
    }

    init {
        controllerManager.connect(
            onConnected = { mediaController ->
                controller = mediaController
                mediaController.addListener(listener)

                pendingMediaItems?.let { items ->
                    mediaController.setMediaItems(items)
                    mediaController.prepare()
                    pendingMediaItems = null
                }

                pendingPlayIndex?.let { index ->
                    if (index in 0 until mediaController.mediaItemCount) {
                        mediaController.seekTo(index, 0L)
                        mediaController.play()
                    }
                    pendingPlayIndex = null
                }

                syncState()

                viewModelScope.launch {
                    while (true) {
                        syncState()
                        kotlinx.coroutines.delay(500)
                    }
                }
            },
            onError = {
                it.printStackTrace()
            }
        )
    }

    fun loadSongs() {
        if (_uiState.value.songs.isNotEmpty()) return

        viewModelScope.launch {
            try {
                val songs = kotlinx.coroutines.withContext(Dispatchers.IO) {
                    repository.getAllSongs()
                }

                val mediaItems = songs.map { song ->
                    MediaItem.Builder()
                        .setUri(song.uri)
                        .setMediaId(song.id.toString())
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.title)
                                .setArtist(song.artist)
                                .build()
                        )
                        .build()
                }

                _uiState.value = _uiState.value.copy(
                    songs = songs,
                    currentIndex = if (songs.isNotEmpty()) 0 else -1
                )

                val mediaController = controller
                if (mediaController != null) {
                    mediaController.setMediaItems(mediaItems)
                    mediaController.prepare()

                    pendingPlayIndex?.let { index ->
                        if (index in songs.indices) {
                            mediaController.seekTo(index, 0L)
                            mediaController.play()
                        }
                        pendingPlayIndex = null
                    }
                } else {
                    pendingMediaItems = mediaItems
                }

                syncState()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playSong(song: Song) {
        val index = _uiState.value.songs.indexOfFirst { it.id == song.id }
        if (index == -1) return

        val mediaController = controller

        if (mediaController != null) {
            if (mediaController.mediaItemCount == 0) {
                pendingMediaItems?.let { items ->
                    mediaController.setMediaItems(items)
                    mediaController.prepare()
                    pendingMediaItems = null
                }
            }

            if (mediaController.mediaItemCount > 0) {
                mediaController.seekTo(index, 0L)
                mediaController.play()
            } else {
                pendingPlayIndex = index
            }
        } else {
            pendingPlayIndex = index
        }

        _uiState.value = _uiState.value.copy(currentIndex = index)
        syncState()
    }

    fun togglePlayPause() {
        controller?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
        syncState()
    }

    fun next() {
        controller?.let {
            if (it.hasNextMediaItem()) {
                it.seekToNextMediaItem()
            } else if (it.mediaItemCount > 0) {
                it.seekTo(0, 0)
            }
            it.play()
        }
        syncState()
    }

    fun previous() {
        controller?.let {
            if (it.hasPreviousMediaItem()) {
                it.seekToPreviousMediaItem()
            } else if (it.mediaItemCount > 0) {
                it.seekTo(it.mediaItemCount - 1, 0)
            }
            it.play()
        }
        syncState()
    }

    private fun syncState() {
        val c = controller

        _uiState.value = _uiState.value.copy(
            currentIndex = c?.currentMediaItemIndex ?: _uiState.value.currentIndex,
            isPlaying = c?.isPlaying ?: false,
            currentPosition = c?.currentPosition ?: 0L,
            duration = if ((c?.duration ?: 0L) > 0) c?.duration ?: 0L else 0L
        )
    }

    fun seekTo(position: Long) {
        controller?.seekTo(position)
    }

    override fun onCleared() {
        controller?.removeListener(listener)
        controllerManager.release()
        super.onCleared()
    }
}