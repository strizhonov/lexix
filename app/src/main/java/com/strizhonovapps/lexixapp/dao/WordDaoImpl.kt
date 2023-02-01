package com.strizhonovapps.lexixapp.dao

import com.orm.SugarRecord
import com.strizhonovapps.lexixapp.model.HARD_LEVEL_COEF
import com.strizhonovapps.lexixapp.model.INITIAL_WORD_LEVEL
import com.strizhonovapps.lexixapp.model.WORD_ID_FIELD
import com.strizhonovapps.lexixapp.model.WORD_IS_ARCHIVED_FIELD
import com.strizhonovapps.lexixapp.model.WORD_LEVEL_FIELD
import com.strizhonovapps.lexixapp.model.WORD_MODIFICATION_DATE_FIELD
import com.strizhonovapps.lexixapp.model.WORD_NAME_FIELD
import com.strizhonovapps.lexixapp.model.WORD_TABLE
import com.strizhonovapps.lexixapp.model.WORD_TARGET_DATE_FIELD
import com.strizhonovapps.lexixapp.model.WORD_TIMES_SHOWN_FIELD
import com.strizhonovapps.lexixapp.model.Word
import com.strizhonovapps.lexixapp.util.now
import com.strizhonovapps.lexixapp.view.wordlist.WordsBatch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordDaoImpl @Inject constructor() : WordDao {

    override fun save(entity: Word) = entity.save()

    override fun get(id: Long): Word? = SugarRecord.findById(Word::class.java, id)

    override fun merge(entity: Word): Word {
        val found = this.get(entity.id)
        if (found == null) {
            val id = entity.save()
            entity.id = id
            return entity
        }
        map(found, entity)
        val id = found.save()
        found.id = id
        return found
    }

    override fun delete(id: Long) {
        SugarRecord.deleteAll(Word::class.java, " $WORD_ID_FIELD = $id ")
    }

    override fun count(filter: WordDaoFilter?): Long =
        SugarRecord.count<Word>(Word::class.java, getFilterString(filter), null)

    override fun findAll(
        filter: WordDaoFilter?,
        batch: WordsBatch?,
        orderByIdDesc: Boolean?
    ): List<Word> {
        val filterString = getFilterString(filter)
        val whereClause = if (filterString.isBlank()) "" else " WHERE $filterString "
        val offsetString = batch?.let { " LIMIT ${it.pageSize} OFFSET ${it.offset} " } ?: ""
        val orderString = if (orderByIdDesc == true) " ORDER BY $WORD_ID_FIELD DESC " else ""
        val query = " SELECT * FROM $WORD_TABLE $whereClause $orderString $offsetString "
        return SugarRecord.findWithQuery(Word::class.java, query)
    }

    override fun erase() = SugarRecord.deleteAll(Word::class.java)

    override fun resetProgress(ids: List<Long>) {
        val now = now()
        SugarRecord.executeQuery(
            " UPDATE $WORD_TABLE " +
                    " SET $WORD_LEVEL_FIELD = $INITIAL_WORD_LEVEL, " +
                    " $WORD_TIMES_SHOWN_FIELD = 0, " +
                    " $WORD_MODIFICATION_DATE_FIELD = $now, " +
                    " $WORD_TARGET_DATE_FIELD = $now " +
                    " WHERE $WORD_ID_FIELD in (${ids.joinToString(",")}) "
        )
    }

    override fun findAllTargetDatesForNonArchived(): List<LocalDateTime> =
        SugarRecord.findWithQuery(
            LocalDateTime::class.java,
            " SELECT $WORD_TARGET_DATE_FIELD FROM $WORD_TABLE WHERE $WORD_IS_ARCHIVED_FIELD = false "
        )

    override fun findAllNames(): List<String> =
        SugarRecord.findWithQuery(String::class.java, " SELECT $WORD_NAME_FIELD FROM $WORD_TABLE ")


    private fun getFilterString(filter: WordDaoFilter?): String {
        return listOfNotNull(
            filter?.archived?.let { archived -> " $WORD_IS_ARCHIVED_FIELD = $archived " },
            filter?.hard?.let { isHard -> getIsHardFilterString(isHard) },
            filter?.targetDateBeforeNow?.let { " $WORD_TARGET_DATE_FIELD / 1000 <= 1 * strftime('%s', 'now') " },
        ).joinToString(" AND ")
    }

    private fun getIsHardFilterString(isHard: Boolean) =
        " ($WORD_TIMES_SHOWN_FIELD / (CASE WHEN $WORD_LEVEL_FIELD = 0 THEN 1 ELSE $WORD_LEVEL_FIELD END) " +
                "${if (isHard) " >=" else " < "} " +
                "$HARD_LEVEL_COEF) "

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
        found.allowedWordCardSide = entity.allowedWordCardSide
    }
}

