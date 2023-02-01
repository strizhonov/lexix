package com.strizhonovapps.lexixapp.dao

import com.strizhonovapps.lexixapp.model.Word
import com.strizhonovapps.lexixapp.view.wordlist.WordsBatch
import java.time.LocalDateTime

interface WordDao {
    fun save(entity: Word): Long
    fun get(id: Long): Word?
    fun merge(entity: Word): Word
    fun delete(id: Long)
    fun count(filter: WordDaoFilter? = null): Long
    fun findAll(
        filter: WordDaoFilter? = null,
        batch: WordsBatch? = null,
        orderByIdDesc: Boolean? = null
    ): List<Word>

    fun erase(): Int
    fun resetProgress(ids: List<Long>)
    fun findAllTargetDatesForNonArchived(): List<LocalDateTime>
    fun findAllNames(): List<String>
}
