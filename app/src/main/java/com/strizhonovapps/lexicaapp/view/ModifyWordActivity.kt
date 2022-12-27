package com.strizhonovapps.lexicaapp.view

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import com.strizhonovapps.lexicaapp.*
import com.strizhonovapps.lexicaapp.di.DiComponentFactory
import com.strizhonovapps.lexicaapp.model.Word
import java.util.*


class ModifyWordActivity : BaseWordManipulationActivity() {

    init {
        DiComponentFactory.getInstance().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity__modify_word)
        super.setTitle(getString(R.string.activity_modify_word__title))

        nameEditText = findViewById(R.id.activity_modify_word__edit_text__word_name)
        transEditText = findViewById(R.id.activity_modify_word__edit_text__word_translation)
        wordNameSpinner = findViewById(R.id.activity_modify_word__spinner__new_word)
        translationSpinner = findViewById(R.id.activity_modify_word__spinner__translation)

        intent.getStringExtra(NAME_KEY).let(nameEditText::setText)
        intent.getStringExtra(TRANSLATION_KEY).let(transEditText::setText)

        findViewById<Button>(R.id.activity_modify_word__button__update).setOnClickListener(this)
        findViewById<ImageButton>(R.id.activity_modify_word__button__translate)
            .setOnClickListener(this)
        findViewById<ImageButton>(R.id.activity_modify_word__button__delete).setOnClickListener(this)

        prepareArchiveButtons()
        setSpinners()

        if (intent.getBooleanExtra(IS_HARD_WORD, false)) {
            val removeHardTagButton =
                findViewById<Button>(R.id.activity_modify_word__button__remove_hard_tag)
            removeHardTagButton.setOnClickListener(this)
            removeHardTagButton.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.activity_modify_word__button__update -> {
                updateWord()
                finish()
            }
            R.id.activity_modify_word__button__delete -> {
                deleteWord()
                finish()
            }
            R.id.activity_modify_word__button__remove_hard_tag -> {
                removeHardTag()
                finish()
            }
            R.id.activity_modify_word__button__archive -> {
                archiveButton()
                finish()
            }
            R.id.activity_modify_word__button__unarchive -> {
                unarchiveButton()
                finish()
            }
            R.id.activity_modify_word__button__translate -> {
                translate()
            }
        }
    }

    private fun prepareArchiveButtons() {
        val archiveButton = findViewById<ImageButton>(R.id.activity_modify_word__button__archive)
        val unarchiveButton =
            findViewById<ImageButton>(R.id.activity_modify_word__button__unarchive)

        if (intent.getBooleanExtra(IS_ARCHIVED_KEY, false)) {
            archiveButton.visibility = View.GONE
            unarchiveButton.setOnClickListener(this)
            unarchiveButton.visibility = View.VISIBLE
        } else {
            unarchiveButton.visibility = View.GONE
            archiveButton.setOnClickListener(this)
            archiveButton.visibility = View.VISIBLE
        }
    }

    private fun updateWord() {
        val id = intent.getLongExtra(ID_KEY, 0L)
        val lvl = intent.getIntExtra(LVL_KEY, 0)
        val targetDate = Date(intent.getLongExtra(TARGET_DATE_TIME_KEY, System.currentTimeMillis()))
        val modificationDate =
            Date(intent.getLongExtra(MODIFICATION_DATE_TIME_KEY, System.currentTimeMillis()))
        val archived = intent.getBooleanExtra(IS_ARCHIVED_KEY, false)
        val timesShown = intent.getLongExtra(TIMES_SHOWN_KEY, 0L)

        val name = nameEditText.text.toString()
        val translation = transEditText.text.toString()

        val word = Word(
            id = id,
            name = name,
            translation = translation,
            level = lvl,
            targetDate = targetDate,
            modificationDate = modificationDate,
            isArchived = archived,
            timesShown = timesShown,
            tag = langService.getStudyLanguage().name
        )
        wordService.update(word)
    }

    private fun deleteWord() {
        val id = intent.getLongExtra(ID_KEY, 0L)
        wordService.delete(id)
    }

    private fun unarchiveButton() {
        val id = intent.getLongExtra(ID_KEY, 0L)
        wordService.unarchive(id)
    }

    private fun archiveButton() {
        val id = intent.getLongExtra(ID_KEY, 0L)
        wordService.archive(id)
    }

    private fun removeHardTag() {
        val id = intent.getLongExtra(ID_KEY, 0L)
        wordService.resetTimesShown(id)
    }
}