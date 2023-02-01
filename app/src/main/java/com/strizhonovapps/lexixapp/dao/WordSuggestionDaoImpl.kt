package com.strizhonovapps.lexixapp.dao

import com.orm.SugarRecord
import com.strizhonovapps.lexixapp.model.WordSuggestion
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordSuggestionDaoImpl @Inject constructor() : WordSuggestionDao {

    override fun setTimesShown(id: Long, timesShown: Int) {
        SugarRecord.executeQuery(
            "UPDATE WORD_SUGGESTION_TABLE SET TIMES_SHOWN = $timesShown WHERE ID = $id"
        )
    }

    override fun saveAll(entities: List<WordSuggestion>) {
        SugarRecord.saveInTx(entities)
    }

    override fun isEmpty(): Boolean =
        SugarRecord.count<WordSuggestion>(WordSuggestion::class.java) == 0L

    override fun findRandomWithMinTimesShown(): WordSuggestion? =
        SugarRecord.findWithQuery(
            WordSuggestion::class.java,
            "SELECT * FROM WORD_SUGGESTION_TABLE " +
                    "WHERE TIMES_SHOWN = (SELECT MIN(TIMES_SHOWN) FROM WORD_SUGGESTION_TABLE) " +
                    "ORDER BY RANDOM() " +
                    "LIMIT 1"
        ).getOrNull(0)


}

