package com.strizhonovapps.lexixapp.view

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import com.strizhonovapps.lexixapp.R
import com.strizhonovapps.lexixapp.model.AllowedWordCardSide
import com.strizhonovapps.lexixapp.model.Word
import com.strizhonovapps.lexixapp.service.WordServiceImpl.WordAddon.isHard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ModifyWordActivity : BaseWordManipulationActivity() {

    private var current: Word? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity__modify_word)
        super.setTitle(getString(R.string.activity_modify_word__title))

        nameEditText = findViewById(R.id.activity_modify_word__edit_text__word_name)
        transEditText = findViewById(R.id.activity_modify_word__edit_text__word_translation)
        wordNameSpinner = findViewById(R.id.activity_modify_word__spinner__new_word)
        translationSpinner = findViewById(R.id.activity_modify_word__spinner__translation)
        studyToNativeRadio = findViewById(R.id.activity_modify_word__radio__study_to_native)
        nativeToStudyRadio = findViewById(R.id.activity_modify_word__radio__native_to_study)
        allWordCardSidesRadio = findViewById(R.id.activity_modify_word__radio__all_sides)

        findViewById<Button>(R.id.activity_modify_word__button__update).setOnClickListener(this)
        findViewById<ImageButton>(R.id.activity_modify_word__button__translate)
            .setOnClickListener(this)
        findViewById<ImageButton>(R.id.activity_modify_word__button__delete).setOnClickListener(this)

        val word = WordIntentExtraMapper(intent).getWord()
        current = word

        nameEditText.setText(word.name)
        transEditText.setText(word.translation)

        when (word.allowedWordCardSide) {
            AllowedWordCardSide.ALL -> allWordCardSidesRadio.isChecked = true
            AllowedWordCardSide.STUDY -> studyToNativeRadio.isChecked = true
            AllowedWordCardSide.NATIVE -> nativeToStudyRadio.isChecked = true
        }

        prepareArchiveButtons(word.isArchived)

        setSpinners(word.tag)

        if (word.isHard()) {
            val removeHardTagButton =
                findViewById<Button>(R.id.activity_modify_word__button__remove_hard_tag)
            removeHardTagButton.setOnClickListener(this)
            removeHardTagButton.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View) {
        val word = current ?: throw IllegalStateException("No word were found from intent")
        when (v.id) {
            R.id.activity_modify_word__button__update -> {
                updateWord(word)
                finish()
            }

            R.id.activity_modify_word__button__delete -> {
                wordService.delete(word.id)
                finish()
            }

            R.id.activity_modify_word__button__remove_hard_tag -> {
                wordService.uncheckHard(word.id)
                finish()
            }

            R.id.activity_modify_word__button__archive -> {
                wordService.archive(word.id)
                finish()
            }

            R.id.activity_modify_word__button__unarchive -> {
                wordService.unarchive(word.id)
                finish()
            }

            R.id.activity_modify_word__button__translate -> {
                translate()
            }
        }
    }

    private fun prepareArchiveButtons(isArchived: Boolean) {
        val archiveButton = findViewById<ImageButton>(R.id.activity_modify_word__button__archive)
        val unarchiveButton =
            findViewById<ImageButton>(R.id.activity_modify_word__button__unarchive)
        unarchiveButton.setOnClickListener(this)
        archiveButton.setOnClickListener(this)

        if (isArchived) {
            archiveButton.visibility = View.GONE
            unarchiveButton.visibility = View.VISIBLE
        } else {
            unarchiveButton.visibility = View.GONE
            archiveButton.visibility = View.VISIBLE
        }
    }

    private fun updateWord(word: Word) {
        word.name = nameEditText.text.toString()
        word.translation = transEditText.text.toString()
        word.allowedWordCardSide =
            if (allWordCardSidesRadio.isChecked) AllowedWordCardSide.ALL
            else if (nativeToStudyRadio.isChecked) AllowedWordCardSide.NATIVE
            else AllowedWordCardSide.STUDY
        wordService.update(word)
    }
}