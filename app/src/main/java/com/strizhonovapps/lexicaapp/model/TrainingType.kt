package com.strizhonovapps.lexicaapp.model

import com.strizhonovapps.lexicaapp.R

enum class TrainingType(
    private val iconRes: Int,
    private val descriptionRes: Int
) : IconedAndNamed {

    STUDY_TO_NATIVE(R.drawable.ic__study_to_native, R.string.fragment_pre_training__button__study_to_native_training),
    NATIVE_TO_STUDY(R.drawable.ic__native_to_study, R.string.fragment_pre_training__button__native_to_study_training),
    MIXED(R.drawable.ic__mixed, R.string.fragment_pre_training__button__mixed_training);

    override fun getNameRes() = this.descriptionRes

    override fun getIconRes() = this.iconRes
}