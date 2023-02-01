package com.strizhonovapps.lexixapp.service

import com.strizhonovapps.lexixapp.model.Word
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WordsTokenizer @Inject constructor() {

    private val defaultWordSeparator = "-"

    fun tokenize(words: List<Word>): String {
        return words.joinToString(separator = System.lineSeparator()) {
            "${it.name} $defaultWordSeparator " +
                    "${it.translation} $defaultWordSeparator " +
                    "${it.level} $defaultWordSeparator " +
                    "${it.tag}"
        }
    }

}