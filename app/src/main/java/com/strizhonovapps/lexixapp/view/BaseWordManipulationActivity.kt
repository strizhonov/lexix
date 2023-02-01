package com.strizhonovapps.lexixapp.view

import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.AppCompatSpinner
import com.strizhonovapps.lexixapp.R
import com.strizhonovapps.lexixapp.model.SupportedLanguage
import com.strizhonovapps.lexixapp.service.LanguageService
import com.strizhonovapps.lexixapp.service.WordService
import com.strizhonovapps.lexixapp.service.WordSuggestionService
import com.strizhonovapps.lexixapp.viewsupport.WithIconListAdapterProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.collections4.iterators.LoopingListIterator
import javax.inject.Inject

private const val LANG_SPINNER_TEXT_SIZE = 14f

abstract class BaseWordManipulationActivity : ComponentActivity(), View.OnClickListener {

    @Inject
    lateinit var languageService: LanguageService

    @Inject
    lateinit var wordService: WordService

    @Inject
    lateinit var wordSuggestionService: WordSuggestionService

    protected lateinit var nameEditText: EditText
    protected lateinit var transEditText: EditText
    protected lateinit var wordNameSpinner: AppCompatSpinner
    protected lateinit var translationSpinner: AppCompatSpinner
    protected lateinit var studyToNativeRadio: RadioButton
    protected lateinit var nativeToStudyRadio: RadioButton
    protected lateinit var allWordCardSidesRadio: RadioButton

    private var translationsCache: Pair<String, LoopingListIterator<String>>? = null

    protected fun setSpinners(wordTag: String? = null) {
        languageService.getNativeLanguage()
            .also(::setNativeLangSpinner)
            .also { setStudyLangSpinner(wordTag) }
    }

    protected fun translate() {
        val name = nameEditText.text.toString()
        val existingTranslation = transEditText.text.toString()
        if (name.isBlank().and(existingTranslation.isBlank())) {
            Toast.makeText(
                applicationContext,
                getString(R.string.activity_modify_word__toast__no_input),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (name.isNotBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                val nextIteration = tryToGetIteratorWithTranslations()?.nextOrNull()
                CoroutineScope(Dispatchers.Main).launch {
                    nextIteration
                        ?.let { translation -> transEditText.setText(translation) }
                        ?: showTranslationNotFoundToast()
                }
            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val translation = languageService.translateFromNative(existingTranslation)
                CoroutineScope(Dispatchers.Main).launch {
                    translation
                        ?.let(nameEditText::setText)
                        ?: showTranslationNotFoundToast()
                }
            }
        }
    }

    private fun setNativeLangSpinner(nativeLang: SupportedLanguage) {
        val langs = SupportedLanguage.values()
        translationSpinner.adapter =
            WithIconListAdapterProvider(applicationContext, LANG_SPINNER_TEXT_SIZE).getListAdapter(
                langs
            )
        translationSpinner.setSelection(nativeLang.ordinal)
        translationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedView: View?,
                position: Int,
                id: Long
            ) {
                languageService.setNativeLanguage(langs[position])
                translationsCache = null
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }

    private fun setStudyLangSpinner(wordTag: String? = null) {
        val langs = SupportedLanguage.values()
        wordNameSpinner.adapter =
            WithIconListAdapterProvider(applicationContext, LANG_SPINNER_TEXT_SIZE).getListAdapter(
                langs
            )
        val studyLanguage = wordTag?.let {
            SupportedLanguage.valueOf(it)
        } ?: try {
            languageService.getStudyLanguage()
        } catch (e: Exception) {
            SupportedLanguage.EN
        }
        langs.indexOf(studyLanguage).let(wordNameSpinner::setSelection)
        wordNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedView: View?,
                position: Int,
                id: Long
            ) {
                languageService.setStudyLanguage(langs[position])
                translationsCache = null
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }

    private suspend fun tryToGetIteratorWithTranslations(): LoopingListIterator<String>? = try {
        val queryString = nameEditText.text.toString()
        if (translationsCache?.first == queryString) {
            translationsCache?.second
        } else {
            val toIterate = languageService.getAllTranslations(queryString)
            val iterator = LoopingListIterator(toIterate)
            translationsCache = Pair(queryString, iterator)
            iterator
        }
    } catch (e: Exception) {
        Log.e(this.javaClass.simpleName, "Unable to find a translation", e)
        showTranslationNotFoundToast()
        null
    }

    private fun showTranslationNotFoundToast() {
        Toast.makeText(
            applicationContext,
            getString(R.string.activity_modify_word__toast__translation_not_found),
            Toast.LENGTH_LONG
        ).show()
    }

    companion object IteratorAddon {
        @JvmStatic
        fun <T> Iterator<T>.nextOrNull(): T? {
            return try {
                next()
            } catch (e: NoSuchElementException) {
                null
            }
        }
    }
}