package com.strizhonovapps.lexixapp.util

import android.media.MediaPlayer

class UrlAudioPlayer(private val mediaPlayer: MediaPlayer = MediaPlayer()) {

    fun play(url: String?) {
        if (url.isNullOrBlank().not()) {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepare()
            mediaPlayer.start()
        }
    }

}