package com.strizhonovapps.lexicaapp.view

import android.app.Activity
import android.os.Bundle
import com.strizhonovapps.lexicaapp.SEPARATOR_KEY
import com.strizhonovapps.lexicaapp.di.DiComponentFactory
import com.strizhonovapps.lexicaapp.service.WordService
import javax.inject.Inject

class FromFileSavingBackgroundActivity : Activity() {

    @Inject
    lateinit var wordService: WordService

    init {
        DiComponentFactory.getInstance().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inputStream = intent.data?.let(contentResolver::openInputStream)
        val separator = intent.getStringExtra(SEPARATOR_KEY)
        inputStream?.let { nonNullInputStream ->
            separator?.let { separator ->
                wordService.saveAllFromStream(nonNullInputStream, separator)
            }
        }
        finish()
    }

}