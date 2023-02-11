package com.strizhonovapps.lexixapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import com.strizhonovapps.lexixapp.R
import com.strizhonovapps.lexixapp.model.WordCardSide
import com.strizhonovapps.lexixapp.model.Word
import com.strizhonovapps.lexixapp.util.toDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

const val INITIAL_DELAY_HOURS = 12L

@AndroidEntryPoint
class AddWordActivity : BaseWordManipulationActivity() {

    private var loadingBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity__add_word)

        title = getString(R.string.activity_add_word__title)

        nameEditText = findViewById(R.id.activity_add_word__edit_text__word_name)
        transEditText = findViewById(R.id.activity_add_word__edit_text__word_translation)

        wordNameSpinner = findViewById(R.id.activity_add_word__spinner__new_word)
        translationSpinner = findViewById(R.id.activity_add_word__spinner__translation)

        studyToNativeRadio = findViewById(R.id.activity_add_word__radio__study_to_native)
        nativeToStudyRadio = findViewById(R.id.activity_add_word__radio__native_to_study)
        allWordCardSidesRadio = findViewById(R.id.activity_add_word__radio__all_sides)

        loadingBar = findViewById(R.id.activity_add_word__progress_bar__loading)

        findViewById<ImageButton>(R.id.activity_add_word__button__translate).setOnClickListener(this)
        findViewById<Button>(R.id.activity_add_word__button__add).setOnClickListener(this)
        findViewById<Button>(R.id.activity_add_word__button__suggest_word).setOnClickListener(this)
        setSpinners()

        if (intent?.action == Intent.ACTION_SEND && "text/plain" == intent.type) {
            nameEditText.setText(intent.getStringExtra(Intent.EXTRA_TEXT))
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.activity_add_word__button__add -> {
                val id = addWord()
                val resultIntent = Intent()
                WordIntentExtraMapper(resultIntent).setWord(
                    Word(
                        id = id,
                        name = nameEditText.text.toString()
                    )
                )
                setResult(RESULT_OK, resultIntent)
                finish()
            }

            R.id.activity_add_word__button__translate -> {
                translate()
            }

            R.id.activity_add_word__button__suggest_word -> {
                loadingBar?.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    val nextSuggestedWord = try {
                        wordSuggestionService.findNextWordForSuggestion()
                    } catch (e: Exception) {
                        null
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        if (nextSuggestedWord == null) {
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.activity_add_word__toast__no_next_word_suggestion),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            nameEditText.setText(nextSuggestedWord.first)
                            transEditText.setText(nextSuggestedWord.second)
                        }
                        loadingBar?.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun addWord(): Long {
        val name = nameEditText.text.toString()
        val translation = transEditText.text.toString()
        val sideToShow =
            if (allWordCardSidesRadio.isChecked) WordCardSide.ALL
            else if (nativeToStudyRadio.isChecked) WordCardSide.NATIVE
            else WordCardSide.STUDY

        val newWord = Word(
            name = name,
            targetDate = toDate(LocalDateTime.now().plusHours(INITIAL_DELAY_HOURS)),
            translation = translation,
            tag = languageService.getStudyLanguage().name,
            allowedWordCardSide = sideToShow
        )

        return wordService.add(newWord)
    }
}