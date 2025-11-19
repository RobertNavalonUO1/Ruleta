package com.api.ruletaeuropea

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.api.ruletaeuropea.R
import android.util.Log
import android.net.Uri




class MusicService : Service() {

    private lateinit var player: MediaPlayer

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

            "PLAY" -> {
                if (!player.isPlaying) {
                    player.start()
                    Log.d("MusicService", "Música iniciada")
                }
            }

            "STOP" -> {
                if (player.isPlaying) {
                    player.pause()
                    Log.d("MusicService", "Música pausada")
                }
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
        }

        return START_NOT_STICKY
    }


    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}