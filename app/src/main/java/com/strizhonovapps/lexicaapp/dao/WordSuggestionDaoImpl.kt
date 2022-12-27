package com.strizhonovapps.lexicaapp.dao

import com.orm.SugarRecord
import com.strizhonovapps.lexicaapp.model.WordSuggestion
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordSuggestionDaoImpl @Inject constructor() : WordSuggestionDao {

    override fun saveAll(entities: List<WordSuggestion>) {
        SugarRecord.saveInTx(entities)
    }

    override fun merge(entity: WordSuggestion): Long {
        val found = this.get(entity.id) ?: return entity.save()
        map(found, entity)
        return found.save()
    }

    override fun findAll(): List<WordSuggestion> = SugarRecord.listAll(WordSuggestion::class.java)

    override fun isEmpty(): Boolean =
        SugarRecord.count<WordSuggestion>(WordSuggestion::class.java) == 0L

    override fun findAllLimited(limit: Int): List<WordSuggestion> =
        SugarRecord.find(
            WordSuggestion::class.java,
            null,
            arrayOf(),
            null,
            null,
            "$limit"
        )

    private fun get(id: Long): WordSuggestion? =
        SugarRecord.findById(WordSuggestion::class.java, id)

    private fun map(found: WordSuggestion, entity: WordSuggestion) {
        found.wordSuggestionStatus = entity.wordSuggestionStatus
        found.name = entity.name
    }

}
