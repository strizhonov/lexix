package com.strizhonovapps.lexicaapp.service

import com.strizhonovapps.lexicaapp.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LevelColorDefiner @Inject constructor() {

    fun defineColor(level: Int): Int {
        return when (level) {
            in Int.MIN_VALUE..-1 -> throw IllegalArgumentException("Illegal value for color")
            in 0..1 -> R.color.word_level__lowest
            in 2..4 -> R.color.word_level__middle
            in 5..Int.MAX_VALUE -> R.color.word_level__advanced
            else -> R.color.word_level__lowest
        }
    }

    fun defineBackground(level: Int): Int {
        return when (level) {
            in Int.MIN_VALUE..-1 -> throw IllegalArgumentException("Illegal value for background")
            in 0..1 -> R.drawable.border__word_level__lowest
            in 2..4 -> R.drawable.border__word_level__middle
            in 5..Int.MAX_VALUE -> R.drawable.border__word_level__advanced
            else -> R.drawable.border__word_level__lowest
        }
    }

}