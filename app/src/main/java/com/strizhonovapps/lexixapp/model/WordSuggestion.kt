package com.strizhonovapps.lexixapp.model

import com.orm.SugarRecord
import com.orm.dsl.Column
import com.orm.dsl.Table

@Table(name = "WORD_SUGGESTION_TABLE")
data class WordSuggestion(
    @Column(name = "ID")
    var id: Long = 0,
    @Column(name = "NAME")
    var name: String = "", // sugar placeholder
    @Column(name = "TIMES_SHOWN")
    var timesShown: Int = 0
) : SugarRecord()