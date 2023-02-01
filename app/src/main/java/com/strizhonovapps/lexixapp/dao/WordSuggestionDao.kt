package com.strizhonovapps.lexixapp.dao

import com.strizhonovapps.lexixapp.model.WordSuggestion

interface WordSuggestionDao {
    fun setTimesShown(id: Long, timesShown: Int)
    fun saveAll(entities: List<WordSuggestion>)
    fun findRandomWithMinTimesShown(): WordSuggestion?
    fun isEmpty(): Boolean
}