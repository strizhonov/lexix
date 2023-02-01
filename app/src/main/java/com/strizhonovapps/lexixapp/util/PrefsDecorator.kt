package com.strizhonovapps.lexixapp.util

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton


/** Preference keys */
private const val LAST_NOTIFIED_ABOUT_ACTIVE_WORDS_PREFS_KEY = "last_notified_active"
private const val TRAINING_PREFS_KEY = "training_key"
private const val NATIVE_LANGUAGE_PREFS_KEY = "NATIVE_LANGUAGE"
private const val STUDY_LANGUAGE_PREFS_KEY = "STUDY_LANGUAGE"

@Suppress("SameParameterValue")
@Singleton
class PrefsDecorator @Inject constructor(private val prefs: SharedPreferences) {

    fun getLastNotifiedAboutActiveWords(): Long? {
        return this.getLong(LAST_NOTIFIED_ABOUT_ACTIVE_WORDS_PREFS_KEY)
    }

    fun setLastNotifiedAboutActiveWords(now: Long) {
        this.putLong(LAST_NOTIFIED_ABOUT_ACTIVE_WORDS_PREFS_KEY, now)
    }

    fun getTrainingType(): String? {
        return this.getString(TRAINING_PREFS_KEY)
    }

    fun setTrainingType(value: String) {
        return this.putString(TRAINING_PREFS_KEY, value)
    }

    fun getStudyLang(): String? {
        return this.getString(STUDY_LANGUAGE_PREFS_KEY)
    }

    fun getNativeLang(): String? {
        return this.getString(NATIVE_LANGUAGE_PREFS_KEY)
    }

    fun setStudyLang(value: String) {
        return this.putString(STUDY_LANGUAGE_PREFS_KEY, value)
    }

    fun setNativeLang(value: String) {
        return this.putString(NATIVE_LANGUAGE_PREFS_KEY, value)
    }

    private fun putBoolean(key: String, value: Boolean) {
        prefs.edit().apply {
            this.putBoolean(key, value)
            apply()
        }
    }

    private fun putString(key: String, value: String) {
        prefs.edit().apply {
            this.putString(key, value)
            apply()
        }
    }

    private fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    private fun getLong(key: String): Long? {
        return if (prefs.contains(key)) prefs.getLong(key, 0L)
        else null
    }

    private fun getBoolean(key: String, default: Boolean): Boolean {
        return prefs.getBoolean(key, default)
    }

    private fun putLong(key: String, value: Long) {
        prefs.edit().apply {
            this.putLong(key, value)
            apply()
        }
    }
}