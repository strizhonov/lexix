package com.strizhonovapps.lexicaapp.model

import com.orm.SugarRecord
import java.util.*

const val INITIAL_WORD_LEVEL = 1
const val HARD_LEVEL_COEF = 10

data class Word(
    var id: Long = 0,
    var name: String? = null,
    var translation: String? = null,
    var level: Int = INITIAL_WORD_LEVEL,
    var modificationDate: Date = Date(),
    var isArchived: Boolean = false,
    var tag: String? = null,
    var timesShown: Long = 0L,
    var audioUrl: String? = null,
    var transcription: String? = null,
) : SugarRecord() {

    constructor(
        id: Long = 0,
        name: String? = null,
        translation: String? = null,
        level: Int = INITIAL_WORD_LEVEL,
        targetDate: Date,
        modificationDate: Date = Date(),
        isArchived: Boolean = false,
        tag: String? = null,
        timesShown: Long = 0L,
        audioUrl: String? = null,
        transcription: String? = null
    ) : this(
        id,
        name,
        translation,
        level,
        modificationDate,
        isArchived,
        tag,
        timesShown,
        audioUrl,
        transcription
    ) {
        this.targetDate = targetDate
    }

    lateinit var targetDate: Date

    fun isHard() =
        if (level == 0)
            timesShown > 5
        else
            timesShown.div(level.toDouble()) >= HARD_LEVEL_COEF

}
