package com.strizhonovapps.lexixapp.view

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.strizhonovapps.lexixapp.model.Word

class AddWordContract<Any> : ActivityResultContract<Any, Word?>() {

    override fun createIntent(context: Context, input: Any) =
        Intent(context, AddWordActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): Word? =
        intent?.let { WordIntentExtraMapper(it).getWord() }
}

