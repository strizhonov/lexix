package com.strizhonovapps.lexixapp.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddWordFromOutsideActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.action != Intent.ACTION_SEND || "text/plain" != intent.type) {
            Log.w(this.javaClass.name, "Illegal intent ${intent.action}, ${intent.type}")
            return
        }
        val addWordIntent = Intent(this, AddWordActivity::class.java)
        addWordIntent.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT))
        startActivity(addWordIntent)
        finish()
    }

}