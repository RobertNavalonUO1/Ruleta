package com.api.ruletaeuropea

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted

    private val mediaPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.musicafondo1).apply {
        isLooping = true
        start()
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.start()
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer.release()
    }
}
