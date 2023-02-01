package com.strizhonovapps.lexixapp.model

import com.orm.SugarRecord
import com.orm.dsl.Column
import com.orm.dsl.Table
import com.strizhonovapps.lexixapp.util.toDate
import com.strizhonovapps.lexixapp.util.toLocalDateTime
import java.time.LocalDateTime
import java.util.Date

const val INITIAL_WORD_LEVEL = 1
const val HARD_LEVEL_COEF = 7

const val WORD_TABLE = "WORD"

const val WORD_ID_FIELD = "ID"
const val WORD_NAME_FIELD = "NAME"
const val WORD_TRANSLATION_FIELD = "TRANSLATION"
const val WORD_LEVEL_FIELD = "LEVEL"
const val WORD_MODIFICATION_DATE_FIELD = "MODIFICATION_DATE"
const val WORD_TARGET_DATE_FIELD = "TARGET_DATE"
const val WORD_IS_ARCHIVED_FIELD = "IS_ARCHIVED"
const val WORD_TAG_FIELD = "TAG"
const val WORD_TIMES_SHOWN_FIELD = "TIMES_SHOWN"
const val WORD_AUDIO_URL_FIELD = "AUDIO_URL"
const val WORD_TRANSCRIPTION_FIELD = "TRANSCRIPTION"
const val WORD_SIDES_TO_SHOW_FIELD = "SIDES_TO_SHOW"

@Table(name = WORD_TABLE)
data class Word(
    @Column(name = WORD_ID_FIELD)
    var id: Long = 0,
    @Column(name = WORD_NAME_FIELD)
    var name: String? = null,
    @Column(name = WORD_TRANSLATION_FIELD)
    var translation: String? = null,
    @Column(name = WORD_LEVEL_FIELD)
    var level: Int = INITIAL_WORD_LEVEL,
    @Column(name = WORD_MODIFICATION_DATE_FIELD)
    var modificationDate: Date = Date(),
    @Column(name = WORD_TARGET_DATE_FIELD)
    var targetDate: Date = Date(),
    @Column(name = WORD_IS_ARCHIVED_FIELD)
    var isArchived: Boolean = false,
    @Column(name = WORD_TAG_FIELD)
    var tag: String? = null,
    @Column(name = WORD_TIMES_SHOWN_FIELD)
    var timesShown: Long = 0L,
    @Column(name = WORD_AUDIO_URL_FIELD)
    var audioUrl: String? = null,
    @Column(name = WORD_TRANSCRIPTION_FIELD)
    var transcription: String? = null,
    @Column(name = WORD_SIDES_TO_SHOW_FIELD)
    var allowedWordCardSide: AllowedWordCardSide = AllowedWordCardSide.ALL
) : SugarRecord() {

    fun getTargetDate(): LocalDateTime {
        return toLocalDateTime(targetDate)
    }

    fun setTargetDate(targetDate: LocalDateTime) {
        this.targetDate = toDate(targetDate)
    }

    fun setModificationDate(modificationDate: LocalDateTime) {
        this.targetDate = toDate(modificationDate)
    }

    fun same(toCompare: Word?): Boolean =
        toCompare != null &&
                this.name == toCompare.name
                && this.translation == toCompare.translation
                && this.tag == toCompare.tag
}
