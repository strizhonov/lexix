package com.strizhonovapps.lexicaapp.model

import com.strizhonovapps.lexicaapp.R


enum class SupportedLanguage(
    private val descriptionRes: Int,
    val yndxLangKey: String,
    val leoLangKey: String,
    private val icon: Int,
) : IconedAndNamed {
    DE(R.string.lang_de, "de", "de", R.drawable.ic__germany),
    EN(R.string.lang_en, "en", "en", R.drawable.ic__united_kingdom),
    ES(R.string.lang_sp, "es", "es", R.drawable.ic__spain),
    FR(R.string.lang_fr, "fr", "fr", R.drawable.ic__france),
    IT(R.string.lang_it, "it", "it", R.drawable.ic__italy),
    PL(R.string.lang_pl, "pl", "pl", R.drawable.ic__poland),
    PT(R.string.lang_por, "pt", "pt", R.drawable.ic__portugal),
    RU(R.string.lang_ru, "ru", "ru", R.drawable.ic__russia),
    TR(R.string.lang_tur, "tr", "tr", R.drawable.ic__turkey);

    override fun getNameRes() = this.descriptionRes

    override fun getIconRes() = this.icon
}

fun getByIndex(index: Int) = SupportedLanguage.values()[index]