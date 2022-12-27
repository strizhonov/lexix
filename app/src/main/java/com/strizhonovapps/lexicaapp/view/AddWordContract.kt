package com.strizhonovapps.lexicaapp.view

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.strizhonovapps.lexicaapp.ID_KEY
import com.strizhonovapps.lexicaapp.NAME_KEY

class AddWordContract<Any> : ActivityResultContract<Any, Pair<String, Long>>() {

    override fun createIntent(context: Context, input: Any) =
        Intent(context, AddWordActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): Pair<String, Long> =
        Pair(
            intent?.getStringExtra(NAME_KEY) ?: "",
            intent?.getLongExtra(ID_KEY, -1) ?: -1
        )

}