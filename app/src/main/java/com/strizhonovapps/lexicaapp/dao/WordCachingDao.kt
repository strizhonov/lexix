package com.strizhonovapps.lexicaapp.dao

import com.strizhonovapps.lexicaapp.model.Word
import java.util.*

class WordCachingDao(private val wordDao: WordDao) : WordDao {

    private val wordCache: MutableMap<Long, Word> = TreeMap()

    private var cacheIsDeprecated: Boolean = false

    init {
        updateCache()
    }

    override fun save(entity: Word): Long {
        cacheIsDeprecated = true
        return wordDao.save(entity)
    }

    override fun get(id: Long): Word? {
        if (cacheIsDeprecated) updateCache()
        return wordCache[id]
    }

    override fun merge(entity: Word): Long {
        cacheIsDeprecated = true
        return wordDao.merge(entity)
    }

    override fun delete(id: Long) {
        cacheIsDeprecated = true
        wordDao.delete(id)
    }

    override fun findAll(): List<Word> {
        if (cacheIsDeprecated) updateCache()
        return wordCache.values.toList()
    }

    override fun erase(): Int {
        cacheIsDeprecated = true
        return wordDao.erase()
    }

    override fun size(): Long {
        if (cacheIsDeprecated) updateCache()
        return wordCache.size.toLong()
    }

    private fun updateCache() {
        val newCache = wordDao.findAll().associateBy(Word::id)
        wordCache.clear()
        wordCache.putAll(newCache)
        cacheIsDeprecated = false
    }
}