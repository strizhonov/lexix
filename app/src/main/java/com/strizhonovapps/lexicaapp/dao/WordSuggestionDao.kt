package com.strizhonovapps.lexicaapp.dao

import com.strizhonovapps.lexicaapp.model.WordSuggestion

interface WordSuggestionDao {
    fun saveAll(entities: List<WordSuggestion>)
    fun merge(entity: WordSuggestion): Long
    fun findAll(): List<WordSuggestion>
    fun isEmpty(): Boolean
    fun findAllLimited(limit: Int): List<WordSuggestion>
}