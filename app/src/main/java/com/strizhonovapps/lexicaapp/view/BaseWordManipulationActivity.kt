package com.strizhonovapps.lexicaapp.view

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatSpinner
import com.strizhonovapps.lexicaapp.R
import com.strizhonovapps.lexicaapp.model.SupportedLanguage
import com.strizhonovapps.lexicaapp.service.WordOperationsServiceImpl
import com.strizhonovapps.lexicaapp.service.WordService
import com.strizhonovapps.lexicaapp.service.WordSuggestionService
import com.strizhonovapps.lexicaapp.viewsupport.WithIconListAdapterProvider
import org.apache.commons.collections4.iterators.LoopingListIterator
import javax.inject.Inject

abstract class BaseWordManipulationActivity : Activity(), View.OnClickListener {

    @Inject
    lateinit var langService: WordOperationsServiceImpl

    @Inject
    lateinit var wordService: WordService

    @Inject
    lateinit var wordSuggestionService: WordSuggestionService

    protected lateinit var nameEditText: EditText
    protected lateinit var transEditText: EditText
    protected lateinit var wordNameSpinner: AppCompatSpinner
    protected lateinit var translationSpinner: AppCompatSpinner

    private var wordNameIteratorPair: Pair<String, LoopingListIterator<String>>? = null

    protected fun setSpinners() {
        langService.getNativeLanguage()
            .also(::setNativeLangSpinner)
            .also(::setStudyLangSpinner)
    }

    protected fun translate() {
        val name = nameEditText.text.toString()
        if (name.isNotBlank()) {
            tryToGetIteratorWithTranslations()?.let(::setNextTranslationToView)
            return
        }
        val translation = transEditText.text.toString()
        if (translation.isNotBlank()) {
            langService.getTranslationFromNative(translation)
                ?.let(nameEditText::setText)
                ?: Toast.makeText(
                    applicationContext,
                    getString(R.string.activity_modify_word__toast__translation_not_found),
                    Toast.LENGTH_LONG
                ).show()
        }
    }

    private fun setNativeLangSpinner(nativeLang: SupportedLanguage) {
        val langs = SupportedLanguage.values()
        translationSpinner.adapter =
            WithIconListAdapterProvider(applicationContext).getListAdapter(langs)
        translationSpinner.setSelection(nativeLang.ordinal)
        translationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedView: View?,
                position: Int,
                id: Long
            ) {
                langService.setNativeLanguage(langs[position])
                setStudyLangSpinner(langs[position])
                wordNameIteratorPair = null
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }

    private fun setStudyLangSpinner(nativeLang: SupportedLanguage) {
        val langs = getStudyLangs(nativeLang)
        wordNameSpinner.adapter =
            WithIconListAdapterProvider(applicationContext).getListAdapter(langs)
        val studyLanguage = try {
            langService.getStudyLanguage()
        } catch (e: Exception) {
            SupportedLanguage.EN
        }
        getStudySelectionIdx(langs, studyLanguage).let(wordNameSpinner::setSelection)
        wordNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedView: View?,
                position: Int,
                id: Long
            ) {
                langService.setStudyLanguage(langs[position])
                wordNameIteratorPair = null
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }

    private fun getStudyLangs(nativeLang: SupportedLanguage): Array<SupportedLanguage> {
        val langs = langService.getSupportedByYdxStudyLangsForNativeLang(nativeLang.toString())
        return if (langs.isNullOrEmpty()) {
            Log.e(this.javaClass.simpleName, "Unable to find study language")
            Toast.makeText(
                applicationContext,
                getString(R.string.activity_add_word__toast__check_internet),
                Toast.LENGTH_LONG
            ).show()
            arrayOf(SupportedLanguage.EN)
        } else {
            langs
        }
    }

    private fun getStudySelectionIdx(
        langs: Array<SupportedLanguage>,
        studyLanguage: SupportedLanguage
    ): Int {
        return if (langs.contains(studyLanguage)) langs.indexOf(studyLanguage)
        else if (langs.contains(SupportedLanguage.EN)) langs.indexOf(SupportedLanguage.EN)
        else if (langs.contains(SupportedLanguage.RU)) langs.indexOf(SupportedLanguage.RU)
        else if (langs.isNotEmpty()) 0
        else throw IllegalStateException("Unable to detect study language")
    }

    private fun tryToGetIteratorWithTranslations(): LoopingListIterator<String>? = try {
        val queryString = nameEditText.text.toString()
        if (wordNameIteratorPair?.first == queryString) {
            wordNameIteratorPair?.second
        } else {
            val toIterate = langService.getAllTranslations(queryString)
            val iterator = LoopingListIterator(toIterate)
            wordNameIteratorPair = Pair(queryString, iterator)
            iterator
        }
    } catch (e: Exception) {
        Log.e(this.javaClass.simpleName, "Unable to find a translation", e)
        Toast.makeText(
            applicationContext,
            getString(R.string.activity_modify_word__toast__translation_not_found),
            Toast.LENGTH_LONG
        ).show()
        null
    }

    private fun setNextTranslationToView(iterator: LoopingListIterator<String>) {
        if (!iterator.hasNext()) {
            Toast.makeText(
                applicationContext,
                getString(R.string.activity_modify_word__toast__translation_not_found),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        transEditText.setText(iterator.next())
    }
}