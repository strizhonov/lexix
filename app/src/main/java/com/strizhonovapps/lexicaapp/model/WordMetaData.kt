package com.strizhonovapps.lexicaapp.model

import android.graphics.Bitmap

data class WordMetaData(
    var image: Bitmap? = null,
    var audioUrl: String? = null,
    var transcription: String? = null
)