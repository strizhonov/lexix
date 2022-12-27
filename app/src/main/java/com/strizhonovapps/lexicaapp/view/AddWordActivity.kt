package com.strizhonovapps.lexicaapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import com.strizhonovapps.lexicaapp.ID_KEY
import com.strizhonovapps.lexicaapp.NAME_KEY
import com.strizhonovapps.lexicaapp.R
import com.strizhonovapps.lexicaapp.di.DiComponentFactory
import com.strizhonovapps.lexicaapp.model.Word


class AddWordActivity : BaseWordManipulationActivity() {

    private var loadingBar: ProgressBar? = null

    init {
        DiComponentFactory.getInstance().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity__add_word)

        title = getString(R.string.activity_add_word__title)
        nameEditText = findViewById(R.id.activity_add_word__edit_text__word_name)
        transEditText = findViewById(R.id.activity_add_word__edit_text__word_translation)
        wordNameSpinner = findViewById(R.id.activity_add_word__spinner__new_word)
        translationSpinner = findViewById(R.id.activity_add_word__spinner__translation)

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
                addWord()
                finish()
            }
            R.id.activity_add_word__button__translate -> {
                translate()
            }
            R.id.activity_add_word__button__suggest_word -> {
                suggestWord()
            }
        }
    }

    private fun addWord() {
        val name = nameEditText.text.toString()
        val translation = transEditText.text.toString()
        val newWordId = wordService.add(
            Word(
                name = name,
                translation = translation,
                tag = langService.getStudyLanguage().name
            )
        )
        val resultIntent = Intent()
        resultIntent.putExtra(ID_KEY, newWordId)
        resultIntent.putExtra(NAME_KEY, name)
        setResult(RESULT_OK, resultIntent)
    }

    private fun suggestWord() {
        loadingBar?.visibility = View.VISIBLE
        runBackgroundThenUi(
            this,
            {
                try {
                    val suggestion = wordSuggestionService.findNextWordForSuggestion()
                    if (suggestion == null) {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.activity_add_word__toast__no_next_word_suggestion),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    suggestion
                } catch (e: Exception) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.activity_add_word__toast__no_next_word_suggestion),
                        Toast.LENGTH_LONG
                    ).show()
                    null
                }
            },
            {
                nameEditText.setText(it?.first)
                transEditText.setText(it?.second)
                loadingBar?.visibility = View.INVISIBLE
            }
        )
    }

}