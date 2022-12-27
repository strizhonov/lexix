package com.strizhonovapps.lexicaapp.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.strizhonovapps.lexicaapp.di.DiComponentFactory

class AddWordFromOutsideActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.action != Intent.ACTION_SEND || "text/plain" != intent.type) {
            Log.w(this.javaClass.name, "Illegal intent ${intent.action}, ${intent.type}")
            return
        }
        DiComponentFactory.initIfNecessary(this)
        val addWordIntent = Intent(this, AddWordActivity::class.java)
        addWordIntent.type = "text/plain"
        addWordIntent.action = Intent.ACTION_SEND
        addWordIntent.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT))
        startActivity(addWordIntent)
        finish()
    }

}