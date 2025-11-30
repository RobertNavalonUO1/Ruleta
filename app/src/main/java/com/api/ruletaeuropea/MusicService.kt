package com.api.ruletaeuropea

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import com.api.ruletaeuropea.R
import android.net.Uri

<<<<<<< HEAD
=======




>>>>>>> Andrea2
class MusicService : Service() {

    private lateinit var player: MediaPlayer
    private var isMuted = false
<<<<<<< HEAD
    private var wasPlayingBeforeBackground = false
=======
>>>>>>> Andrea2

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer.create(this, R.raw.musicafondo1)
        player.isLooping = true
        player.setVolume(1.0f, 1.0f)
        Log.d("MusicService", "MediaPlayer creado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getStringExtra("action")
        Log.d("MusicService", "onStartCommand con action=$action")

        when (action) {

<<<<<<< HEAD
            "PLAY", "RESUME" -> {
=======
            "PLAY" -> {
>>>>>>> Andrea2
                try {
                    if (!player.isPlaying) {
                        player.start()
                    }
                } catch (e: IllegalStateException) {
                    // Si ocurre, reintenta correctamente:
                    player.reset()
                    player = MediaPlayer.create(this, R.raw.musicafondo1)
                    player.isLooping = true
                    player.start()
                }
            }

<<<<<<< HEAD
            "STOP", "PAUSE" -> {
=======
            "STOP" -> {
>>>>>>> Andrea2
                if (player.isPlaying) {
                    player.pause()
                    Log.d("MusicService", "Música pausada")
                }
            }

            "TOGGLE_MUTE" -> {
                toggleMute()
            }

            "SET_MUSIC" -> {
                val uriString = intent.getStringExtra("audioUri")
                if (uriString != null) {
                    try {
                        player.reset()
                        player.setDataSource(this, Uri.parse(uriString))
                        player.prepare()
                        player.isLooping = true
                        player.start()
                        Log.d("MusicService", "Reproduciendo nueva música desde archivo externo")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("MusicService", "Error cargando el archivo de audio: ${e.message}")
                    }
                }
            }
<<<<<<< HEAD

            "PAUSE_BG" -> {
                if (this::player.isInitialized && player.isPlaying) {
                    wasPlayingBeforeBackground = true
                    player.pause()
                } else {
                    wasPlayingBeforeBackground = false
                }
            }

            "RESUME_BG" -> {
                if (wasPlayingBeforeBackground && this::player.isInitialized && !player.isPlaying) {
                    try {
                        player.start()
                    } catch (e: Exception) {
                        Log.e("MusicService", "Error reanudando: ${e.message}")
                    }
                }
                wasPlayingBeforeBackground = false
            }
=======
>>>>>>> Andrea2
        }

        return START_STICKY
    }

    private fun toggleMute() {
        isMuted = !isMuted
        if (isMuted) {
            player.setVolume(0f, 0f)
        } else {
            player.setVolume(1f, 1f)
        }
    }

    override fun onDestroy() {
        if (this::player.isInitialized) player.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}