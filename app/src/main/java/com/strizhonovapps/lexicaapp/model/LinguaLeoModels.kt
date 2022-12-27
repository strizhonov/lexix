package com.strizhonovapps.lexicaapp.model

data class LeoWord(
    val sound_url: String,
    val translate: List<LeoTranslation>,
    val transcription: String
)

data class LeoTranslation(
    val value: String,
    val pic_url: String,
    val votes: Int
)

data class LeoWordData(
    val soundUrl: String?,
    val transcription: String?,
    val picUrl: String?
)

data class LeoRequestData(
    val text: String,
    val langPair: LeoLangPair
)

data class LeoLangPair(
    val source: String,
    val target: String
)

data class LeoWordRequest(
    val apiVersion: String = "1.0.1",
    val port: Int = 1002,
    val data: LeoRequestData
) {

    constructor(word: String, source: String, target: String) : this(
        "1.0.1",
        1001,
        LeoRequestData(
            word,
            LeoLangPair(source, target)
        )
    )

}
