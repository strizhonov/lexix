package com.strizhonovapps.lexicaapp.dao

import com.orm.SugarRecord
import com.strizhonovapps.lexicaapp.model.Word
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordDaoImpl @Inject constructor() : WordDao {

    override fun save(entity: Word) = entity.save()

    override fun get(id: Long): Word? = SugarRecord.findById(Word::class.java, id)

    override fun merge(entity: Word): Long {
        val found = this.get(entity.id) ?: return entity.save()
        map(found, entity)
        return found.save()
    }

    override fun delete(id: Long) {
        SugarRecord.deleteAll(Word::class.java, "ID = $id ")
    }

    override fun findAll(): List<Word> = SugarRecord.listAll(Word::class.java)

    override fun erase() = SugarRecord.deleteAll(Word::class.java)

    override fun size() = SugarRecord.count<Word>(Word::class.java)

    private fun map(found: Word, entity: Word) {
        found.level = entity.level
        found.name = entity.name
        found.translation = entity.translation
        found.targetDate = entity.targetDate
        found.modificationDate = entity.modificationDate
        found.isArchived = entity.isArchived
        found.timesShown = entity.timesShown
        found.tag = entity.tag
        found.audioUrl = entity.audioUrl
        found.transcription = entity.transcription
    }

}
