package com.strizhonovapps.lexicaapp.view

import android.app.Activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


fun <IntermediateType> runBackgroundThenUi(
    activity: Activity,
    backgroundFn: () -> IntermediateType,
    uiFn: (IntermediateType) -> Unit
) {
    CoroutineScope(Dispatchers.Default).launch {
        val intermediateType = backgroundFn()
        activity.runOnUiThread {
            uiFn(intermediateType)
        }
    }
}
