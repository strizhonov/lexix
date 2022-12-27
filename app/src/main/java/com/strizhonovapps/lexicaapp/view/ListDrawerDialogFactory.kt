package com.strizhonovapps.lexicaapp.view

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.EditText
import com.strizhonovapps.lexicaapp.R
import com.strizhonovapps.lexicaapp.service.WordService

class ListDrawerDialogFactory(
    private val activity: Activity,
    private val wordService: WordService
) {

    fun getEraseConfirmationDialog(onPositiveButtonCallback: () -> Unit): AlertDialog =
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.dialog__content__clear_all))
            .setMessage(activity.getString(R.string.dialog__content__are_you_sure))
            .setPositiveButton(activity.getString(R.string.dialog__content__yes)) { _, _ ->
                wordService.erase()
                onPositiveButtonCallback()
            }
            .setNegativeButton(activity.getString(R.string.dialog__content__cancel), null)
            .create()

    fun getSeparatorDialog(
        listener: DialogInterface.OnClickListener,
        separatorEditText: EditText
    ): AlertDialog = AlertDialog.Builder(activity)
        .setTitle(activity.getString(R.string.dialog__content__insert_separator))
        .setMessage(activity.getString(R.string.dialog__content__separator_message))
        .setView(separatorEditText)
        .setPositiveButton(activity.getString(R.string.dialog__content__done), listener)
        .setNegativeButton(activity.getString(R.string.dialog__content__cancel), null)
        .create()


    fun getResetProgressDialog(onPositiveButtonCallback: () -> Unit): AlertDialog =
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.dialog__content__reset_progress))
            .setMessage(activity.getString(R.string.dialog__content__are_you_sure))
            .setPositiveButton(activity.getString(R.string.dialog__content__yes)) { _, _ ->
                wordService.resetProgress()
                onPositiveButtonCallback()
            }
            .setNegativeButton(activity.getString(R.string.dialog__content__cancel), null)
            .create()

}