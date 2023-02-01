package com.strizhonovapps.lexixapp.model

import com.strizhonovapps.lexixapp.R


enum class SupportedLanguage(
    private val descriptionRes: Int,
    val yndxLangKey: String,
    val leoLangKey: String,
    private val icon: Int,
) : IconedAndNamed {
    EN(R.string.lang_en, "en", "en", R.drawable.ic__united_kingdom),
    PL(R.string.lang_pl, "pl", "pl", R.drawable.ic__poland),
    RU(R.string.lang_ru, "ru", "ru", R.drawable.ic__russia);

    override fun getNameRes() = this.descriptionRes

    override fun getIconRes() = this.icon
}