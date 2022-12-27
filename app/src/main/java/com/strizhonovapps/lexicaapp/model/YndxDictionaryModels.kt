package com.strizhonovapps.lexicaapp.model

data class YndxDictionaryResponse(val def: List<YndxDefinition>)

data class YndxDefinition(var tr: List<YndxTranslation> = emptyList())

data class YndxTranslation(
    var text: String?,
    var ex: List<YndxExample> = emptyList()
)

data class YndxExample(var text: String?)
