package com.strizhonovapps.lexixapp.model

data class WordMetaData(
    var id: Long,
    var picUrl: String? = null,
    var audioUrl: String? = null,
    var transcription: String? = null
)