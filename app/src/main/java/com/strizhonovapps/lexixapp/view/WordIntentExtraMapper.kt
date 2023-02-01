package com.strizhonovapps.lexixapp.view

import android.content.Intent
import com.strizhonovapps.lexixapp.model.AllowedWordCardSide
import com.strizhonovapps.lexixapp.model.INITIAL_WORD_LEVEL
import com.strizhonovapps.lexixapp.model.Word
import com.strizhonovapps.lexixapp.util.toDate
import com.strizhonovapps.lexixapp.util.toTimestamp


private const val NAME_KEY = "name"
private const val TAG_KEY = "tag"
private const val TRANSLATION_KEY = "translation"
private const val LVL_KEY = "lvl"
private const val ID_KEY = "id"
private const val IS_ARCHIVED_KEY = "is_archived"
private const val TIMES_SHOWN_KEY = "times_shown"
private const val TARGET_DATE_TIME_KEY = "target_date_time"
private const val MODIFICATION_DATE_TIME_KEY = "modification_date_time"
private const val WORD_CARD_SIDE_KEY = "word_card_side"

class WordIntentExtraMapper(private val intent: Intent) {

    fun setWord(word: Word) {
        intent
            .putExtra(ID_KEY, word.id)
            .putExtra(NAME_KEY, word.name)
            .putExtra(TRANSLATION_KEY, word.translation)
            .putExtra(MODIFICATION_DATE_TIME_KEY, toTimestamp(word.modificationDate))
            .putExtra(TARGET_DATE_TIME_KEY, toTimestamp(word.targetDate))
            .putExtra(LVL_KEY, word.level)
            .putExtra(TAG_KEY, word.tag)
            .putExtra(IS_ARCHIVED_KEY, word.isArchived)
            .putExtra(TIMES_SHOWN_KEY, word.timesShown)
            .putExtra(WORD_CARD_SIDE_KEY, word.allowedWordCardSide)
    }

    fun getWord(): Word {
        return Word(
            id = intent.getLongExtra(ID_KEY, -1L),
            name = intent.getStringExtra(NAME_KEY),
            translation = intent.getStringExtra(TRANSLATION_KEY),
            allowedWordCardSide = intent.getSerializableExtra(WORD_CARD_SIDE_KEY) as? AllowedWordCardSide
                ?: AllowedWordCardSide.ALL,
            tag = intent.getStringExtra(TAG_KEY),
            timesShown = intent.getLongExtra(TIMES_SHOWN_KEY, 0L),
            isArchived = intent.getBooleanExtra(IS_ARCHIVED_KEY, false),
            level = intent.getIntExtra(LVL_KEY, INITIAL_WORD_LEVEL),
            targetDate = toDate(
                intent.getLongExtra(
                    TARGET_DATE_TIME_KEY,
                    System.currentTimeMillis()
                )
            ),
            modificationDate = toDate(
                intent.getLongExtra(
                    MODIFICATION_DATE_TIME_KEY,
                    System.currentTimeMillis()
                )
            )
        )

    }

}