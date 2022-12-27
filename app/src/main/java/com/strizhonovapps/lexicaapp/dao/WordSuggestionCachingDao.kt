package com.strizhonovapps.lexicaapp.dao

import com.strizhonovapps.lexicaapp.model.WordSuggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentSkipListMap

class WordSuggestionCachingDao(private val engineDao: WordSuggestionDao) : WordSuggestionDao {

    private val wordCache: MutableMap<Long, WordSuggestion> = ConcurrentSkipListMap()

    init {
        val hundred = findAllLimited(100)
        wordCache.putAll(hundred.associateBy(WordSuggestion::id))
        CoroutineScope(Dispatchers.Default).launch {
            wordCache.putAll(engineDao.findAll().associateBy(WordSuggestion::id))
        }
    }

    override fun saveAll(entities: List<WordSuggestion>) = engineDao.saveAll(entities)

    override fun merge(entity: WordSuggestion): Long = engineDao.merge(entity)

    override fun findAll(): List<WordSuggestion> = wordCache.values.toList()

    override fun isEmpty(): Boolean = engineDao.isEmpty()

    override fun findAllLimited(limit: Int): List<WordSuggestion> = engineDao.findAllLimited(limit)

}